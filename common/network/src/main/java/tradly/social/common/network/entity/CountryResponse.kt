package tradly.social.common.network.entity

import tradly.social.domain.entities.Constant
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CountryResponse(
    @field:Json(name = "data") val data:CountryData = CountryData()
):ResponseStatus()


@JsonClass(generateAdapter = true)
data class CountryData(
    @field:Json(name = "countries") val countries:List<CountryEntity> = listOf()
)

@JsonClass(generateAdapter = true)
data class CountryEntity(
    @field:Json(name = "id") val id:Int = 0,
    @field:Json(name = "name") val name:String = Constant.EMPTY,
    @field:Json(name = "code2") val code2:String? = Constant.EMPTY,
    @field:Json(name = "code3") val code3:String? = Constant.EMPTY,
    @field:Json(name = "dial_code") val dialCode:String? = Constant.EMPTY,
    @field:Json(name = "time_zone") val timeZone:String? = Constant.EMPTY,
    @field:Json(name = "currency_code")val currencyCode:String? = Constant.EMPTY,
    @field:Json(name = "currency_en") val currencyEn:String? = Constant.EMPTY,
    @field:Json(name = "currency_locale") val currencyLocale:String? = Constant.EMPTY,
    @field:Json(name = "locale") val locale:String? = Constant.EMPTY,
    @field:Json(name = "mobile_number_regex") val mobileNumberRegex:String = Constant.EMPTY,
    @field:Json(name = "mobile_number_legth") val mobileNumberLength:String = Constant.EMPTY,
    @field:Json(name = "flag_url") val flagUrl:String = Constant.EMPTY
)