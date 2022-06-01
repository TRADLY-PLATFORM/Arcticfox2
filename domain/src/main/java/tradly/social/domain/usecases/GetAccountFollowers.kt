package tradly.social.domain.usecases

import tradly.social.domain.repository.StoreRepository

class GetAccountFollowers(private val storeRepository: StoreRepository) {
    fun execute(accountId:String,page:Int) = storeRepository.getFollowers(accountId, page)
}