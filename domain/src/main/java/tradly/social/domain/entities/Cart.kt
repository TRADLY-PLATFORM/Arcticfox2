package tradly.social.domain.entities

data class Cart(
   val cartDetail: CartDetail,
   val cartList: List<CartItem>
)

data class CartDetail(
    val id:Int,
    val grandTotal:Price,
    val offerTotal:Price,
    val shippingTotal:Shipping
)

data class CartItem(
    val id: String,
    var quantity:Int,
    var totalPrice: Price,
    var offerPrice: Price,
    val listing:Product
)

data class Shipping(
    var shippingEnabled: Boolean = false,
    val amount: Double = 0.0,
    val formattedAmount:String = Constant.EMPTY,
    val currency:String = Constant.EMPTY,
    val message: String = Constant.EMPTY
)