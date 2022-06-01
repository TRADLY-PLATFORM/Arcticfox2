package tradly.social.common.network.entity
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GroupDetailResponse(
    @field:Json(name = "data") val groupDetailData: GroupDetailData = GroupDetailData()
):ResponseStatus()

@JsonClass(generateAdapter = true)
data class GroupDetailData(
    @field:Json(name = "group") val groupDetailEntity: GroupDetailEntity = GroupDetailEntity()
)
@JsonClass(generateAdapter = true)
data class GroupDetailEntity(
    @field:Json(name = "id") val id:Int = 0
)