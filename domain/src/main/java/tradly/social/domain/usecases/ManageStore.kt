package tradly.social.domain.usecases

import tradly.social.domain.repository.StoreRepository

class ManageStore (val storeRepository: StoreRepository){
    suspend fun blockStore(storeId:String) = storeRepository.blockStore(storeId)
    suspend fun unBlockStore(storeId:String) = storeRepository.unblockStore(storeId)
}