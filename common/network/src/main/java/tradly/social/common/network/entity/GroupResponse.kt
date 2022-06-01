package tradly.social.common.network.entity

import tradly.social.domain.entities.Constant
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GroupResponse(
    @field:Json(name = "data") val responseData:GroupTypeData = GroupTypeData()
):ResponseStatus()

@JsonClass(generateAdapter = true)
data class GroupTypeData(
    @field:Json(name = "groups_types") val groupTypes:List<GroupTypeEntity> = listOf()
)

@JsonClass(generateAdapter = true)
data class GroupTypeEntity(
    @field:Json(name = "id") val id:Int = 0,
    @field:Json(name = "name") val name:String = Constant.EMPTY,
    @field:Json(name = "image_path") val imagePath:String = Constant.EMPTY
)