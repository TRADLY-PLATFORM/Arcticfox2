package tradly.social.common.network.entity

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import tradly.social.domain.entities.Constant

@JsonClass(generateAdapter = true)
data class CheckoutResponse(
    @field:Json(name = "data") val checkoutData: CheckoutData
):ResponseStatus()

@JsonClass(generateAdapter = true)
data class CheckoutData(
    @field:Json(name = "order_reference") val orderReference:String = Constant.EMPTY
)