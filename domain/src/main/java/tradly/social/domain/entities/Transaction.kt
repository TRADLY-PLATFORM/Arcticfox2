package tradly.social.domain.entities

data class Transaction(
    val transactionNumber:String,
    val referenceType:Int,
    val referenceId:Int,
    val amount: Price,
    val supertype:Int,
    val type:Int,
    val orderId:Int,
    val orderReferenceNumber:Int,
    val createdAt:Long
)