package tradly.social.domain.usecases

import tradly.social.domain.repository.CartRepository

class AddCart(val cartRepository: CartRepository) {
    suspend fun addCart(id:String?,quantity:Int,type:Int) = cartRepository.addToCart(id, quantity, type)
}