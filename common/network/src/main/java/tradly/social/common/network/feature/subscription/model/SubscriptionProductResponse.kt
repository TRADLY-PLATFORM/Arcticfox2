package tradly.social.common.network.feature.subscription.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import tradly.social.common.network.base.AppConstant
import tradly.social.common.network.entity.ResponseStatus

@JsonClass(generateAdapter = true)
data class SubscriptionProductResponse(
    @field:Json(name = "data") val data:SubscriptionProductData
):ResponseStatus()

@JsonClass(generateAdapter = true)
data class SubscriptionProductData(
@field:Json(name = "subscription_products") val subscriptionProducts:List<SubscriptionProductEntity> = listOf())

@JsonClass(generateAdapter = true)
data class SubscriptionProductEntity(
    @field:Json(name = "id") val id:Int = 0,
    @field:Json(name = "sku") val sku:String = AppConstant.EMPTY,
    @field:Json(name = "amount") val amount: Double = 0.0,
    @field:Json(name = "image_path") val imagePath:String = AppConstant.EMPTY,
    @field:Json(name = "allowed_listings") val allowedListing:Int = 0,
    @field:Json(name = "fee_fixed_per_order") val feeFixedPerOrder:Double = 0.0,
    @field:Json(name = "commission_percent_per_order") val commissionPercentPerOrder:Double = 0.0,
    @field:Json(name = "expiry_days") val expiryDays:Int = 0,
    @field:Json(name = "type") val type:String = AppConstant.EMPTY,
    @field:Json(name = "active") val active:Int = 0,
    @field:Json(name = "title") val title:String = AppConstant.EMPTY,
    @field:Json(name = "description") val description:String = AppConstant.EMPTY,
    @field:Json(name = "subscription_status") val subscriptionStatus:Boolean = false
)
