package tradly.social.domain.dataSource

import tradly.social.domain.entities.*

interface ProductDataSource {

    fun getProducts(
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
    ): Result<List<Product>>

    suspend fun getProducts(queryParamMap:HashMap<String,Any?>):Result<List<Product>>

    fun getStoreProducts(storeId: String?,pagination: Int): Result<List<Product>>

    fun getSocialFeeds(type: String, page: Int): Result<List<Product>>

    fun addProduct(productId: String?,map: HashMap<String, Any?>,isForEdit:Boolean = false, variantList: List<Variant>): Result<Product>



    fun addVariants(productId:String,list: List<Variant>):Result<Boolean>

    fun updateProduct(productId:String,hashMap: HashMap<String,Any>): Result<Product>

    fun deleteProduct(productId: String):Result<Boolean>

    fun getProduct(productId: String?, locale: String): Result<Product>

    fun markAsSold(productId: String):Result<Boolean>

    fun getAttributes(categoryId: String, locale: String): Result<List<Attribute>>

    fun likeProduct(productId:String,isForLike:Boolean):Result<Boolean>

    fun getWishList(page: Int):Result<List<Product>>

    fun getSimilarProducts(productId: String,pagination: Int, perPage:Int):Result<List<Product>>

}