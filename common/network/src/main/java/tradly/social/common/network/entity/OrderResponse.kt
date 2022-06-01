package tradly.social.common.network.entity

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OrderResponse(
    @field:Json(name ="data") val data : Data
):ResponseStatus()

@JsonClass(generateAdapter = true)
data class Data (
    @field:Json(name ="total_records") val total_records : Int,
    @field:Json(name ="page") val page : Int,
    @field:Json(name ="orders") val orders : List<OrderEntity>
)

@JsonClass(generateAdapter = true)
data class StatusHistory (
    @field:Json(name ="status") val status : Int,
    @field:Json(name ="remarks") val remarks : String,
    @field:Json(name ="created_at") val createdAt : Long
)

@JsonClass(generateAdapter = true)
data class OrderPaymentMethod (
    @field:Json(name ="name") val name : String,
    @field:Json(name ="logo_path") val logoPath : String,
    @field:Json(name ="type") val type : String
)

@JsonClass(generateAdapter = true)
data class OrderEntity (
    @field:Json(name ="id") val id : Int,
    @field:Json(name ="reference_number") val referenceNumber : Int,
    @field:Json(name = "grand_total") val grandTotal:PriceEntity,
    @field:Json(name ="list_total") val listTotal : PriceEntity,
    @field:Json(name ="offer_total") val offerTotal : PriceEntity,
    @field:Json(name ="tax_total") val taxTotal : PriceEntity,
    @field:Json(name ="shipping_total") val shippingTotal : PriceEntity,
    @field:Json(name ="payment_status") val paymentStatus : Int,
    @field:Json(name ="shipping_address") val shippingAddress : ShippingAddressEntity,
    @field:Json(name ="billing_address") val billingAddress : ShippingAddressEntity,
    @field:Json(name = "pickup_address") val pickupAddress:ShippingAddressEntity? = ShippingAddressEntity(),
    @field:Json(name ="order_status") val orderStatus : Int,
    @field:Json(name ="created_at") val createdAt : Long,
    @field:Json(name = "user") val user:UserEntity,
    @field:Json(name ="payment_method") val paymentMethod : OrderPaymentMethod,
    @field:Json(name = "shipping_method") val shippingMethod: ShippingMethodEntity? = ShippingMethodEntity(),
    @field:Json(name ="account") val account : StoreEntity,
    @field:Json(name ="status_history") val statusHistory : List<StatusHistory>,
    @field:Json(name ="order_details") val orderListing : List<OrderListingEntity>,
    @field:Json(name = "next_status") val nextStatus:List<Int> = listOf()
)

@JsonClass(generateAdapter = true)
data class OrderListingEntity (
    @field:Json(name ="quantity") val quantity : Int,
    @field:Json(name ="list_price") val listPrice : PriceEntity,
    @field:Json(name ="offer_price") val offerPrice : PriceEntity,
    @field:Json(name ="tax_charges") val taxCharges : PriceEntity,
    @field:Json(name ="shipping_charges") val shippingCharges : PriceEntity,
    @field:Json(name ="listing") val listing : ProductEntity
)