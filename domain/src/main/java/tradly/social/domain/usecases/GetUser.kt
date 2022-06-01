package tradly.social.domain.usecases

import tradly.social.domain.repository.UserAuthenticateRepository

class GetUser(val userAuthenticateRepository: UserAuthenticateRepository){
    suspend operator fun invoke(id:String) = userAuthenticateRepository.getUser(id)
}