package tradly.social.common.network.entity

import tradly.social.domain.entities.Constant
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class HomeResponse(
    @field:Json(name = "data") val homeEntity: HomeEntity
) : ResponseStatus()

@JsonClass(generateAdapter = true)
data class HomeEntity(
    @field:Json(name = "categories") val categories: List<CategoryEntity>? = mutableListOf(),
    @field:Json(name = "promo_banners") val promoBanners: List<PromoEntity>? = mutableListOf(),
    @field:Json(name = "collections") val collections: List<CollectionEntity>? = mutableListOf(),
    @field:Json(name = "client_version") val clientVersion: ClientVersion = ClientVersion()
)

@JsonClass(generateAdapter = true)
data class PromoEntity(
    @field:Json(name = "id") val id: String? = Constant.EMPTY,
    @field:Json(name = "type") val type: String = Constant.EMPTY,
    @field:Json(name = "value") val value:String? = Constant.EMPTY,
    @field:Json(name = "image_path") val imagePath: String = Constant.EMPTY
)

@JsonClass(generateAdapter = true)
data class CollectionEntity(
    @field:Json(name = "id")val id:String = Constant.EMPTY,
    @field:Json(name = "scope_type") val scopeType: Int = 0,
    @field:Json(name = "view_type") val viewType: String = Constant.EMPTY,
    @field:Json(name = "background_color") val backgroundColor: String? = Constant.EMPTY,
    @field:Json(name = "background_url") val backgroundUrl: String? = Constant.EMPTY,
    @field:Json(name = "title") val title: String? = Constant.EMPTY,
    @field:Json(name = "image_path") val imagePath: String?=Constant.EMPTY,
    @field:Json(name = "description") val description: String? = Constant.EMPTY,
    @field:Json(name = "groups") val groups: List<GroupEntity>? = listOf(),
    @field:Json(name = "accounts") val stores:List<StoreEntity>?=  listOf(),
    @field:Json(name = "listings")val products:List<ProductEntity>? = listOf()
)

@JsonClass(generateAdapter = true)
data class GroupEntity(
    @field:Json(name = "group_name") val groupName: String = Constant.EMPTY,
    @field:Json(name = "members_count") val membersCount: Int? = 0,
    @field:Json(name = "group_pic") val groupPic: String? = Constant.EMPTY,
    @field:Json(name = "id") val id: String = Constant.EMPTY
)

@JsonClass(generateAdapter = true)
data class ClientVersion(
    @field:Json(name = "android") val version:Int? = 0,
    @field:Json(name = "ios") val iOSVersion:String? = Constant.EMPTY
)
