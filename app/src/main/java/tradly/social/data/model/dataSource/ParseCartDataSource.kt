package tradly.social.data.model.dataSource

import tradly.social.common.base.getOrNull
import tradly.social.common.network.converters.CartDetailConverter
import tradly.social.common.network.parseHelper.ParseManager
import tradly.social.domain.dataSource.CartDataSource
import tradly.social.domain.entities.CartDetails
import tradly.social.domain.entities.Result
import tradly.social.data.model.network.retrofit.RetrofitManager
import tradly.social.common.network.CustomError
import tradly.social.domain.entities.AppError
import tradly.social.domain.entities.Cart

class ParseCartDataSource : CartDataSource {
    override fun addToCart(id: String?, quantity: Int, type: Int): Result<Boolean> =

        when (val result = RetrofitManager.getInstance().addToCart(getRequestBody(id,quantity,true))) {
            is Result.Success ->Result.Success((result.data.status))
            is Result.Error -> result
        }



    override fun removeCartItem(id: String?) =
        when(val result = RetrofitManager.getInstance().deleteCart(getRequestBody(id,isAddCart = false))){
            is Result.Success ->{
                if(result.data.status){
                    Result.Success((result.data.status))
                }
                else{
                    Result.Error(AppError(code = CustomError.DELETE_CART_ERROR))
                }
            }
            is Result.Error -> result
        }

    override fun getCartItems(shippingMethodId:String): Result<Cart> =
        when (val result = RetrofitManager.getInstance().getCartList(shippingMethodId.getOrNull())) {
            is Result.Success -> {
                Result.Success(CartDetailConverter.mapFrom(result.data.data))
            }
            is Result.Error -> {
                result
            }
        }

    override fun clearCartItems(): Result<Boolean> =
        when(val result = RetrofitManager.getInstance().clearCart()){
            is Result.Success->{
                if(result.data.status){
                    Result.Success(result.data.status)
                }
                else{
                    Result.Error(exception = AppError(code = CustomError.CLEAR_CART_FAILED))
                }
            }
            is Result.Error->result
        }

    override fun findCartItem(id: String?,type:Int): Result<CartDetails> =
        when(val result = ParseManager.getInstance().findCartItem(id,type)){
            is Result.Success->{Result.Success(CartDetails())}
            is Result.Error->result
        }

    private fun getRequestBody(listingId:String?,qty:Int = 0,isAddCart:Boolean):HashMap<String,Any?>{
        val cart = hashMapOf<String,Any?>()
        if(qty != 0){
            cart["quantity"] = qty
        }
        cart["listing_id"] = if(isAddCart) listingId else listOf(listingId)
        return hashMapOf("cart" to cart)
    }
}