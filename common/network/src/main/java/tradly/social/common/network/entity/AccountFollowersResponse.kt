package tradly.social.common.network.entity

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class AccountFollowersResponse(
    @field:Json(name = "data") val followersData: FollowersData
):ResponseStatus()

@JsonClass(generateAdapter = true)
data class FollowersData(
    @field:Json(name = "total_records") val totalRecords:Int = 0,
    @field:Json(name = "users") val users:List<UserEntity>
)