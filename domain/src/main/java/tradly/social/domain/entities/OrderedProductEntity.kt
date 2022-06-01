package tradly.social.domain.entities

data class OrderedProductEntity(
    val id:Int,
    val image: String,
    val name: String,
    val description: String,
    val units: String,
    val price: String,
    var reviewStatus:Boolean
)