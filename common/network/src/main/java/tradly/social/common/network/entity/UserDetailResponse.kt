package tradly.social.common.network.entity

import tradly.social.domain.entities.Constant
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserDetailResponse(
    @field:Json(name = "data") val response: Response = Response()
) : ResponseStatus()

@JsonClass(generateAdapter = true)
data class Response(
    @field:Json(name = "user") val user: UserEntity = UserEntity()
)

@JsonClass(generateAdapter = true)
data class AuthKeyEntity(
    @field:Json(name = "auth_key") val authKey: String = Constant.EMPTY,
    @field:Json(name = "refresh_key") val refreshKey: String = Constant.EMPTY,
    @field:Json(name = "firebase_token") val customToken:String = Constant.EMPTY
)

@JsonClass(generateAdapter = true)
data class UserEntity(
    @field:Json(name = "id")val id:String = Constant.EMPTY,
    @field:Json(name = "first_name") val firstName: String? = Constant.EMPTY,
    @field:Json(name = "last_name") val lastName: String? = Constant.EMPTY,
    @field:Json(name = "email") val email: String? = Constant.EMPTY,
    @field:Json(name = "profile_pic") val profilePic: String? = Constant.EMPTY,
    @field:Json(name = "email_verified") val emailVerified: Boolean? = false,
    @field:Json(name = "metadata") val metadata:UserMeta = UserMeta(),
    @field:Json(name = "mobile") val mobile:String = Constant.EMPTY,
    @field:Json(name = "dial_code") val dialCode:String = Constant.EMPTY,
    @field:Json(name = "key") val authKeyEntity: AuthKeyEntity = AuthKeyEntity()
)

@JsonClass(generateAdapter = true)
data class UserMeta(
    @field:Json(name = "stripe_connected") val isStripeConnected:Boolean = false,
    @field:Json(name = "stripe_connect_onboarding") val isStripeOnboardConnected:Boolean = false
)