package tradly.social.common.network.feature.common.datasource

import tradly.social.common.network.NetworkConstant
import tradly.social.common.network.converters.ProductConverter
import tradly.social.common.network.entity.ProductDetailResponse
import tradly.social.common.network.NetworkError
import tradly.social.common.network.base.AppConstant
import tradly.social.common.network.base.BaseService
import tradly.social.common.network.retrofit.ProductAPI
import tradly.social.common.util.common.UtilConstant
import tradly.social.common.util.parser.extension.toObject
import tradly.social.domain.dataSource.ProductDataSource
import tradly.social.domain.entities.*

class ProductDataSourceImpl : ProductDataSource,BaseService() {

    private val apiService = getRetrofitService(ProductAPI::class.java)

    override fun addProduct(productId: String?,map: HashMap<String, Any?>,isForEdit:Boolean, variantList: List<Variant>): Result<Product> =
        when (val result = apiCall(if (isForEdit) apiService.editProduct(map,productId) else apiService.addProduct(map))) {
            is Result.Success ->{
                if(variantList.isEmpty()){
                    Result.Success(ProductConverter.mapFrom(result.data.productData.listing))
                }
                else{
                    val product = ProductConverter.mapFrom(result.data.productData.listing)
                    if (variantList.any{ it.id.startsWith(UtilConstant.NEW_ID)}){
                        when(val res = addVariants(product.id,variantList.filter { it.id.startsWith(UtilConstant.NEW_ID) })){
                            is Result.Success -> Result.Success(product)
                            is Result.Error -> res
                        }
                    }
                    else{
                        Result.Success(product)
                    }
                }
            }
            is Result.Error -> result
        }

    override suspend fun getProducts(queryParamMap: HashMap<String, Any?>): Result<List<Product>> {
        return when(val result = apiCall(apiService.getProducts(queryParamMap))){
            is Result.Success -> {
                if (result.data.status){
                    val productList = ProductConverter.mapFromList(result.data.dataListing.productEntity)
                    Result.Success(productList)
                }
                else{
                    Result.Error(AppError(errorType = NetworkError.API_UNKNOWN_ERROR))
                }
            }
            is Result.Error -> result
        }
    }

    override fun addVariants(
        productId: String,
        list: List<Variant>
    ):Result<Boolean> {
        return when(val result = apiCall(apiService.addVariants(getVariantsQueryParams(list),productId))){
            is Result.Success->{
                if (result.data.status){
                    Result.Success(result.data.status)
                }
                else{
                    Result.Error(AppError(errorType = NetworkError.API_UNKNOWN_ERROR))
                }
            }
            is Result.Error -> result
        }
    }


    override fun updateProduct(productId: String, hashMap: HashMap<String, Any>): Result<Product> =
        when(val result = apiCall(apiService.updateProduct(productId,hashMap))){
            is Result.Success -> Result.Success(ProductConverter.mapFrom(result.data.productData.listing))
            is Result.Error -> result
        }

    override fun deleteProduct(productId: String): Result<Boolean> =
        when(val result = apiCall(apiService.deleteProduct(productId))){
            is Result.Success-> Result.Success(result.data.status)
            is Result.Error->result
    }


    override fun getStoreProducts(storeId: String?,pagination: Int): Result<List<Product>> =
        when(val result = apiCall(apiService.getProducts(getQueryParam(pagination = pagination,storeId = storeId)))){
            is Result.Success -> {
                val productList = ProductConverter.mapFromList(result.data.dataListing.productEntity)
                Result.Success(productList)
            }
            is Result.Error -> result
        }

    override fun getProducts(
        categoryId: String?,
        sort: String?,
        priceRangeFrom: Int,
        priceRangeTo: Int,
        pagination: Int,
        key: String?,
        latitude:Double,
        longitude:Double,
        filterRadius:String,
        collectionId:String,
        allowLocation:Boolean
    ): Result<List<Product>> =
        when (val result = apiCall(apiService.getProducts(
            getQueryParam(
                categoryId,
                sort,
                priceRangeFrom,
                priceRangeTo,
                pagination,
                key,
                lat = latitude,
                lng = longitude,
                filterRadius = filterRadius,
                collectionId = collectionId,
                allowLocation = allowLocation
            )
        ))) {
            is Result.Success -> { val productList = ProductConverter.mapFromList(result.data.dataListing.productEntity)
                Result.Success(productList)
            }
            is Result.Error -> result
        }

    override fun getSimilarProducts(productId: String, pagination: Int, perPage: Int):Result<List<Product>> {
       return when(val result = apiCall(apiService.getSimilarProducts(productId,pagination,perPage))){
            is Result.Success->{
                if (result.data.status){
                    Result.Success(ProductConverter.mapFromList(result.data.dataListing.productEntity))
                }
                else{
                    Result.Error(AppError(errorType = NetworkError.API_UNKNOWN_ERROR))
                }
            }
            is Result.Error->result
        }
    }

    override fun getProduct(productId: String?, locale: String): Result<Product> {
       return when (val result = apiCall(apiService.getProduct(productId, locale))) {
            is Result.Success -> {
                val responseString = result.data.string()
                val productDetailResponse = responseString.toObject<ProductDetailResponse>()
                Result.Success(ProductConverter.mapFrom(productDetailResponse?.productData?.listing,responseString))
            }
        is Result.Error -> result
    }
}

    override fun markAsSold(productId: String): Result<Boolean> =
        when(val result = updateProduct(productId,getAddUpdateRequestData( hashMapOf("stock" to 0)))){
            is Result.Success-> Result.Success(true)
            is Result.Error->result
        }

    override fun getAttributes(categoryId: String,locale:String) : Result<List<Attribute>> =
        when(val result = apiCall(apiService.getAttribute(getAttributeQueryParam(categoryId)))){
            is Result.Success->Result.Success(ProductConverter.mapFrom(result.data.string()))
            is Result.Error->result
        }

    override fun getSocialFeeds(type: String, page: Int): Result<List<Product>> {
        val map = hashMapOf<String,Any?>()
        map["type"] = type
        map["page"] = page
        map["page_per"] = AppConstant.LIST_PER_PAGE
        return when(val result = apiCall(apiService.getSocialFeeds(map))){
            is Result.Success->Result.Success(ProductConverter.mapFromList(result.data.dataListing.productEntity))
            is Result.Error->result
        }
    }

    override fun getWishList(page: Int): Result<List<Product>> {
        return when(val result = apiCall(apiService.getWishList(getQueryParam(pagination = page)))){
            is Result.Success -> {
                val productList = ProductConverter.mapFromList(result.data.dataListing.productEntity)
                Result.Success(productList)
            }
            is Result.Error -> result
        }
    }

    override fun likeProduct(productId: String, isForLike: Boolean):Result<Boolean> {
        return if(isForLike){
            when(val result = apiCall(apiService.likeProduct(productId))){
                is Result.Success->Result.Success(result.data.status)
                is Result.Error->result
            }
        } else{
            when(val result = apiCall(apiService.disLikeProduct(productId))){
                is Result.Success->Result.Success(result.data.status)
                is Result.Error->result
            }
        }
    }

    private fun getVariantsQueryParams(variantList:List<Variant>):HashMap<String,Any?>{
        val variantMapList = mutableListOf<HashMap<String,Any?>>()
        variantList.forEach { variant->
              variantMapList.add(getVariantQueryMap(variant))
        }
        val variantMap = hashMapOf<String,Any?>()
        variantMap["variants"] = variantMapList
        return variantMap
    }

    private fun getVariantQueryMap(variant: Variant):HashMap<String,Any?>{
        val queryMap = hashMapOf<String,Any?>()
        queryMap["active"] = true
        queryMap["stock"] = variant.quantity
        queryMap["images"] = variant.images
        queryMap["title"] = variant.variantName
        queryMap["description"] = variant.variantDescription
        queryMap["list_price"] = variant.listPrice.amount
        queryMap["offer_percent"] = variant.offerPercent
        queryMap["variant_values"] = variant.values.map{ hashMapOf<String,Any?>("variant_type_id" to it.variantTypeId,"variant_type_value_id" to it.variantValueId) }
        return queryMap
    }

    private fun getQueryParam(
        categoryId: String? = Constant.EMPTY,
        sort: String? = Constant.EMPTY,
        priceRangeFrom: Int = 0,
        priceRangeTo: Int = 0,
        pagination: Int = 0,
        key: String? = Constant.EMPTY,
        storeId:String? = Constant.EMPTY,
        lat:Double = 0.0,
        lng:Double = 0.0,
        filterRadius:String = Constant.EMPTY,
        collectionId:String = Constant.EMPTY,
        allowLocation:Boolean = false
    ):Map<String,Any?>{
        val queryMap = hashMapOf<String,Any?>()
        queryMap["page"] = pagination
        if(!storeId.isNullOrEmpty()){
            queryMap["account_id"] = storeId
            return queryMap
        }
        if(priceRangeFrom != 0){queryMap["price_from"] = priceRangeFrom}
        queryMap["per_page"] = AppConstant.LIST_PER_PAGE
        if(!categoryId.isNullOrEmpty()){queryMap["category_id"] =  categoryId}
        if(priceRangeTo != 0){queryMap["price_to"] = priceRangeTo}
        if(!key.isNullOrEmpty()){queryMap["search_key"] = key}
        if(!sort.isNullOrEmpty()){queryMap["sort"] = sort}
        if(collectionId.isNotEmpty())queryMap["collection_id"] = collectionId
        if(allowLocation && lat!=0.0){
            queryMap["latitude"] = lat
            queryMap["longitude"] = lng
        }
        else if(lat != 0.0 && filterRadius.isNotEmpty()){
            queryMap["latitude"] = lat
            queryMap["longitude"] = lng
            queryMap["max_distance"] = filterRadius.toFloat()
        }
        return queryMap
    }

    private fun getAddUpdateRequestData(hashMap: HashMap<String, Any>): HashMap<String,Any>{
        val map = hashMapOf<String,Any>()
        map["listing"] = hashMap
        return map
    }

    private fun getAttributeQueryParam(categoryId: String) = hashMapOf<String,Any?>("category_id" to categoryId , "type" to NetworkConstant.Param.LISTINGS)
}