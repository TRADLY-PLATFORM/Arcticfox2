package tradly.social.domain.repository

import tradly.social.domain.dataSource.UserAuthenticateDataSource

class UserAuthenticateRepository(private val userAuthenticateDataSource: UserAuthenticateDataSource) {

    suspend fun updateUserDetail(userId:String,map:HashMap<String,Any?>) = userAuthenticateDataSource.updateUserDetail(userId,map)
    suspend fun getUser(id:String) = userAuthenticateDataSource.getUser(id)
    suspend fun initLogin(uuid:String,mobile:String,countryId:String) = userAuthenticateDataSource.initLogin(uuid, mobile, countryId)
    suspend fun verifyAuthentication(verifyId: String?,code: String) = userAuthenticateDataSource.verifyAuthentication(verifyId, code)
    suspend fun login(uuid:String,mobile:String,email: String,password:String,countryId:String?) = userAuthenticateDataSource.login(uuid, mobile, email, password, countryId)
    suspend fun signUp(uuid: String,firstName:String,lastName:String,mobile: String,email: String,password: String,countryId: String?) = userAuthenticateDataSource.signUp(uuid, firstName, lastName, mobile, email, password, countryId)
    suspend fun signUpNoOtp(uuid: String,firstName:String,lastName:String,mobile: String,email: String,password: String,countryId: String?) = userAuthenticateDataSource.signUpNoOtp(uuid, firstName, lastName, mobile, email, password, countryId)
    suspend fun doPasswordRecover(email:String,dialCode:String,mobileNo:String) = userAuthenticateDataSource.doPasswordRecover(email, dialCode, mobileNo)
    suspend fun doPasswordSet(password:String,verifyId:String,code:String) = userAuthenticateDataSource.doPasswordSet(password, verifyId, code)
    suspend fun socialLogin(token:String,provider:String,uuid: String) = userAuthenticateDataSource.socialLogin(token, provider,uuid)
    suspend fun logout(uuid:String) = userAuthenticateDataSource.logout(uuid)
}