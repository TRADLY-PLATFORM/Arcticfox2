package tradly.social.domain.usecases

import tradly.social.domain.repository.UserAuthenticateRepository

class LogoutUser(val userAuthenticateRepository: UserAuthenticateRepository) {
    suspend fun logoutUser(uuid:String) = userAuthenticateRepository.logout(uuid)
}