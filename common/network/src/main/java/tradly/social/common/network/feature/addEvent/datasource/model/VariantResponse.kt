package tradly.social.common.network.feature.addEvent.datasource.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import tradly.social.common.network.entity.ResponseStatus
import tradly.social.common.network.entity.VariantEntity

@JsonClass(generateAdapter = true)
class VariantResponse(
    @field:Json(name = "data") val data:VariantData
):ResponseStatus()

@JsonClass(generateAdapter = true)
class VariantData(
    @field:Json(name = "variant") val variant:VariantEntity
)


