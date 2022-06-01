package tradly.social.common.network.entity

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CartResponse(
    @field:Json(name = "data") val data:CartData
):ResponseStatus()

@JsonClass(generateAdapter = true)
data class CartData(
    @field:Json(name = "cart") val cartTotal:CartTotalEntity,
    @field:Json(name = "cart_details") val cartDetails:List<CartDetails> = listOf()

)
@JsonClass(generateAdapter = true)
data class CartTotalEntity(
    @field:Json(name = "id") val id:Int = 0,
    @field:Json(name = "grand_total") val grandTotal:PriceEntity = PriceEntity(),
    @field:Json(name = "offer_total") val offerTotal:PriceEntity = PriceEntity(),
    @field:Json(name = "shipping_total") val shippingCharges:PriceEntity?=null
)

@JsonClass(generateAdapter = true)
data class CartDetails(
    @field:Json(name = "id") val id:Int = 0,
    @field:Json(name = "quantity") val quantity:Int = 0,
    @field:Json(name = "quantity_total_price") val quantityTotalPrice:PriceEntity = PriceEntity(),
    @field:Json(name = "quantity_total_offer_price") val quantityTotalOfferPrice:PriceEntity = PriceEntity(),
    @field:Json(name = "tax_total_offer_price") val taxTotalOfferPrice:Int = 0,
    @field:Json(name = "listing") val listing:ProductEntity
)