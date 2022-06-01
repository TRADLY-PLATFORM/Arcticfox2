package tradly.social.common.network.entity

import tradly.social.domain.entities.Constant
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CurrencyResponse(
    @field:Json(name = "data") val currencyData:CurrencyData = CurrencyData()
):ResponseStatus()

@JsonClass(generateAdapter = true)
data class CurrencyData(
    @field:Json(name = "currencies") val currencyList:List<CurrencyEntity> = listOf()
)
@JsonClass(generateAdapter = true)
data class CurrencyEntity(
    @field:Json(name = "id")val id:Int = 0,
    @field:Json(name = "name")val name:String = Constant.EMPTY,
    @field:Json(name = "code") val code:String = Constant.EMPTY,
    @field:Json(name = "symbol")val symbol:String = Constant.EMPTY,
    @field:Json(name = "precision") val precision:String = Constant.EMPTY,
    @field:Json(name = "tenant_id") val tenantId:Int = 0,
    @field:Json(name = "image_path") val imagePath:String = Constant.EMPTY,
    @field:Json(name = "format") val format:String = Constant.EMPTY,
    @field:Json(name = "exchange_rate") val exchangeRate:String = Constant.EMPTY,
    @field:Json(name = "decimal_point") val decimalPoint:String = Constant.EMPTY,
    @field:Json(name = "thousand_separator") val thousandSeperator:String = Constant.EMPTY,
    @field:Json(name = "default") val default:Boolean = false,
    @field:Json(name = "order_by") val orderBy:Int = 0,
    @field:Json(name = "active") val active:Boolean = false,
    @field:Json(name = "archived") val archived:Int = 0
)