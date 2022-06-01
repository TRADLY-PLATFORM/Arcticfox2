package tradly.social.domain.usecases

import tradly.social.domain.repository.UserAuthenticateRepository
import java.util.HashMap

class UpdateUserDetail(val userAuthenticateRepository: UserAuthenticateRepository) {
    suspend operator fun invoke(userId:String,map: HashMap<String,Any?>)= userAuthenticateRepository.updateUserDetail(userId ,map)
}