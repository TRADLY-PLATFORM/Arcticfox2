package tradly.social.common.network.entity

import tradly.social.domain.entities.Constant
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class StoreResponse(
    @field:Json(name = "data") val storeData: StoreData = StoreData()
) : ResponseStatus()

@JsonClass(generateAdapter = true)
data class StoreData(
    @field:Json(name = "account") val storeEntity: StoreEntity = StoreEntity(),
    @field:Json(name = "accounts") val storeEntityList: List<StoreEntity> = listOf(),
    @field:Json(name = "total_records") val totalRecords:Int = 0,
    @field:Json(name = "page") val page:Int = 0
)

@JsonClass(generateAdapter = true)
data class StoreEntity(
    @field:Json(name = "id") val id: String = Constant.EMPTY,
    @field:Json(name = "active") val active:Boolean = false,
    @field:Json(name = "name") val storeName: String = Constant.EMPTY,
    @field:Json(name = "user_pic") val userPic: String? = Constant.EMPTY,
    @field:Json(name = "images") val images:List<String> = listOf(),
    @field:Json(name = "description") val description: String? = Constant.EMPTY,
    @field:Json(name = "web_address") val webAddress: String? = Constant.EMPTY,
    @field:Json(name = "total_followers") val totalFollowers:Int = 0,
    @field:Json(name = "total_listings") val totalListing:Int = 0,
    @field:Json(name = "unique_name") val uniqueName:String = Constant.EMPTY,
    @field:Json(name = "location") val location:LocationEntity = LocationEntity(),
    @field:Json(name = "coordinates") val coordinates:GeoPointEntity? = GeoPointEntity(),
    @field:Json(name = "user") val user:UserEntity? = UserEntity(),
    @field:Json(name = "following")val following:Boolean = false,
    @field:Json(name = "category_id") val categoryIds:List<Int> = listOf(),
    @field:Json(name = "categories") val categories:List<CategoryEntity> = listOf(),
    @field:Json(name = "attributes") val attributes:List<AttributeEntity> = listOf(),
    @field:Json(name = "shipping_methods") val shippingMethods:List<ShippingMethodEntity> = listOf(),
    @field:Json(name = "status") val status:Int = 0,
    @field:Json(name = "subscription") val subscription: SubscriptionEntity?=null
)

@JsonClass(generateAdapter = true)
data class SubscriptionEntity(
    @field:Json(name = "allow_listing") val allowListing:Boolean?=false,
    @field:Json(name = "current_period_end") val subscriptionEnd:Long = 0,
    @field:Json(name = "created_at") val createdAt:Long = 0
)

