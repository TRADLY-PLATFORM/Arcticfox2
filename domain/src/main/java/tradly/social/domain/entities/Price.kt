package tradly.social.domain.entities

data class Price(
    val amount: Double = 0.0,
    val currency: String = Constant.EMPTY,
    val displayCurrency: String = Constant.EMPTY
)