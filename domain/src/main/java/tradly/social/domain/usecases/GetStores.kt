package tradly.social.domain.usecases

import tradly.social.domain.entities.Constant
import tradly.social.domain.repository.StoreRepository

class GetStores(private val storeRepository: StoreRepository) {
    suspend fun getStores(queryMap:HashMap<String,Any?>) = storeRepository.getStores(queryMap)
    suspend fun getStores(pagination:Int=0,key:String = Constant.EMPTY , sort: String = Constant.EMPTY,searchKey:String = Constant.EMPTY,collectionId:String = Constant.EMPTY,latitude:Double = 0.0,longitude:Double=0.0) =  storeRepository.getStores(pagination, key,sort,searchKey,collectionId,latitude,longitude)
    suspend fun getStore(storeId: String? = Constant.EMPTY)=storeRepository.getStore(storeId)
    suspend fun getStoreType() = storeRepository.getStoreType()
    suspend fun findStoreByUser(userId:String) = storeRepository.findStoreByUser(userId)
    suspend fun syncUserStore(userId:String?) = storeRepository.syncUserStore(userId)
    suspend fun getStoreFeeds(type:String,pagination: Int) = storeRepository.getStoreFeeds(type, pagination)
    suspend fun activateAccount(storeId: String ,activate:Boolean) = storeRepository.activateAccount(storeId,activate)
}