package tradly.social.domain.repository

import tradly.social.domain.dataSource.StoreDataSource
import tradly.social.domain.entities.*

class StoreRepository(private val storeDataSource: StoreDataSource) {
    suspend fun addStore(categoryId:String,storeName:String?,webAddress: String?,geoPoint: GeoPoint?,desc:String?,storeLinkName:String,storePic:String? , attributeValue: List<HashMap<String, Any?>>,selectedShipments:List<Int>) = storeDataSource.addStore(categoryId,storeName, webAddress,geoPoint, desc, storeLinkName,storePic,attributeValue,selectedShipments)
    suspend fun editStore(accountId:String,categoryId:String,storeName:String?,webAddress: String? ,geoPoint: GeoPoint?,desc:String?,storeLinkName:String,storePic:String? , attributeValue: List<HashMap<String, Any?>>,selectedShipments:List<Int>) = storeDataSource.editStore(accountId,categoryId,storeName, webAddress,geoPoint, desc,storeLinkName, storePic,attributeValue,selectedShipments)
    suspend fun getStores(pagination:Int=0,key:String =Constant.EMPTY,sort: String = Constant.EMPTY,searchKey:String,collectionId:String,latitude:Double = 0.0,longitude:Double=0.0) = storeDataSource.getStores(pagination, key,sort,searchKey,collectionId,latitude,longitude)
    suspend fun getStoreFeeds(type:String,pagination: Int) = storeDataSource.getStoreFeeds(type, pagination)
    suspend fun followStore(storeId:String?) = storeDataSource.followStore(storeId)
    suspend fun unfollowStore(storeId:String?) = storeDataSource.unfollowStore(storeId)
    suspend fun getStores(queryMap:HashMap<String,Any?>) = storeDataSource.getStores(queryMap)
    suspend fun getStore(storeId: String? = Constant.EMPTY) = storeDataSource.getStore(storeId)
    suspend fun getStoreType() = storeDataSource.getStoreType()
    suspend fun findStoreByUser(userId:String) = storeDataSource.findStoreByUser(userId)
    suspend fun syncUserStore(userId: String?) = storeDataSource.syncUserStore(userId)
    suspend fun blockStore(storeId: String) = storeDataSource.blockStore(storeId)
    suspend fun unblockStore(storeId: String) = storeDataSource.unBlockStore(storeId)
    suspend fun getAttribute(categoryId:String) = storeDataSource.getAttributes(categoryId)
    fun activateAccount(storeId: String,active:Boolean) = storeDataSource.accountActivate(storeId,active)
    fun getFollowers(accountId: String,page:Int) = storeDataSource.getFollowers(accountId, page)
}