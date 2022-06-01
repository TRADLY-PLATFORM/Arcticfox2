package tradly.social.domain.entities

data class Payout(
    val url: String = Constant.EMPTY,
    val isStripeOnBoardingConnected:Boolean = false,
    val isPayoutEnabled:Boolean = false,
    val errors:List<String> = listOf()
)