package tradly.social.domain.usecases

import tradly.social.domain.repository.UserAuthenticateRepository

class ResetPassword(val userAuthenticateRepository: UserAuthenticateRepository){
    suspend fun doPasswordRecover(email:String,dialCode:String,mobileNo:String) = userAuthenticateRepository.doPasswordRecover(email, dialCode, mobileNo)
    suspend fun doPasswordSet(password:String,verifyId:String,code:String) = userAuthenticateRepository.doPasswordSet(password, verifyId, code)
}