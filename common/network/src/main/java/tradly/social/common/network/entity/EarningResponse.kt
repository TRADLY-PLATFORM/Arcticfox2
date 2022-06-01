package tradly.social.common.network.entity

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class EarningResponse(
    @field:Json(name = "data") val earningData: EarningData
):ResponseStatus()

@JsonClass(generateAdapter = true)
data class EarningData(
    @field:Json(name = "total_sales") val totalSales:PriceEntity,
    @field:Json(name = "pending_balance") val pendingBalance:PriceEntity,
    @field:Json(name = "total_payouts") val totalPayouts:PriceEntity
)

