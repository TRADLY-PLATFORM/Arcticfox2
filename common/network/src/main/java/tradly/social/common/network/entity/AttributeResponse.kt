package tradly.social.common.network.entity

import tradly.social.domain.entities.Constant
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AttributeResponse(
    @field:Json(name = "data") val data: AttributeData = AttributeData()
) : ResponseStatus()

@JsonClass(generateAdapter = true)
data class AttributeData(
    @field:Json(name = "attributes") val attributeList: List<AttributeEntity> = listOf()
)

@JsonClass(generateAdapter = true)
data class AttributeEntity(
    @field:Json(name = "id") val id: Int = 0,
    @field:Json(name = "name") val name: String = Constant.EMPTY,
    @field:Json(name = "field_type") val type: Int = 0,
    @field:Json(name = "optional") val optional: Boolean = false,
    @field:Json(name = "values") val valueList: Any?= null
)

@JsonClass(generateAdapter = true)
data class ValueEntity(
    @field:Json(name = "id") val id: Int = 0,
    @field:Json(name = "name") val name: String = Constant.EMPTY
)
