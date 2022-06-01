package tradly.social.domain.usecases

import tradly.social.domain.repository.UserAuthenticateRepository

class UserSignUp(private val userAuthenticateRepository: UserAuthenticateRepository) {
    suspend fun signUp(uuid: String, firstName:String, lastName:String, mobile: String, email: String, password: String, countryId: String?) = userAuthenticateRepository.signUp(uuid, firstName, lastName, mobile, email, password, countryId)
    suspend fun signUpNoOtp(uuid: String,firstName:String,lastName:String,mobile: String,email: String,password: String,countryId: String?) = userAuthenticateRepository.signUpNoOtp(uuid, firstName, lastName, mobile, email, password, countryId)
}