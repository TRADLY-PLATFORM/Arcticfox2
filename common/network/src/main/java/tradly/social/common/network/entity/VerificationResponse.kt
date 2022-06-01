package tradly.social.common.network.entity

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VerificationResponse(
    @field:Json(name = "data") val verificationEntity: VerificationEntity = VerificationEntity()
) : ResponseStatus()

@JsonClass(generateAdapter = true)
data class VerificationEntity(
    @field:Json(name = "verify_id") val verifyId:Int=0
)