package tradly.social.domain.usecases

import tradly.social.domain.entities.Constant
import tradly.social.domain.repository.CartRepository

class GetCart(val cartRepository: CartRepository) {
    suspend fun getCartItems(shippingMethodId:String = Constant.EMPTY) = cartRepository.getCartItems(shippingMethodId)
    suspend fun findCartItem(id:String?,type:Int) = cartRepository.findCartItem(id,type)
    suspend fun clearCartItems() = cartRepository.clearCartItems()
    suspend fun removeCartItem(id:String)= cartRepository.removeCartItem(id)
}