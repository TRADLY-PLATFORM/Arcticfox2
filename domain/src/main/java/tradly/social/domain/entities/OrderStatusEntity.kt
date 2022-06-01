package tradly.social.domain.entities

data class OrderStatusEntity(
    val description: String,
    val date: Long,
    val time: Long,
    val inProgress: Boolean,
    val status:Int
)