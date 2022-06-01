package tradly.social.domain.entities

data class Earning(
    val totalSales: Price,
    val pendingBalance:Price,
    val totalPayouts:Price
)