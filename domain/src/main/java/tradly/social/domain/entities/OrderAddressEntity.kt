package tradly.social.domain.entities

data class OrderAddressEntity(
    val id:Int,
    val name: String,
    val address: String,
    val phoneNumber: String,
    val formattedAddress:String
)