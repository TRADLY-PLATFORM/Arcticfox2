package tradly.social.common.network.entity

import tradly.social.domain.entities.Constant
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AddressResponse(
    @field:Json(name = "data") val addressData: AddressData = AddressData()
) : ResponseStatus()

@JsonClass(generateAdapter = true)
data class AddressData(
    @field:Json(name = "address") val addressEntity: AddressEntity = AddressEntity(),
    @field:Json(name = "addresses") val addresses:List<LocationEntity> = listOf()
)

@JsonClass(generateAdapter = true)
data class AddressEntity(
    @field:Json(name = "location") val locationEntity: LocationEntity = LocationEntity()
)

@JsonClass(generateAdapter = true)
data class LocationEntity(
    @field:Json(name = "locality") val locality: String? = Constant.EMPTY,
    @field:Json(name = "country") val country: String? = Constant.EMPTY,
    @field:Json(name = "postcode") val postalCode: String? = Constant.EMPTY,
    @field:Json(name = "formatted_address") val formattedAddress: String? = Constant.EMPTY,
    @field:Json(name = "city") val city:String? = Constant.EMPTY,
    @field:Json(name = "country_code")val countryCode:String? = Constant.EMPTY,
    @field:Json(name = "state") val state:String? = Constant.EMPTY,
    @field:Json(name = "latitude") val latitude:Double = 0.0,
    @field:Json(name = "longitude") val longitude:Double = 0.0,
    @field:Json(name = "type") val type:String = Constant.EMPTY
)

@JsonClass(generateAdapter = true)
data class GeoPointEntity(
    @field:Json(name = "latitude") val latitude:Double? = 0.0,
    @field:Json(name = "longitude") val longitude:Double? = 0.0

)