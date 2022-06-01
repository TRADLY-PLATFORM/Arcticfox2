package tradly.social.domain.usecases

import tradly.social.domain.entities.Constant
import tradly.social.domain.repository.UserAuthenticateRepository

class GetUserDetail(private val userAuthenticateRepository: UserAuthenticateRepository) {

    suspend fun initLogin(uuid:String,mobile:String,countryId:String) = userAuthenticateRepository.initLogin(uuid, mobile, countryId)

    suspend fun login(uuid:String,mobile:String = Constant.EMPTY,email: String = Constant.EMPTY,password:String = Constant.EMPTY,countryId:String? = Constant.EMPTY) = userAuthenticateRepository.login(uuid, mobile, email, password, countryId)

    suspend fun verifyAuthentication(verifyId: String?,code: String) = userAuthenticateRepository.verifyAuthentication(verifyId, code)

    suspend fun doPasswordRecover(email:String = Constant.EMPTY,dialCode:String = Constant.EMPTY,mobileNo:String = Constant.EMPTY) = userAuthenticateRepository.doPasswordRecover(email, dialCode, mobileNo)

    suspend fun passwordReset(password:String,verifyId:String,code:String) = userAuthenticateRepository.doPasswordSet(password, verifyId, code)

    suspend fun socialLogin(token:String,provider:String,uuid: String) = userAuthenticateRepository.socialLogin(token, provider,uuid)
}