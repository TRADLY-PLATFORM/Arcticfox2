package tradly.social.data.model.network.retrofit

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.rollbar.android.Rollbar
import io.sentry.Sentry
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import tradly.social.BuildConfig
import tradly.social.common.base.AppConstant
import tradly.social.common.network.entity.AppConfigResponse
import tradly.social.common.network.entity.ResponseStatus
import tradly.social.common.network.entity.UserDetailResponse
import tradly.social.common.network.NetworkError
import tradly.social.common.base.ErrorHandler
import tradly.social.common.network.CustomError
import tradly.social.common.network.Header
import tradly.social.common.network.retrofit.RetrofitAPIService
import tradly.social.common.network.retrofit.RetrofitAuthenticator
import tradly.social.common.network.retrofit.RetrofitInterceptor
import tradly.social.common.util.parser.extension.toObject
import tradly.social.domain.entities.AppError
import tradly.social.domain.entities.GeoPoint
import tradly.social.domain.entities.Result
import java.io.File
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.CancellationException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException


class RetrofitManager {

    private var retrofit: Retrofit
    private var api: RetrofitAPIService
    private var okHttpClient: OkHttpClient? = null

    private constructor() {
        retrofit = getRetrofitInstance()
        api = retrofit.create(RetrofitAPIService::class.java)
    }


    companion object {
        @Volatile
        private var INSTANCE: RetrofitManager? = null

        fun getInstance(): RetrofitManager {
            return INSTANCE
                ?: synchronized(this) {
                RetrofitManager().also {
                    INSTANCE = it
                }
            }
        }
    }


    private fun getRetrofitInstance(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(tradly.social.common.util.parser.TradlyParser.moshi))
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .client(getOkHttpClient())
            .build()
    }

    private fun getOkHttpClient(): OkHttpClient? {
        if (okHttpClient != null) {
            return okHttpClient
        } else {
            val okHttpClientBuilder = OkHttpClient.Builder()
            okHttpClientBuilder.apply {
                addInterceptor(RetrofitInterceptor())
                authenticator(RetrofitAuthenticator())
                when (BuildConfig.BUILD_TYPE) {
                    "debug", "sandBox" -> addInterceptor(getHttpLoggingInterceptor())
                }
                connectTimeout(60,TimeUnit.SECONDS)
                readTimeout(60, TimeUnit.SECONDS)
                writeTimeout(60, TimeUnit.SECONDS)
                retryOnConnectionFailure(true)
            }
            okHttpClient = okHttpClientBuilder.build()
            return okHttpClient
        }
    }

    fun <T>getApiService(cls: Class<T>) = retrofit.create(cls)

    fun getDefaultApiService() = api

    private fun getHttpLoggingInterceptor() = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY

    }

    fun getParseConfig(tenantKey: String): Result<AppConfigResponse> =
        try {
            val result = api.getParseConfig(tenantKey).execute()
            val parseAppConfig = result.body()
            if (result.isSuccessful && parseAppConfig != null) {
                when (parseAppConfig.status) {
                    true -> Result.Success(parseAppConfig)
                    false -> Result.Error(ErrorHandler.getErrorInfo(parseAppConfig.error))
                }
            } else {
                getErrorResponse(result)
            }
        } catch (exception: Exception) {
            Result.Error(AppError(exception.message))
        }

    fun login(map: HashMap<String, Any?>): Result<String> =
        when (val result = getResponse(api.login(map))) {
            is Result.Success -> Result.Success(result.data.string())
            is Result.Error -> result
        }

    fun verifyLogin(map: HashMap<String, Any?>) = getResponse(api.verifyUser(map))

    fun signUp(map: HashMap<String, Any?>) = getResponse(api.signUp(map))

    fun signUpNoOTP(map: HashMap<String, Any?>) = getResponse(api.signUpNoOTP(map))

    fun updateUserDetail(userId: String, map: HashMap<String, Any?>) =
        getResponse(api.updateUserDetail(userId, map))

    fun getUserDetail(userId: String) = getResponse(api.getUserDetail(userId))

    fun getCountries() = getResponse(api.getCountries())

    fun getHome(map: HashMap<String, Any?>) = getResponse(api.getHome(map))


    fun getAddressByLocation(geoPoint: GeoPoint) = getResponse(
        api.getAddressByLocation(
            geoPoint.latitude.toString(),
            geoPoint.longitude.toString()
        )
    )

    //region group
    fun getGroupTypes() = getResponse(api.getGroupTypes())

    fun addGroup(map: HashMap<String, Any?>) = getResponse(api.addGroup(map))

    fun searchTag(map: Map<String, Any?>) = getResponse(api.searchTag(map))


    fun getCurrencies() = getResponse(api.getCurrencies())

    fun getCartList(shippingMethodId: String?) = getResponse(api.getCartList(shippingMethodId))

    fun addToCart(map: Map<String, Any?>) = getResponse(api.addToCart(map))

    fun deleteCart(map: Map<String, Any?>) = getResponse(api.deleteCart(map))

    fun getShippingAddress(type: String) = getResponse(api.getAddressList(type))

    fun addShippingAddress(map: Map<String, Any?>) = getResponse(api.addShippingAddress(map))

    fun updateShippingAddress(map: Map<String, Any?>, addressId: String) =
        getResponse(api.updateShippingAddress(map, addressId))

    fun getPaymentTypes() = getResponse(api.getPaymentTypes())

    fun doPasswordRecovery(map: Map<String, Any?>) = getResponse(api.doPasswordRecovery(map))

    fun doPasswordSet(map: Map<String, Any?>) = getResponse(api.passwordReset(map))

    fun connectOAuth() = getResponse(api.connectOAuth())

    fun getAccountLink(map: Map<String, Any?>) = getResponse(api.getAccountLink(map))

    fun getStripeStatus(accountId: String) = getResponse(api.getStripeStatus(accountId))

    fun getStripeLoginLink(map: Map<String, Any?>) = getResponse(api.getStripeLoginLink(map))

    fun disConnectOAuth() = getResponse(api.disconnectOAuth())

    fun verifyStripeOAuth(url: String) = getResponse(api.verifyStripeOAuth(url))

    fun logout(map: Map<String, Any?>) = getResponse(api.logout(map))

    fun socialLogin(map: Map<String, Any>) = getResponse(api.socialLogin(map))

    fun getAppConfig(key: String) = when (val result = getResponse(api.getAppConfig(key))) {
        is Result.Success -> Result.Success(result.data.string())
        is Result.Error -> result
    }

    fun getAppGroupConfig(keyGroup: String) =
        when (val result = getResponse(api.getAppGroupConfig(keyGroup))) {
            is Result.Success -> Result.Success(result.data.string())
            is Result.Error -> result
        }

    fun getNotification(pagination: Int) = getResponse(api.getNotifications(pagination))

    fun getEarnings(accountId: String) = getResponse(api.getEarnings(accountId))

    fun getTransactions(queryMap: Map<String, Any?>) = getResponse(api.getTransactions(queryMap))

    fun searchLocation(key: String) = getResponse(api.searchLocation(key))

    fun getReviewList(map:Map<String,Any?>) = getResponse(api.getReviewList(map))

    fun addReview(map: Map<String, Any?>) =
        getResponse(api.addReview(map))

    fun getTenantShippingMethods() = getResponse(api.getShippingMethods())

    fun getShippingMethods(accountId: String?) =
        getResponse(api.getShippingMethodsByAccount(accountId))

    fun clearCart() = getResponse(api.clearCart())

    fun likeReview(reviewId: String, map: Map<String, Any?>) =
        getResponse(api.likeReview(reviewId, map))

    fun updatePickupAddress(orderId: String, map: Map<String, Any?>) =
        getResponse(api.updatePickupAddress(orderId, map))

    fun getFeedbackCategories() = getResponse(api.getFeedbackCategories())

    fun sendFeedback(map:Map<String,Any?>) = getResponse(api.sendFeedback(map))

    fun makeNegotiation(productId: String,map:Map<String,Any?>) = getResponse(api.makeNegotiation(productId,map))

    fun updateNegotiation(productId:String,negotiationId:String,map: Map<String, Any?>) = getResponse(api.updateNegotiation(productId, negotiationId, map))

    fun refreshToken(): Result<UserDetailResponse> =
        try {
            val result = api.refreshToken().execute()
            val resultBody = result.body()
            if (result.code() == 200 && resultBody != null) {
                Result.Success(resultBody)
            } else {
                getErrorResponse(result)
            }
        } catch (exception: Exception) {
            Result.Error(exception = AppError(exception.message))
        }

    private fun <T : Any> getResponse(call: Call<T>):Result<T>{
        val apiResult:Result<T>
        apiResult = try {
            val result = call.execute()
            val responseBody = result.body()
            responseBody?.let { Result.Success(it) } ?: run { getErrorResponse(result) }
        } catch (exception: Exception) {
            Sentry.captureException(exception)
            Rollbar.reportException(exception)
            when (exception) {
                is TimeoutException,
                is ConnectException,
                is UnknownHostException,
                is SocketTimeoutException -> Result.Error(getApiError(call.request(),exception.message,CustomError.TIME_OUT_EXCEPTION,
                    NetworkError.NETWORK_ERROR))
                is CancellationException -> Result.Error(AppError(errorType = NetworkError.TASK_CANCELLATION))
                else -> Result.Error(getApiError(call.request(),exception.message, CustomError.UNKNOWN_ERROR,
                    NetworkError.NO_NETWORK))
            }
        }
        when(apiResult){
            is Result.Error->{
                val apiError = apiResult.exception
                if(apiError.errorType!= NetworkError.TASK_CANCELLATION && shouldLog(apiError.networkResponseCode,apiError.code)){
                    ErrorHandler.logAPIError(apiResult.exception)
                }
            }
        }
        return apiResult
    }


    private fun <T> getErrorResponse(response: Response<T>) =
        response.errorBody()?.let {
            if (response.code() == 422) {
                Result.Error(getApiError(response.raw().request,response.message(), response.code(),
                    NetworkError.API_ERROR))
            } else {
                val responseStatus = it.string().toObject<ResponseStatus>()
                val appError = ErrorHandler.getErrorInfo(responseStatus?.error)
                Result.Error(getApiError(response.raw().request,appError.message,appError.code,response.code(),
                    NetworkError.API_ERROR))
            }
        } ?: run {
            Result.Error(getApiError(response.raw().request,response.message(), response.code(),
                NetworkError.API_UNKNOWN_ERROR))
        }


    private fun getApiError(request:Request , message:String?,code:Int, errorType:Int) = getApiError(request, message, code,code,errorType)

    private fun getApiError(request:Request , message:String?,code:Int,networkResponseCode:Int, errorType: Int):AppError {
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