package tradly.social.common.network.entity

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import tradly.social.domain.entities.Constant

@JsonClass(generateAdapter = true)
data class ShippingMethodResponse(
    @field:Json(name = "data") val data:ShippingMethodData
):ResponseStatus()

@JsonClass(generateAdapter = true)
data class ShippingMethodData(
    @field:Json(name = "shipping_methods") val shippingMethodList:List<ShippingMethodEntity>
)
@JsonClass(generateAdapter = true)
data class ShippingMethodEntity(
    @field:Json(name = "id") val id:Int = 0,
    @field:Json(name = "name") val name:String = Constant.EMPTY,
    @field:Json(name = "logo_path") val logoPath:String = Constant.EMPTY,
    @field:Json(name = "type") val type:String = Constant.EMPTY,
    @field:Json(name = "default") val default:Boolean = false
)