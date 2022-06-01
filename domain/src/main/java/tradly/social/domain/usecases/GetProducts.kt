package tradly.social.domain.usecases

import tradly.social.domain.entities.Constant
import tradly.social.domain.repository.ProductRepository

class GetProducts(val productRepository: ProductRepository) {
    suspend fun getProducts(
        categoryId: String? = Constant.EMPTY,
        sort: String? = Constant.EMPTY,
        pagination: Int = 0,
        priceRangeFrom:Int = 0,
        priceRangeTo: Int = 0,
        key:String? = Constant.EMPTY,
        latitude:Double = 0.0,
        longitude:Double = 0.0,
        filterRadius:String = Constant.EMPTY,
        collectionId:String = Constant.EMPTY,
        allowLocation:Boolean = false
    ) = productRepository.getProducts(categoryId, sort, priceRangeFrom,priceRangeTo, pagination, key,latitude, longitude,filterRadius,collectionId,allowLocation)

    suspend fun getProducts(queryParamMap:HashMap<String,Any?>) = productRepository.getProducts(queryParamMap)

    suspend fun getStoreProducts(storeId:String? ,pagination: Int) = productRepository.getStoreProducts(storeId,pagination)

    suspend fun getSocialFeeds(type:String,page:Int) = productRepository.getSocialFeeds(type, page)

    suspend fun deleteProduct(productId:String) = productRepository.deleteProduct(productId)

    suspend fun markAsSold(productId: String) = productRepository.markAsSold(productId)

    suspend fun likeProduct(productId: String,isForLike:Boolean) = productRepository.likeProduct(productId, isForLike)

    suspend fun getWishList(page: Int) = productRepository.getWishList(page)

    fun getSimilarProducts(productId: String,pagination: Int,perPage:Int) = productRepository.getSimilarProducts(productId, pagination, perPage)
}