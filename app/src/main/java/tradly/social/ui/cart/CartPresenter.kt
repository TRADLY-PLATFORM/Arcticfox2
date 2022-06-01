package tradly.social.ui.cart

import tradly.social.common.base.NetworkUtil
import tradly.social.data.model.dataSource.ParseCartDataSource
import tradly.social.data.model.dataSource.ParseShippingAddressDataSource
import tradly.social.domain.repository.CartRepository
import tradly.social.domain.repository.ShippingAddressRepository
import tradly.social.domain.usecases.AddCart
import tradly.social.domain.usecases.GetCart
import tradly.social.domain.usecases.ManageShippingAddress
import com.parse.ParseException
import kotlinx.coroutines.*
import tradly.social.common.base.AppConfigHelper
import tradly.social.common.base.AppConstant
import tradly.social.data.model.CoroutinesManager
import tradly.social.common.base.ErrorHandler
import tradly.social.common.network.CustomError
import tradly.social.domain.entities.*
import kotlin.coroutines.CoroutineContext

class CartPresenter(var view: View?) : CoroutineScope {

    private var job: Job
    private var getCart: GetCart
    private var addCart:AddCart
    private var manageShippingAddress:ManageShippingAddress
    private var shipmentMethodEnabled:Boolean
    interface View {
        fun onSuccess(cartResult: Cart,isFor:Int)
        fun onFailure(appError: AppError)
        fun onCartItemRemoved()
        fun showProgressLoader()
        fun hideProgressLoader()
        fun showProgressDialog()
        fun hideProgressDialog()
        fun showShipmentList(list: List<ShippingMethod>)
        fun isItemInCart(isInItem:Boolean)
        fun showCurrentAddress(shippingAddress: ShippingAddress?)
        fun onCartAdded()
        fun noNetwork()
    }

    object CartAction{
        const val NONE = 0
        const val CART_REMOVE = 1
        const val CART_QTY = 2
        const val CART_BY_SHIPMENT = 3
        const val CART_BY_SELECT_SHIPMENT = 4
    }
    init {
        val parseCartDataSource = ParseCartDataSource()
        val cartRepository = CartRepository(parseCartDataSource)
        val shippingAddressDataSource = ParseShippingAddressDataSource()
        manageShippingAddress = ManageShippingAddress(ShippingAddressRepository(shippingAddressDataSource))
        shipmentMethodEnabled = AppConfigHelper.getConfigKey(AppConfigHelper.Keys.SHIPPING_METHOD_PREFERENCE)
        getCart = GetCart(cartRepository)
        addCart = AddCart(cartRepository)
        job = SupervisorJob()
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    fun getCartItems(isFor:Int=0) {
        if (NetworkUtil.isConnectingToInternet()) {
            when(isFor){
                CartAction.CART_REMOVE,
                CartAction.CART_QTY->{}
                else->{view?.showProgressLoader()}
            }
            launch(Dispatchers.Main) {
                val call = async(Dispatchers.IO) { getCart.getCartItems()}
                when(val result = call.await()){
                    is Result.Success->{
                        view?.onSuccess(result.data,isFor)
                    }
                    is Result.Error->{view?.onFailure(result.exception)}
                }
                when(isFor){
                    CartAction.CART_REMOVE,
                    CartAction.CART_QTY->{view?.hideProgressDialog()}
                    else->{view?.hideProgressLoader()}
                }
            }

        }
    }

    fun findItemInCart(id:String,type:Int){
        launch(Dispatchers.Main){
            view?.showProgressDialog()
            val call = async(Dispatchers.IO){ getCart?.findCartItem(id,type) }
            when(val result = call.await()){
                is Result.Success->{view?.isItemInCart(true)}
                is Result.Error->{
                    if(result.exception.code == ParseException.OBJECT_NOT_FOUND){
                        view?.isItemInCart(false)
                    }
                    else{
                        view?.onFailure(result.exception)
                    }
                }
            }
            view?.hideProgressDialog()
        }
    }
    fun removeCartItem(cartId:String){
        if(NetworkUtil.isConnectingToInternet()){
            launch(Dispatchers.Main){
                view?.showProgressDialog()
                val call = async(Dispatchers.IO){ getCart?.removeCartItem(cartId) }
                when(val result = call.await()){
                    is Result.Success->{view?.onCartItemRemoved()}
                    is Result.Error->{view?.onFailure(result.exception)}
                }
            }
        }
    }

    fun addCartItem(id:String?,type:Int,quantity:Int=1){
        if(NetworkUtil.isConnectingToInternet()){
            view?.showProgressDialog()
            launch(Dispatchers.Main){
                val call = async (Dispatchers.IO){ addCart?.addCart(id, quantity, type) }
                when(val result = call.await()){
                    is Result.Success->{view?.onCartAdded()}
                    is Result.Error->{view?.onFailure(result.exception)}
                }
                view?.hideProgressDialog()
            }
        }
    }


    fun getShippingMethods(accountId:String){
        view?.showProgressLoader()
        CoroutinesManager.ioThenMain({ manageShippingAddress.getShippingMethods(accountId) },{ result->
            when(result){
                is Result.Success->{
                    if(result.data.isNotEmpty()){
                        val defaultShipment = result.data[0]
                        getCartByShipment(defaultShipment,CartAction.CART_BY_SHIPMENT)
                        if(AppConstant.ShipmentType.DELIVERY == defaultShipment.type ){
                            getCurrentAddress(AppConstant.AddressType.SHIPPING,false)
                        }
                        else if(AppConstant.ShipmentType.STORAGE_HUB == defaultShipment.type){
                            getCurrentAddress(AppConstant.AddressType.STORAGE_HUB,false)
                        }
                        view?.showShipmentList(result.data)
                    }
                }
                is Result.Error->view?.onFailure(result.exception)
            }
            view?.hideProgressLoader()
        })
    }

    fun getCartByShipment(shipmentMethod: ShippingMethod, isFor: Int){
        when(isFor){
            CartAction.CART_BY_SELECT_SHIPMENT->view?.showProgressDialog()
        }
        CoroutinesManager.ioThenMain({getCart.getCartItems(shipmentMethod.id.toString())}){ result->
            when(result){
                is Result.Success->{
                    view?.onSuccess(result.data,CartAction.NONE)
                    if(AppConstant.ShipmentType.DELIVERY==shipmentMethod.type){
                        getCurrentAddress(AppConstant.AddressType.SHIPPING,false)
                    }
                    else if( AppConstant.ShipmentType.STORAGE_HUB==shipmentMethod.type){
                        getCurrentAddress(AppConstant.AddressType.STORAGE_HUB,false)
                    }
                }
                is Result.Error->view?.onFailure(result.exception)
            }
            when(isFor){
                CartAction.CART_BY_SELECT_SHIPMENT->view?.hideProgressDialog()
            }
        }
    }

    fun getCurrentAddress(shippingType:String, showProgressDialog:Boolean = true) {
        if (showProgressDialog) {
            view?.showProgressDialog()
        }
        CoroutinesManager.ioThenMain({ manageShippingAddress.getCurrentAddress(shippingType) }) { result ->
            when (result) {
                is Result.Success -> view?.showCurrentAddress(result.data)
                is Result.Error -> {
                    if (result.exception.code == CustomError.CODE_NO_ADDRESS_SELECTED) {
                        view?.showCurrentAddress(null)
                    } else {
                        view?.onFailure(result.exception)
                    }
                }
            }
            view?.hideProgressDialog()
        }
    }
}