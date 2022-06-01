package tradly.social.domain.dataSource

import tradly.social.domain.entities.*

interface StoreDataSource {

    fun addStore(categoryId:String,storeName:String?,webAddress: String? , geoPoint: GeoPoint?,desc:String?,storeLinkName:String,storePic:String?,attributeValue: List<HashMap<String, Any?>>,selectedShipments:List<Int>): Result<Store>

    fun editStore(accountId:String ,categoryId:String,storeName:String?,webAddress: String?, geoPoint: GeoPoint?,desc:String?,storeLinkName:String,storePic:String?,attributeValue: List<HashMap<String, Any?>>,selectedShipments:List<Int>): Result<Store>

    fun getStores(pagination: Int = 0, key: String = Constant.EMPTY,sort: String,searchKey:String,collectionId:String,latitude:Double,longitude:Double): Result<List<Store>>

    suspend fun getStores(queryMap:HashMap<String,Any?>):Result<List<Store>>

    fun getStoreFeeds(type:String,pagination: Int):Result<List<Store>>

    fun followStore(storeId: String?): Result<Boolean>

    fun unfollowStore(storeId: String?): Result<Boolean>

    fun getStore(storeId: String? = Constant.EMPTY):Result<Store>

    fun getStoreType():Result<List<StoreType>>

    fun getAttributes(categoryId: String):Result<List<Attribute>>

    fun findStoreByUser(userId:String):Result<Store>

    fun syncUserStore(userId: String?):Result<List<Store>>

    fun blockStore(storeId: String):Result<Boolean>

    fun unBlockStore(storeId: String):Result<Boolean>

    fun accountActivate(storeId: String, activate:Boolean):Result<Boolean>

    fun getFollowers(accountId: String,page:Int):Result<List<User>>
}