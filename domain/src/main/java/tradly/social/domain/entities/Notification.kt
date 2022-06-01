package tradly.social.domain.entities

data class Notification(
    val user: User,
    val store: Store,
    val product:Product,
    val referenceType:Int,
    val referenceId:Int,
    val orderStatus:Int,
    val accountId:String,
    val referenceNumber:Int,
    val type:Int,
    val createdAt:Long
)