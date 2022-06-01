package tradly.social.data.model.dataSource

import tradly.social.common.base.AppController
import tradly.social.common.network.converters.UserModelConverter
import tradly.social.common.network.converters.VerificationConverter
import tradly.social.common.network.entity.UserDetailResponse
import tradly.social.common.network.entity.VerificationResponse
import tradly.social.data.model.FireBaseAuthHelper
import tradly.social.data.model.network.retrofit.RetrofitManager
import tradly.social.common.network.parseHelper.ParseConstant
import tradly.social.common.base.ErrorHandler
import tradly.social.common.base.ErrorHandler.Companion.getErrorInfo
import tradly.social.common.network.CustomError
import tradly.social.common.network.NetworkError
import tradly.social.common.util.parser.extension.toObject
import tradly.social.domain.dataSource.UserAuthenticateDataSource
import tradly.social.domain.entities.*

class ParseUserAuthenticateDataSource : UserAuthenticateDataSource {

    override fun updateUserDetail(userId:String,map: HashMap<String, Any?>): Result<User> {

         when (val result = RetrofitManager.getInstance().updateUserDetail(userId,map)) {
            is Result.Success ->{
                val user = UserModelConverter().mapFrom(result.data)
                val authKeys = AppController.appController.getUser()?.authKeys
                authKeys?.let {
                    user.authKeys = it
                    AppController.appController.cacheUserData(user)
                    return Result.Success(user)
                }?:run{
                    return Result.Error(AppError())
                }
            }
            is Result.Error -> return result
        }
    }


    override fun getUser(id: String):Result<User> = when(val result = RetrofitManager.getInstance().getUserDetail(id)){
            is Result.Success -> {
                if(result.data.status) {
                    val user = UserModelConverter().mapFrom(result.data)
                    val authKeys = AppController.appController.getUser()?.authKeys
                    authKeys?.let {
                        user.authKeys = it
                        AppController.appController.cacheUserData(user)
                        return Result.Success(user)
                    }?:run{
                        return Result.Error(exception = AppError(code = CustomError.AUTH_KEY_NOT_AVAILABLE))
                    }
                }
                else{
                     Result.Error(exception = AppError(code = CustomError.GET_USER_DETAIL_FAILED))
                }
            }
            is Result.Error -> result
        }


    //region Login
    override fun initLogin(uuid: String, mobile: String, countryId: String): Result<Verification> =
        when(val initResult = RetrofitManager.getInstance().login(getLoginRequestBody(uuid,mobile = mobile, countryId = countryId))){
            is Result.Success->{
             val verificationResponse = initResult.data.toObject<VerificationResponse>()
             verificationResponse?.let {
                 if(it.status){
                     Result.Success(VerificationConverter.mapFrom(verificationResponse.verificationEntity))
                 }
                 else{
                     Result.Error(getErrorInfo(it.error))
                 }
             }?:run{
                 Result.Error(AppError())
             }
            }
            is Result.Error->initResult
        }


        override suspend fun verifyAuthentication(verifyId: String?, code: String): Result<User> {
        return when (val verifyResult = RetrofitManager.getInstance().verifyLogin(getVerifyRequestBody(verifyId, code))) {
            is Result.Success -> {
                val userDetailResponse = verifyResult.data
                if(userDetailResponse.status){
                    authenticateWithFirebase(userDetailResponse)
                }
                else{
                    Result.Error(getErrorInfo(userDetailResponse.error))
                }
            }
            is Result.Error -> verifyResult
        }
    }

    override suspend fun login(uuid: String, mobile: String, email: String, password: String, countryId: String?): Result<User> =
        when(val loginResult = RetrofitManager.getInstance().login(getLoginRequestBody(uuid,mobile,email,password,countryId))){
            is Result.Success->{
                val userDetailResponse = loginResult.data.toObject<UserDetailResponse>()
                userDetailResponse?.let {
                    if(it.status){
                        authenticateWithFirebase(it)
                    }
                    else{
                        Result.Error(getErrorInfo(it.error))
                    }
                }?:run{
                    Result.Error(AppError())
                }
            }
            is Result.Error->loginResult
        }
    //endregion

    //region signup
    override fun signUp(
        uuid: String,
        firstName: String,
        lastName: String,
        mobile: String,
        email: String,
        password: String,
        countryId: String?
    ): Result<Verification> =
        when(val signUpResult = RetrofitManager.getInstance().signUp(getSignUpRequestBody(uuid,firstName,lastName, mobile, email, password, countryId))){
            is Result.Success->{
                if(signUpResult.data.status){
                    Result.Success(VerificationConverter.mapFrom(signUpResult.data.verificationEntity))
                }
                else{
                    Result.Error(getErrorInfo(signUpResult.data.error))
                }
            }
            is Result.Error->signUpResult
        }

    //endregion

    override fun doPasswordRecover(
        email: String,
        dialCode: String,
        mobileNo: String
    ): Result<Verification> =
        when( val result = RetrofitManager.getInstance().doPasswordRecovery(getPasswordRecoverRequestBody(email, dialCode, mobileNo))){
            is Result.Success-> Result.Success(VerificationConverter.mapFrom(result.data.verificationEntity))
            is Result.Error-> result
        }

    override suspend fun signUpNoOtp(
        uuid: String,
        firstName: String,
        lastName: String,
        mobile: String,
        email: String,
        password: String,
        countryId: String?
    ): Result<User> {
       return when(val signUpResult = RetrofitManager.getInstance().signUpNoOTP(getSignUpRequestBody(uuid,firstName,lastName, mobile, email, password, countryId))){
            is Result.Success->{
                if(signUpResult.data.status){
                    authenticateWithFirebase(signUpResult.data)
                }
                else{
                    Result.Error(getErrorInfo(signUpResult.data.error))
                }
            }
            is Result.Error->signUpResult
        }
    }

    override fun doPasswordSet(
        password: String,
        verifyId: String,
        code: String
    ): Result<Boolean> =
        when(val result = RetrofitManager.getInstance().doPasswordSet(getPasswordResetRequestBody(verifyId, code, password))){
            is Result.Success->{
                if(result.data.status){
                    Result.Success(result.data.status)
                }
                else{
                    Result.Error(AppError(code = CustomError.SET_PASSWORD_FAILED))
                }
            }
            is Result.Error->result
        }

    override fun logout(uuid: String): Result<Boolean> {
        return when(val result = RetrofitManager.getInstance().logout(getLogoutRequestBody(uuid))){
            is Result.Success->{
                if(result.data.status){
                    Result.Success(result.data.status)
                }
                else{
                    Result.Error(AppError(code = CustomError.LOGOUT_FAILED))
                }
            }
            is Result.Error->result
        }
    }

    private suspend fun authenticateWithFirebase(userDetailResponse: UserDetailResponse) =
        when(FireBaseAuthHelper.signInWithCustomToken(userDetailResponse.response.user.authKeyEntity.customToken)){
            is Result.Success->{
                val user = UserModelConverter().mapFrom(userDetailResponse)
                AppController.appController.cacheUserData(user)
                Result.Success(user)
            }
            is Result.Error->{
                if(userDetailResponse.response.user.authKeyEntity.customToken.isEmpty()){
                    val user = UserModelConverter().mapFrom(userDetailResponse)
                    AppController.appController.cacheUserData(user)
                    Result.Success(user)
                }
                else{
                    val user = UserModelConverter().mapFrom(userDetailResponse)
                    AppController.appController.cacheUserData(user)
                    Result.Success(user)
                    //result
                }
            }
        }

    override suspend fun socialLogin(token: String, provider: String,uuid: String): Result<User> {
        return when(val result = RetrofitManager.getInstance().socialLogin(getSocialLoginRequestBody(token, provider,uuid))){
            is Result.Success->{
                if (result.data.status){
                    authenticateWithFirebase(result.data)
                }
                else{
                    Result.Error(AppError(errorType = NetworkError.API_UNKNOWN_ERROR))
                }
            }
            is Result.Error->result
        }
    }

    //region Request Body

    private fun getSocialLoginRequestBody(token:String,provider:String,uuid: String):HashMap<String,Any>{
        return hashMapOf(
            "provider" to provider,
            "token" to token,
            "is_web" to false,
            "type" to "customer",
            "uuid" to uuid
        )
    }

    private fun getVerifyRequestBody(verifyId: String?, code: String):HashMap<String,Any?> {
        val map = hashMapOf<String,Any?>()
        map["verify_id"] = verifyId
        map["code"] = code
        return map
    }

    private fun getPasswordResetRequestBody(verifyId: String?, code: String,password:String = Constant.EMPTY):HashMap<String,Any?> {
        val map = hashMapOf<String,Any?>()
        map["verify_id"] = verifyId
        map["code"] = code
        map["password"] = password
        return map
    }

    private fun getPasswordRecoverRequestBody(email: String, dialCode: String, mobileNo: String):HashMap<String,Any?>{
        val map = hashMapOf<String,Any?>()
        when(AppController.appController.getAppConfig()?.authType){
            ParseConstant.AuthType.MOBILE_PASSWORD,
            ParseConstant.AuthType.MOBILE_PASSWORD_NO_OTP->{
                map["mobile"] = mobileNo
                map["dial_code"] = dialCode
            }
            ParseConstant.AuthType.EMAIL->{
                map["email"] = email
            }
        }
        return hashMapOf("user" to map)
    }

    private fun getLogoutRequestBody(uuid:String) = hashMapOf("uuid" to uuid)

    private fun getLoginRequestBody(uuid: String, mobile: String = Constant.EMPTY, email: String = Constant.EMPTY, password:String = Constant.EMPTY, countryId: String? = Constant.EMPTY):HashMap<String,Any?>{
        val map = hashMapOf<String,Any?>()
        when(AppController.appController.getAppConfig()?.authType){
            ParseConstant.AuthType.MOBILE_PASSWORD,
            ParseConstant.AuthType.MOBILE_PASSWORD_NO_OTP->{
                map["mobile"] = mobile
                map["password"] = password
                map["dial_code"] = countryId
            }
            ParseConstant.AuthType.EMAIL->{
                map["email"] = email
                map["password"] = password
            }
            ParseConstant.AuthType.MOBILE->{
                map["mobile"] = mobile
                map["dial_code"] = countryId
            }
        }
        map["uuid"] = uuid
        map["type"] = "customer"
        return hashMapOf("user" to map)
    }

    private fun getSignUpRequestBody(
        uuid: String,
        firstName: String,
        lastName: String,
        mobile: String = Constant.EMPTY,
        email: String = Constant.EMPTY,
        password:String = Constant.EMPTY,
        countryId: String? = Constant.EMPTY):HashMap<String,Any?>{
        val map = hashMapOf<String,Any?>()
            when(AppController.appController.getAppConfig()?.authType){
                ParseConstant.AuthType.MOBILE_PASSWORD,
                ParseConstant.AuthType.MOBILE_PASSWORD_NO_OTP->{
                    map["mobile"] = mobile
                    map["password"] = password
                    map["dial_code"] = countryId
                }
                ParseConstant.AuthType.EMAIL->{
                    map["email"] = email
                    map["password"] = password
                }
                ParseConstant.AuthType.MOBILE->{
                    map["mobile"] = mobile
                    map["dial_code"] = countryId
                }
            }
            map["uuid"] = uuid
            map["first_name"] = firstName
            map["last_name"] = lastName
            map["type"] = "customer"
            return hashMapOf("user" to map)
    }
    //endregion
}