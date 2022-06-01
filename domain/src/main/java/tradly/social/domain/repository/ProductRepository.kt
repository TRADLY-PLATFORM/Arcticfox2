package tradly.social.domain.repository

import tradly.social.domain.dataSource.ProductDataSource
import tradly.social.domain.entities.Constant
import tradly.social.domain.entities.Variant

class ProductRepository(private val productDataSource: ProductDataSource) {

    fun addProduct(productId: String?,map: HashMap<String, Any?>,isForEdit:Boolean = false, variantList: List<Variant>) = productDataSource.addProduct(productId,map,isForEdit,variantList)

    fun updateProduct(productId:String,hashMap: HashMap<String,Any>) = productDataSource.updateProduct(productId,hashMap)

    fun deleteProduct(productId: String) = productDataSource.deleteProduct(productId)

    fun getProducts(
        categoryId: String? = Constant.EMPTY,
        sort: String? = Constant.EMPTY,
        priceRangeFrom: Int,
        priceRangeTo: Int,
        pagination: Int,
        key: String?,
        latitude:Double,
        longitude:Double,
        filterRadius:String,
        collectionId:String,
        allowLocation:Boolean
    ) = productDataSource.getProducts(
        categoryId,
        sort,
        priceRangeFrom,
        priceRangeTo,
        pagination,
        key,
        latitude,
        longitude,
        filterRadius,
        collectionId,
        allowLocation
    )

    fun markAsSold(productId: String) = productDataSource.markAsSold(productId)

    suspend fun getProducts(queryParamMap:HashMap<String,Any?>) = productDataSource.getProducts(queryParamMap)

    fun getStoreProducts(storeId:String? , pagination: Int) = productDataSource.getStoreProducts(storeId,pagination)

    fun getSocialFeeds(type: String, page: Int) = productDataSource.getSocialFeeds(type, page)

    fun getProduct(productId: String?, locale: String) =
        productDataSource.getProduct(productId, locale)

    fun getAttributes(categoryId: String, locale: String) =
        productDataSource.getAttributes(categoryId, locale)

    fun likeProduct(productId: String,isForLike:Boolean) = productDataSource.likeProduct(productId,isForLike)

    fun getWishList(page: Int) = productDataSource.getWishList(page)

    fun getSimilarProducts(productId: String,pagination: Int,perPage:Int) = productDataSource.getSimilarProducts(productId, pagination, perPage)
}