package tradly.social.common.network.entity

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import tradly.social.domain.entities.Constant

@JsonClass(generateAdapter = true)
data class TagResponse(
    @field:Json(name = "data")val tagData:TagData = TagData()
):ResponseStatus()

@JsonClass(generateAdapter = true)
data class TagData(
    @field:Json(name = "tags") val tagList:List<TagEntity> = listOf()
)

@JsonClass(generateAdapter = true)
data class TagEntity(
    @field:Json(name = "id") val id:Int = 0,
    @field:Json(name = "name") val name:String = Constant.EMPTY
)