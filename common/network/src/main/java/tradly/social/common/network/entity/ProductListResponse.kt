package tradly.social.common.network.entity

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
open class ProductListResponse(
    @field:Json(name = "data") val dataListing:ProductListData = ProductListData()
):ResponseStatus()

@JsonClass(generateAdapter = true)
data class ProductListData(
    @field:Json(name = "listings") val productEntity:List<ProductEntity> = listOf(),
    @field:Json(name = "page") val page:Int = 0
)