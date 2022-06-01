package tradly.social.common.network.retrofit

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.rollbar.android.Rollbar
import io.sentry.Sentry
import io.sentry.SentryEvent
import io.sentry.SentryLevel
import io.sentry.protocol.Message
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import tradly.social.common.network.BuildConfig
import tradly.social.common.network.CustomError
import tradly.social.common.network.NetworkError
import tradly.social.common.network.base.AppConstant
import tradly.social.common.network.entity.ResponseStatus
import tradly.social.common.resources.ResourceConfig
import tradly.social.common.util.parser.TradlyParser
import tradly.social.common.util.parser.extension.toObject
import tradly.social.domain.entities.AppError
import tradly.social.domain.entities.Result
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.CancellationException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

object RetrofitManager {

    val retrofit:Retrofit
    private var api: RetrofitAPIService
    private val extendedTime = Pair<Long, TimeUnit>(60, TimeUnit.SECONDS)

    init {
        retrofit = getRetrofitInstance()
        api = retrofit.create(RetrofitAPIService::class.java)
    }

    fun <T>getApiService(cls: Class<T>) = retrofit.create(cls)

    fun getDefaultApiService() = api

    private fun getRetrofitInstance(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(TradlyParser.moshi))
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .client(getOkHttpClient())
            .build()
    }

    private fun getOkHttpClient() = OkHttpClient.Builder().apply {
                addInterceptor(RetrofitInterceptor())
                authenticator(RetrofitAuthenticator())
                when (BuildConfig.BUILD_TYPE) {
                    "debug", "sandBox" -> addInterceptor(getHttpLoggingInterceptor())
                }
                val (timeout,unit) = extendedTime
                connectTimeout(timeout,unit)
                readTimeout(timeout, unit)
                writeTimeout(timeout, unit)
                retryOnConnectionFailure(true)
            }.build()

    private fun getHttpLoggingInterceptor() = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    fun <T:Any> getImageUploadResponse(call: Call<T>):Result<Boolean>{
        return try {
            val result = call.execute()
            if (result.code() == 200 || result.code() == 201) {
                Result.Success(data = true)
            } else {
                Result.Error(exception = AppError(code = result.code()))
            }
        }
        catch (exception:Exception){
            getExceptionResponse(call.request(),exception)
        }
    }

    fun <T : Any> getResponse(call: Call<T>): Result<T> {
        val apiResult: Result<T> = try {
            val result = call.execute()
            val responseBody = result.body()
            responseBody?.let { Result.Success(it) } ?: run { getErrorResponse(result) }
        } catch (exception: Exception) {
            getExceptionResponse(call.request(),exception)
        }
        when(apiResult){
            is Result.Error->{
                val apiError = apiResult.exception
                if(apiError.errorType!= NetworkError.TASK_CANCELLATION && shouldLog(apiError.networkResponseCode,apiError.code)){
                    logSentryEvent(apiError.message.orEmpty(),apiError.code,apiError.url,apiError.payload.orEmpty())
                }
            }
        }
        return apiResult
    }


    private fun <T> getErrorResponse(response: Response<T>) =
        response.errorBody()?.let {
            if (response.code() == 422) {
                Result.Error(getApiError(response.raw().request,response.message(), response.code(), NetworkError.API_ERROR))
            } else {
                val responseStatus = it.string().toObject<ResponseStatus>()
                val appError = responseStatus?.error
                if (appError!=null){
                    Result.Error(getApiError(response.raw().request,appError.message,appError.code,response.code(), NetworkError.API_ERROR))
                }
                else{
                    Result.Error(getApiError(response.raw().request,response.message(), response.code(), NetworkError.API_UNKNOWN_ERROR))
                }
            }
        } ?: run {
            Result.Error(getApiError(response.raw().request,response.message(), response.code(), NetworkError.API_UNKNOWN_ERROR))
        }

    private fun <T:Any> getExceptionResponse(request: Request,exception:Exception): Result<T> {
        Sentry.captureException(exception)
        Rollbar.reportException(exception)
        return when (exception) {
            is TimeoutException,
            is ConnectException,
            is UnknownHostException,
            is SocketTimeoutException -> Result.Error(getApiError(request,exception.message,
                CustomError.TIME_OUT_EXCEPTION,
                NetworkError.NETWORK_ERROR))
            is CancellationException -> Result.Error(AppError(errorType = NetworkError.TASK_CANCELLATION))
            else -> Result.Error(getApiError(request,exception.message, CustomError.UNKNOWN_ERROR,
                NetworkError.UNKNOWN_ERROR))
        }
    }


    private fun getApiError(request: Request, message:String?, code:Int, errorType:Int) = getApiError(request, message, code,code,errorType)

    private fun getApiError(request: Request, message:String?, code:Int, networkResponseCode:Int, errorType: Int): AppError {
        return AppError(
            message,
            code,
            networkResponseCode,errorType = errorType).also {
            if(shouldLog(networkResponseCode,code)){
                it.url = request.url.encodedPath
                it.payload = bodyToString(request)
            }
        }
    }

    private fun bodyToString(request: Request):String{
        return try{
            val buffer = okio.Buffer()
            request.body?.writeTo(buffer)
            buffer.readUtf8()
        }catch (ex:Exception){
            AppConstant.EMPTY
        }
    }

    fun logSentryEvent(errorMessage:String,errorCode:Int,url:String,payload:String){
        if(errorMessage.isNotEmpty()){
            val event = SentryEvent()
            event.level = SentryLevel.ERROR
            event.setTag("app",ResourceConfig.APP_NAME)
            event.message = Message().apply { message = "API Error - Android - $url" }
            event.environment = if(BuildConfig.BUILD_TYPE == "release") "production" else BuildConfig.BUILD_TYPE
            event.setExtras(getSentryExtras(errorMessage, errorCode, url, payload))
            Sentry.captureEvent(event)
        }
    }

    private fun getSentryExtras(errorMessage:String, errorCode:Int, url:String, payload:String):MutableMap<String,Any>{
        val map = mutableMapOf<String,Any>()
        if(payload.isNotEmpty()){
            map["request"] = payload
        }
        map["url"] = url
        map["errorCode"] = errorCode.toString()
        map["message"] = errorMessage
        return map
    }

    private fun shouldLog(networkResponseCode:Int , apiResponseErrorCode:Int):Boolean{
        if(networkResponseCode!=401 && networkResponseCode!=200 && networkResponseCode!=201){
            if(networkResponseCode==412){
                if((apiResponseErrorCode == CustomError.INVALID_OR_MISSING_PARAMETERS || apiResponseErrorCode == CustomError.API_TECH_ISSUES || apiResponseErrorCode == CustomError.ACTION_NOT_ALLOWED)){
                    return true
                }
                return false
            }
            else{
                return when(apiResponseErrorCode){
                    CustomError.CODE_USER_ALREADY_EXISTS,
                    CustomError.CODE_USER_NOT_REGISTERED,
                    CustomError.CODE_INVALID_CREDENTIALS,
                    CustomError.CODE_VERIFY_CODE_EXPIRED,
                    CustomError.CODE_VERIFY_CODE_INVALID,
                    CustomError.CODE_PICKUP_ADDRES_NOT_SET -> false
                    else-> true
                }
            }
        }
        return false
    }

}