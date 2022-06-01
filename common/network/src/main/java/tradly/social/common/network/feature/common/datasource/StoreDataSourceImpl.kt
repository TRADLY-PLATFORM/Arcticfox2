package tradly.social.common.network.feature.common.datasource

import tradly.social.common.cache.AppCache
import tradly.social.common.network.converters.StoreModelConverter
import tradly.social.common.network.converters.StoreTypeConverter
import tradly.social.common.network.CustomError
import tradly.social.common.network.NetworkConstant
import tradly.social.common.network.parseHelper.ParseManager
import tradly.social.domain.dataSource.StoreDataSource
import tradly.social.domain.entities.*
import tradly.social.common.network.converters.ProductConverter
import tradly.social.common.network.NetworkError
import tradly.social.common.network.base.BaseService
import tradly.social.common.network.converters.UserModelConverter
import tradly.social.common.network.retrofit.ProductAPI
import tradly.social.common.network.retrofit.StoreAPI

class StoreDataSourceImpl : StoreDataSource,BaseService() {

    val productApiService = getRetrofitService(ProductAPI::class.java)
    val storeApiService = getRetrofitService(StoreAPI::class.java)

    override fun addStore(categoryId:String,storeName:String?,webAddress: String?,geoPoint: GeoPoint?,desc:String?,storeLinkName:String,storePic:String?,attributeValue: List<HashMap<String, Any?>>,selectedShipments:List<Int>): Result<Store> {
        return when (val result = apiCall(storeApiService.addStore(getRequestBody(categoryId,storeName, webAddress, geoPoint,desc,storeLinkName,storePic,attributeValue,selectedShipments)))) {
            is Result.Success -> Result.Success(StoreModelConverter.mapFrom(result.data.storeData.storeEntity))
            is Result.Error -> result
        }
    }

    override fun editStore(
        accountId: String,
        categoryId: String,
        storeName: String?,
        webAddress: String?,
        geoPoint: GeoPoint?,
        desc: String?,
        storeLinkName:String,
        storePic: String?,
        attributeValue: List<HashMap<String, Any?>>,
        selectedShipments:List<Int>
    ): Result<Store> {
        return when (val result = apiCall(storeApiService.editStore(accountId,getRequestBody(categoryId,storeName, webAddress, geoPoint,desc,storeLinkName, storePic,attributeValue,selectedShipments)))) {
            is Result.Success -> Result.Success(StoreModelConverter.mapFrom(result.data.storeData.storeEntity))
            is Result.Error -> result
        }
    }

    override fun accountActivate(storeId: String, activate: Boolean) =
        when(val result = apiCall(storeApiService.accountActivation(storeId,getAccountActivateRequestBody(activate)))){
            is Result.Success->{
                if(result.data.status){
                    Result.Success(true)
                }
                else{
                    Result.Error(AppError(code = if(activate) CustomError.ACCOUNT_ACTIVATION else CustomError.ACCOUNT_DE_ACTIVATION))
                }
            }
            is Result.Error->result
        }

    override fun getStoreFeeds(type: String, pagination: Int):Result<List<Store>> =
        when(val result = apiCall(storeApiService.getStoreFeeds(type,pagination,NetworkConstant.Param.ACCOUNT))){
            is Result.Success->{
                if(result.data.status){
                    Result.Success(StoreModelConverter.mapFrom(result.data.storeData.storeEntityList))
                }
                else{
                    Result.Error(AppError(errorType = NetworkError.API_UNKNOWN_ERROR))
                }
            }
            is Result.Error->result
        }

    override fun getStores(
        pagination: Int,
        key: String,
        sort: String,
        searchKey: String,
        collectionId: String,
        latitude: Double,
        longitude: Double
    ): Result<List<Store>> {
        return when(val result = apiCall(storeApiService.syncUserStore(getStoresQueryParam(pagination,
            NetworkConstant.Param.ACCOUNT,key,sort,searchKey,collectionId,latitude,longitude)))){
            is Result.Success-> Result.Success(StoreModelConverter.mapFrom(result.data.storeData.storeEntityList))
            is Result.Error->result
        }
    }

    override suspend fun getStores(queryMap: HashMap<String, Any?>): Result<List<Store>> {
        return when(val result = apiCall(storeApiService.syncUserStore(queryMap))){
            is Result.Success->{
                if (result.data.status){
                    Result.Success(StoreModelConverter.mapFrom(result.data.storeData.storeEntityList))
                }
                else{
                    Result.Error(AppError(errorType = NetworkError.API_UNKNOWN_ERROR))
                }
            }
            is Result.Error->result
        }
    }


    override fun followStore(storeId: String?): Result<Boolean> =
        when(val result = apiCall(storeApiService.followStore(storeId))){
            is Result.Success->Result.Success(result.data.status)
            is Result.Error->result
        }

    override fun unfollowStore(storeId: String?): Result<Boolean> =
        when(val result = apiCall(storeApiService.unFollowStore(storeId))){
            is Result.Success->Result.Success(result.data.status)
            is Result.Error->result
        }

    override fun getStore(storeId: String?) :Result<Store> {
        if (!storeId.isNullOrEmpty()) {
            return when (val result = apiCall(storeApiService.getStore(storeId))) {
                is Result.Success -> {
                    Result.Success(StoreModelConverter.mapFrom(result.data.storeData.storeEntity))
                }
                is Result.Error -> result
            }
        }
        return Result.Error(AppError("getStore storeId is emptyOrNull", CustomError.CODE_TECH_ISSUES))
    }

    override fun getAttributes(categoryId: String): Result<List<Attribute>> =
        when(val result = apiCall(productApiService.getAttribute(getAttributeQueryParam(categoryId, NetworkConstant.Param.ACCOUNT)))){
            is Result.Success-> Result.Success(ProductConverter.mapFrom(result.data.string()))
            is Result.Error->result
        }

    override fun blockStore(storeId: String): Result<Boolean> =
        when(val result = apiCall(storeApiService.blockStore(storeId))){
            is Result.Success->Result.Success(result.data.status)
            is Result.Error->result
        }

    override fun unBlockStore(storeId: String): Result<Boolean> =
        when(val result = apiCall(storeApiService.unBlockStore(storeId))){
            is Result.Success->Result.Success(result.data.status)
            is Result.Error->result
        }


    override fun getStoreType(): Result<List<StoreType>> =
        when(val result = ParseManager.getInstance().getStoreType()){
            is Result.Success->{
                Result.Success(StoreTypeConverter().mapFrom(result.data))
            }
            is Result.Error->{result}
        }

    override fun findStoreByUser(userId: String): Result<Store> =
        when(val result = ParseManager.getInstance().findStoreByUser(userId)){
            is Result.Success->{Result.Success(Store())}
            is Result.Error->result
        }


    override fun syncUserStore(userId: String?): Result<List<Store>> {
        if(!userId.isNullOrEmpty()){
            val queryMap = hashMapOf<String,Any?>( "user_id" to userId , "page" to 1 , "type" to NetworkConstant.Param.ACCOUNT)
            return when (val result = apiCall(storeApiService.syncUserStore(queryMap))) {
                is Result.Success -> {
                    val store = StoreModelConverter.mapFrom(result.data.storeData.storeEntityList)
                    AppCache.setUserStoreList(store)
                    Result.Success(store)
                }
                is Result.Error -> result
            }
        }
        return Result.Error(AppError("syncUserStore userId is empty", CustomError.CODE_TECH_ISSUES))
    }

    override fun getFollowers(accountId: String, page: Int): Result<List<User>> {
        return when(val result = apiCall(storeApiService.getAccountFollowers(accountId, page))){
            is Result.Success->{
                if (result.data.status){
                    Result.Success(UserModelConverter().mapFrom(result.data.followersData.users))
                }
                else{
                    Result.Error(AppError(errorType = NetworkError.API_UNKNOWN_ERROR))
                }
            }
            is Result.Error->result
        }
    }

    private fun getRequestBody(categoryId:String,storeName:String?,webAddress: String?,geoPoint: GeoPoint?,desc:String?,storeLinkName:String,storePic:String?,attributeValue: List<HashMap<String, Any?>>,selectedShipments:List<Int>):HashMap<String,Any?>{
        val body = hashMapOf<String,Any?>()
        val storeMap = hashMapOf<String,Any?>(
            "type" to NetworkConstant.Param.ACCOUNT,
            "name" to storeName,
            "images" to listOf(storePic)
        )
        if(!webAddress.isNullOrEmpty()){ storeMap["web_address"] = webAddress }
        if(geoPoint != null){
            val geoMap = hashMapOf(
                "latitude" to geoPoint.latitude,
                "longitude" to geoPoint.longitude)
            storeMap["coordinates"] = geoMap
        }
        if(selectedShipments.isNotEmpty()){
            storeMap["shipping_methods"] = selectedShipments
        }
        if(!desc.isNullOrEmpty()){ storeMap["description"] = desc }
        if(categoryId.isNotEmpty()){ storeMap["category_id"] = listOf(categoryId) }
        if(attributeValue.isNotEmpty()){ storeMap["attributes"] = attributeValue }
        if(storeLinkName.isNotEmpty()){storeMap["unique_name"] = storeLinkName.toLowerCase()}
        body["account"] = storeMap
        return body
    }

    private fun getAttributeQueryParam(categoryId: String,type:String):HashMap<String,Any?>{
        val map = hashMapOf<String,Any?>()
        if(categoryId.isNotEmpty()){
            map["category_id"] = categoryId
        }
        map["type"] = type
        return map
    }

    private fun getAccountActivateRequestBody(activate: Boolean) = hashMapOf<String,Any?>(
        NetworkConstant.QueryParam.ACCOUNT to hashMapOf<String,Any?>(NetworkConstant.QueryParam.ACTIVATE to activate))

    private fun getReviewParam(storeId: String, pagination: Int) =
        hashMapOf(
            "id" to storeId,
            "type" to NetworkConstant.Param.LISTINGS,
            "page" to pagination
        )

    private fun getStoresQueryParam(pagination: Int,type: String,key: String,sort: String,searchKey:String,collectionId:String,latitude: Double,longitude: Double):HashMap<String,Any?>{
        val map = hashMapOf<String,Any?>()
        map["page"] = pagination
        map["type"] = type
        if(key.isNotEmpty()){
            map["unique_name"] = key
        }
        if(sort.isNotEmpty()) map["sort"] = sort
        if(searchKey.isNotEmpty()) map["search_key"] = searchKey
        if(collectionId.isNotEmpty())map["collection_id"] = collectionId
        if(latitude!=0.0){
            map["latitude"] = latitude
            map["longitude"] = longitude
        }
        return map
    }

}