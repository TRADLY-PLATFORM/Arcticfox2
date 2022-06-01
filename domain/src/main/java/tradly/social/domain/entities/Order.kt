package tradly.social.domain.entities

data class Order(
    val id:String,
    val image:String,
    val storeName: String,
    val listingName:String,
    val orderId: String,
    val timeStamp: Long,
    val price : String
)