package tradly.social.common.network.entity

import tradly.social.domain.entities.Constant
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SignedURLResponse(
    @field:Json(name = "data") val response:SignedURLData = SignedURLData()
):ResponseStatus()

@JsonClass(generateAdapter = true)
data class SignedURLData(
    @field:Json(name = "result") val signedUrlList:List<SignedURLEntity> = listOf()
)

@JsonClass(generateAdapter = true)
data class SignedURLEntity(
    @field:Json(name = "fileUri") val fileUri:String = Constant.EMPTY,
    @field:Json(name = "signedUrl") val signedUrl:String = Constant.EMPTY
)