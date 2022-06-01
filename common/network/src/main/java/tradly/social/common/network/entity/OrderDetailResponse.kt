package tradly.social.common.network.entity

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OrderDetailResponse(
    @field:Json(name = "status") val status: Boolean,
    @field:Json(name = "data") val responseData: ResponseData
)

@JsonClass(generateAdapter = true)
data class ResponseData(
    @field:Json(name = "order") val order: OrderEntity
)