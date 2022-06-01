package tradly.social.domain.dataSource

import tradly.social.domain.entities.*

interface UserAuthenticateDataSource {

    fun updateUserDetail(userId:String,map: HashMap<String, Any?>): Result<User>

    fun getUser(id:String):Result<User>

    suspend fun login(uuid:String,mobile:String,email: String,password:String,countryId:String?):Result<User>

    fun initLogin(uuid:String,mobile:String,countryId:String):Result<Verification>

    suspend fun verifyAuthentication(verifyId: String?,code: String):Result<User>

    fun signUp(uuid: String,firstName:String,lastName:String,mobile: String,email: String,password: String,countryId: String?):Result<Verification>

    suspend fun signUpNoOtp(uuid: String,firstName:String,lastName:String,mobile: String,email: String,password: String,countryId: String?):Result<User>

    fun doPasswordRecover(email:String,dialCode:String,mobileNo:String):Result<Verification>

    fun doPasswordSet(password:String,verifyId:String,code:String):Result<Boolean>

    suspend fun socialLogin(token:String,provider:String,uuid: String):Result<User>

    fun logout(uuid:String):Result<Boolean>
}