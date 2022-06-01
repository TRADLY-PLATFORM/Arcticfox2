package tradly.social.common.network.entity

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import tradly.social.domain.entities.Constant

data class ShippingAddressResponse(
    @Json(name = "data") val data:ShippingAddressData = ShippingAddressData()
)

@JsonClass(generateAdapter = true)
data class ShippingAddressData(
    @Json(name = "addresses") val addresses:List<ShippingAddressEntity> = listOf(),
    @Json(name = "address") var address:ShippingAddressEntity = ShippingAddressEntity()
)

@JsonClass(generateAdapter = true)
data class ShippingAddressEntity(
    @Json(name = "id") val id: Int = 0,
    @Json(name = "name") val name:String? = Constant.EMPTY,
    @Json(name = "phone_number") val phoneNumber:String? = Constant.EMPTY,
    @Json(name = "address_line_1") val lane1:String? = Constant.EMPTY,
    @Json(name = "address_line_2") val lane2:String? = Constant.EMPTY,
    @Json(name = "post_code") val postCode:String? = Constant.EMPTY,
    @Json(name = "landmark") val landMark:String? = Constant.EMPTY,
    @Json(name = "state") val state:String? = Constant.EMPTY,
    @Json(name = "country") val country:String? = Constant.EMPTY,
    @Json(name = "formatted_address") val formattedAddress:String? = Constant.EMPTY,
    @Json(name = "type") val type:String = Constant.EMPTY,
    @Json(name = "current_address") val isCurrentAddress:Boolean = false
)

