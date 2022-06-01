package tradly.social.domain.usecases

import tradly.social.domain.repository.StoreRepository

class FollowStore(private val storeRepository: StoreRepository) {
    suspend fun followStore(storeId:String?) = storeRepository.followStore(storeId)
    suspend fun unfollowStore(storeId: String?) = storeRepository.unfollowStore(storeId)
}