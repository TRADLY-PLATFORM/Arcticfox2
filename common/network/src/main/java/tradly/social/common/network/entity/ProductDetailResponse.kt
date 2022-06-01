package tradly.social.common.network.entity

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import tradly.social.domain.entities.Constant

@JsonClass(generateAdapter = true)
open class ProductDetailResponse(
    @field:Json(name = "data") val productData: ProductData = ProductData()
) : ResponseStatus()

@JsonClass(generateAdapter = true)
data class ProductData(
    @field:Json(name = "listing") val listing: ProductEntity = ProductEntity()
)

@JsonClass(generateAdapter = true)
open class ProductEntity(
    @field:Json(name = "id") val id: Int = 0,
    @field:Json(name = "title") val title: String = Constant.EMPTY,
    @field:Json(name = "offer_price") val offerPrice: PriceEntity = PriceEntity(),
    @field:Json(name = "offer_percent") val offerPercent: Int = 0,
    @field:Json(name = "list_price") val listPrice: PriceEntity = PriceEntity(),
    @field:Json(name = "description") val description: String = Constant.EMPTY,
    @field:Json(name = "sold") val isSold: Boolean = false,
    @field:Json(name = "stock") val stock:Int = 0,
    @field:Json(name = "status") val status: Int = 0,
    @field:Json(name = "currency_id") val currencyId: Int = 0,
    @field:Json(name = "category_id") val categoryId: List<Int> = listOf(),
    @field:Json(name = "created_at") val createdAt: Long = 0,
    @field:Json(name = "in_cart") val isInCart: Boolean = false,
    @field:Json(name = "account") val store: StoreEntity? = null,
    @field:Json(name = "liked") val liked: Boolean = false,
    @field:Json(name = "images") val images: List<String> = listOf(),
    @field:Json(name = "coordinates") val coordinates: GeoPointEntity? = GeoPointEntity(),
    @field:Json(name = "categories") val categories:List<CategoryEntity> = listOf(),
    @field:Json(name = "location") val address: LocationEntity = LocationEntity(),
    @field:Json(name = "attributes") val attributes: List<AttributeEntity> = listOf(),
    @field:Json(name = "tags") val tags: List<TagEntity> = listOf(),
    @field:Json(name = "active") val active: Boolean = false,
    @field:Json(name = "variants") val variantList: List<VariantEntity> = listOf(),
    @field:Json(name = "max_quantity") val maxQuantity:Int = 0,
    @field:Json(name = "review_status") val reviewStatus:Boolean = false,
    @field:Json(name = "shipping_charges") val shippingCharges:PriceEntity? = null,
    @field:Json(name = "negotiation") val negotiationEntity:NegotiationEntity?=null,
    @field:Json(name = "rating_data") val ratingData:RatingEntity?=null,
    @field:Json(name = "likes") val likes:Int = 0,
    @field:Json(name = "start_at") val startAt:Long? = 0,
    @field:Json(name = "end_at")  val endAt:Long? = 0
)

@JsonClass(generateAdapter = true)
data class PriceEntity(
    @field:Json(name = "amount") val amount: Double = 0.0,
    @field:Json(name = "currency") val currency: String = Constant.EMPTY,
    @field:Json(name = "formatted") val displayCurrency: String = Constant.EMPTY
)

@JsonClass(generateAdapter = true)
data class VariantEntity(
    @field:Json(name = "id") val id: Int = 0,
    @field:Json(name = "title") val title:String = Constant.EMPTY,
    @field:Json(name = "description") val description: String = Constant.EMPTY,
    @field:Json(name = "stock") val stock:Int = 0,
    @field:Json(name = "list_price") val listPrice: PriceEntity = PriceEntity(),
    @field:Json(name = "offer_percent") val offerPercent: Int = 0,
    @field:Json(name = "images") val images: List<String> = listOf(),
    @field:Json(name = "offer_price") val offerPrice: PriceEntity = PriceEntity(),
    @field:Json(name = "active") val active: Boolean = false,
    @field:Json(name = "stock_enabled") val stockEnabled: Boolean = false,
    @field:Json(name = "created_at") val createdAt: Long = 0,
    @field:Json(name = "updated_at") val updatedAt: Long = 0,
    @field:Json(name = "values") val valueList: List<VariantValueEntity> = listOf(),
    @field:Json(name = "variant_values") val variantValues: List<VariantValueEntity> = listOf()
)

@JsonClass(generateAdapter = true)
data class VariantValueEntity(
    @field:Json(name = "id") val id: Int = 0,
    @field:Json(name = "active") val active: Boolean = false,
    @field:Json(name = "variant_type") val variantTypeEntity: VariantTypeEntity,
    @field:Json(name = "variant_type_value") val variantTypeValueEntity: VariantTypeEntity,
    @field:Json(name = "value") val value: String = Constant.EMPTY
)

@JsonClass(generateAdapter = true)
data class ReviewListResponse(
    @field:Json(name = "data") val data: ReviewData
) : ResponseStatus()

@JsonClass(generateAdapter = true)
data class ReviewData(
    @field:Json(name = "total_records") val totalRecords: Int=0,
    @field:Json(name = "page") val page:Int=0,
    @field:Json(name = "reviews") val reviews:List<ReviewEntity> = emptyList(),
    @field:Json(name = "rating_data") val ratingData:RatingEntity?=null,
    @field:Json(name = "my_review") val myReview:ReviewEntity?=null
)

@JsonClass(generateAdapter = true)
data class ReviewEntity(
    @field:Json(name = "id") val id:Int,
    @field:Json(name = "title") val title:String,
    @field:Json(name = "content") val content:String,
    @field:Json(name = "rating") val rating:Int,
    @field:Json(name = "created_at") val createdAt:Long,
    @field:Json(name = "user") val user:UserEntity,
    @field:Json(name = "like_status") val likeStatus:Int,
    @field:Json(name = "images") val images:List<String>
)

@JsonClass(generateAdapter = true)
data class RatingEntity(
    @field:Json(name = "rating_average") val ratingAverage:Double=0.0,
    @field:Json(name = "rating_count") val ratingCount:Int=0,
    @field:Json(name = "review_count") val reviewCount:Int=0,
    @field:Json(name = "rating_count_data") val ratingCountData:RatingCountData?=null
)

@JsonClass(generateAdapter = true)
data class RatingCountData(
    @field:Json(name = "rating_1") val rating1:Int,
    @field:Json(name = "rating_2") val rating2:Int,
    @field:Json(name = "rating_3") val rating3:Int,
    @field:Json(name = "rating_4") val rating4:Int,
    @field:Json(name = "rating_5") val rating5:Int
)



