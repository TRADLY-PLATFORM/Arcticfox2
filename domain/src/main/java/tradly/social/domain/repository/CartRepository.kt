package tradly.social.domain.repository

import tradly.social.domain.dataSource.CartDataSource

class CartRepository(val cartDataSource: CartDataSource) {
    suspend fun addToCart(id:String?,quantity:Int,type:Int) = cartDataSource.addToCart(id, quantity, type)
    suspend fun removeCartItem(id: String?) = cartDataSource.removeCartItem(id)
    suspend fun getCartItems(shippingMethodId:String) = cartDataSource.getCartItems(shippingMethodId)
    suspend fun clearCartItems() = cartDataSource.clearCartItems()
    suspend fun  findCartItem(id:String?,type:Int) = cartDataSource.findCartItem(id,type)
}