package tradly.social.domain.entities

data class SubscriptionProduct(
    val id: Int = 0,
    val sku: String = Constant.EMPTY,
    val price: Double = 0.0,
    val imagePath: String = Constant.EMPTY,
    val allowedListing: Int = 0,
    val feeFixedPerOrder: Double = 0.0,
    val commissionPercentPerOrder: Double = 0.0,
    val expiryDays: Int = 0,
    val type: String = Constant.EMPTY,
    val active: Int = 0,
    val title: String = Constant.EMPTY,
    val description: String = Constant.EMPTY,
    val subscriptionStatus: Boolean = false
)