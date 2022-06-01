package tradly.social.common.network.entity

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ActivitiesResponse(
    @field:Json(name = "data") val data:ActivitiesData
):ResponseStatus()

@JsonClass(generateAdapter = true)
data class ActivitiesData(
    @field:Json(name = "total_records") val totalRecords:Int,
    @field:Json(name = "page") val page:Int,
    @field:Json(name = "activities") val activities:List<ActivitiesEntity> = listOf()
)

@JsonClass(generateAdapter = true)
data class ActivitiesEntity(
    @field:Json(name = "user") val user:UserEntity,
    @field:Json(name = "account") val account:StoreEntity,
    @field:Json(name = "listing") val listing:ProductEntity,
    @field:Json(name = "reference_id") val referenceId:Int,
    @field:Json(name = "reference_type") val referenceType:Int,
    @field:Json(name = "metadata") val metadata:MetaData,
    @field:Json(name = "type") val type:Int,
    @field:Json(name = "created_at") val createdAt:Long
)

@JsonClass(generateAdapter = true)
data class MetaData(
   @field:Json(name = "order_status") val orderStatus:Int=0,
   @field:Json(name = "reference_number") val referenceNumber:Int=0,
   @field:Json(name = "account_id") val accountId:Int=0
)