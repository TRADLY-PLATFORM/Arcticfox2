package tradly.social.common.network.entity

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NegotiationResponse(
    @field:Json(name = "data") val data:NegotiationData
):ResponseStatus()

@JsonClass(generateAdapter = true)
data class NegotiationData(
    @field:Json(name = "negotiation") val negotiation:NegotiationEntity
)

@JsonClass(generateAdapter = true)
data class NegotiationEntity(
    @field:Json(name = "id") val id:Int = 0,
    @field:Json(name = "price") val priceEntity: PriceEntity?=null,
    @field:Json(name = "expiry") val expiry:Long = 0,
    @field:Json(name = "created_at")val createdAt:Long = 0
)
