package tradly.social.domain.dataSource

import tradly.social.domain.entities.Cart
import tradly.social.domain.entities.CartDetails
import tradly.social.domain.entities.Result

interface CartDataSource {
    fun addToCart(id:String?,quantity:Int,type:Int):Result<Boolean>
    fun removeCartItem(id: String?):Result<Boolean>
    fun getCartItems(shippingMethodId:String):Result<Cart>
    fun clearCartItems():Result<Boolean>
    fun findCartItem(id:String?,type:Int):Result<CartDetails>
}