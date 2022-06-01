package tradly.social.common.network.converters


import tradly.social.common.network.entity.CartData
import tradly.social.common.network.entity.CartDetails
import tradly.social.domain.entities.Cart
import tradly.social.domain.entities.CartDetail
import tradly.social.domain.entities.CartItem

object CartDetailConverter {
    fun mapFrom(from: CartData):Cart{
       return Cart(
            cartDetail = CartDetail(
                id = from.cartTotal.id,
                grandTotal = ProductConverter.mapFrom(from.cartTotal.grandTotal),
                offerTotal = ProductConverter.mapFrom(from.cartTotal.offerTotal),
                shippingTotal =  ProductConverter.mapFrom(from.cartTotal.shippingCharges)
            ),
            cartList = mapFrom(from.cartDetails)
        )
    }

    private fun mapFrom(cartDetails: List<CartDetails>): List<CartItem> {
        val list = mutableListOf<CartItem>()
        cartDetails.forEach {
            list.add(mapFrom(it))
        }
        return list
    }

    fun mapFrom(cartDetails: CartDetails) = CartItem(
        id = cartDetails.id.toString(),
        quantity = cartDetails.quantity,
        totalPrice = ProductConverter.mapFrom(cartDetails.quantityTotalPrice),
        offerPrice = ProductConverter.mapFrom(cartDetails.quantityTotalOfferPrice),
        listing = ProductConverter.mapFrom(cartDetails.listing)
    )
}