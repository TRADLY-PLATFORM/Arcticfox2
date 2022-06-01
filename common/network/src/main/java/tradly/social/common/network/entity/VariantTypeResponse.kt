package tradly.social.common.network.entity

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Json
import tradly.social.domain.entities.Constant

@JsonClass(generateAdapter = true)
data class VariantTypeResponse(
    @field:Json(name = "data") val variantTypeData:VariantTypeData
):ResponseStatus()

@JsonClass(generateAdapter = true)
data class VariantTypeData(
    @field:Json(name = "variant_types") val variantTypes:List<VariantTypeEntity> = emptyList()
)

@JsonClass(generateAdapter = true)
data class VariantTypeEntity(
    @field:Json(name = "id") val id: Int = 0,
    @field:Json(name = "active") val active: Boolean = false,
    @field:Json(name = "name") val name: String = Constant.EMPTY,
    @field:Json(name = "values") val variantTypeValues:List<VariantTypeEntity> = emptyList()
)
