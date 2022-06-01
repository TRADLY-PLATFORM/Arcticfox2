package tradly.social.common.network.feature.exploreEvent.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import tradly.social.common.network.entity.*
import tradly.social.domain.entities.Constant

@JsonClass(generateAdapter = true)
open class EventListResponse(
    @field:Json(name = "data") val dataListing:EventListData = EventListData()
): ResponseStatus()

@JsonClass(generateAdapter = true)
data class EventDetailResponse(
    @field:Json(name = "listing") val listing: EventDetailData
): ResponseStatus()

@JsonClass(generateAdapter = true)
class EventEntity(
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
    @field:Json(name = "shipping_charges") val shippingCharges: PriceEntity? = null,
    @field:Json(name = "negotiation") val negotiationEntity: NegotiationEntity?=null,
    @field:Json(name = "rating_data") val ratingData: RatingEntity?=null,
    @field:Json(name = "start_at") val startAt:Long = 0,
    @field:Json(name = "end_at")  val endAt:Long = 0
)

@JsonClass(generateAdapter = true)
data class EventListData(
    @field:Json(name = "listings") val productEntity:List<EventEntity> = listOf(),
    @field:Json(name = "page") val page:Int = 0
)

@JsonClass(generateAdapter = true)
data class EventDetailData(
    @field:Json(name = "listing") val productEntity:EventEntity
)