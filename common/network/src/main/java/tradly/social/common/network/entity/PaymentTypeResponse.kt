package tradly.social.common.network.entity

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PaymentTypeResponse(
    @Json(name = "data") val responseData:PaymentMethod
):ResponseStatus()

@JsonClass(generateAdapter = true)
data class PaymentMethod(
    @Json(name = "payment_methods") val paymentMethodList: List<PaymentMethodEntity>
)

@JsonClass(generateAdapter = true)
data class PaymentMethodEntity(
    @Json(name = "id") val id:Int,
    @Json(name = "name") val name:String,
    @Json(name = "logo_path") val logoPath:String,
    @Json(name = "type") val type:String,
    @Json(name = "channel") val channel:String,
    @Json(name = "default") val default:Boolean,
    @Json(name = "min_amount") val minAmount:Double
)