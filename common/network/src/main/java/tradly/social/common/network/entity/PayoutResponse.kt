package tradly.social.common.network.entity

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import tradly.social.domain.entities.Constant

@JsonClass(generateAdapter = true)
data class PayoutResponse(
        @field:Json(name = "data") val data:PayoutData
):ResponseStatus()

@JsonClass(generateAdapter = true)
data class PayoutData(
    @field:Json(name = "oauth_url") val oAuthUrl:String = Constant.EMPTY,
    @field:Json(name = "account_link") val accountLink:String = Constant.EMPTY,
    @field:Json(name = "login_link") val loginLink:String = Constant.EMPTY,
    @field:Json(name = "stripe_connect_onboarding") val isStripeOnBoardingConnected:Boolean = false,
    @field:Json(name = "payouts_enabled") val isPayoutEnabled:Boolean = false,
    @field:Json(name = "errors") val errors:List<StripeError> = listOf()
)

@JsonClass(generateAdapter = true)
data class StripeError(
    @field:Json(name = "reason") val reason:String = Constant.EMPTY
)