package tradly.social.ui.shipment

import android.content.Context
import android.location.Geocoder
import android.location.Location
import tradly.social.R
import tradly.social.common.base.NetworkUtil
import tradly.social.data.model.dataSource.ParseShippingAddressDataSource
import tradly.social.domain.entities.*
import tradly.social.domain.repository.ShippingAddressRepository
import tradly.social.domain.usecases.ManageShippingAddress
import kotlinx.coroutines.*
import tradly.social.common.base.AppConstant
import tradly.social.common.base.AppController
import tradly.social.common.base.ErrorHandler
import java.io.IOException
import java.util.Locale
import kotlin.coroutines.CoroutineContext

class ShippingPresenter(var view: View?) : CoroutineScope {

    private var job: Job
    private var manageShippingAddress: ManageShippingAddress? = null

    interface View {
        fun onSuccess(shippingAddress: ShippingAddress)
        fun onLoadAddressList(list:List<ShippingAddress>)
        fun fieldError(id: Int, msg: Int)
        fun onFailure(appError: AppError)
        fun noNetwork()
        fun showProgressDialog()
        fun hideProgressDialog()
        fun showProgressLoader()
        fun hideProgressLoader()
        fun showLocationFetchingError()
        fun onLoadCurrentLocationAddress(address: Address)
    }

    init {
        val shippingAddressDataSource = ParseShippingAddressDataSource()
        val shippingAddressRepository = ShippingAddressRepository(shippingAddressDataSource)
        manageShippingAddress = ManageShippingAddress(shippingAddressRepository)
        job = Job()
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    fun addAddress(
        isFromCart:Boolean,
        name: String,
        phone: String,
        streetAddress1: String,
        streetAddress2:String,
        landMark:String?,
        city: String,
        state: String,
        country:String,
        zCode: String,
        type:String,
        isForPickupAddress:Boolean
    ) {
        if (validate(name, phone, streetAddress1,streetAddress2,landMark, city, state, country, zCode)) {
            if (NetworkUtil.isConnectingToInternet()) {
                view?.showProgressDialog()
                launch(Dispatchers.Main) {
                    val shippingAddress = ShippingAddress(
                        name = name,
                        city = city,
                        phoneNumber = phone,
                        defaultAddress = isFromCart,
                        lane1 = streetAddress1,
                        lane2 = streetAddress2,
                        province = state,
                        zipcode = zCode,
                        country = country,
                        landmark = landMark,
                        type = if(isForPickupAddress) AppConstant.AddressType.PICKUP_ADDRESS else if (type == AppConstant.ShipmentType.DELIVERY) AppConstant.AddressType.SHIPPING else AppConstant.AddressType.STORAGE_HUB
                    )
                    val call = async(Dispatchers.IO) { manageShippingAddress?.addShippingAddress(shippingAddress) }
                    when (val result = call.await()) {
                        is Result.Success -> view?.onSuccess(shippingAddress)
                        is Result.Error -> view?.onFailure(result.exception)
                    }
                    view?.hideProgressDialog()
                }
            }
            else{ view?.noNetwork() }
        }
    }


    fun getAddressList(shipmentType:String,isForPickupAddress: Boolean){
        if(NetworkUtil.isConnectingToInternet()){
            launch(Dispatchers.Main) {
                view?.showProgressLoader()
                val type = if(isForPickupAddress) AppConstant.AddressType.PICKUP_ADDRESS else if (shipmentType == AppConstant.ShipmentType.DELIVERY) AppConstant.AddressType.SHIPPING else AppConstant.AddressType.STORAGE_HUB
                val call = async(Dispatchers.IO){  manageShippingAddress?.getShippingAddresses(type)}
                when(val result = call.await()){
                    is Result.Success->{view?.onLoadAddressList(result.data) }
                    is Result.Error->{view?.onFailure(result.exception)}
                }
                view?.hideProgressLoader()
            }
        }
        else{ view?.noNetwork() }
    }

    fun updateAddress(
        address: ShippingAddress,
        name: String,
        phone: String,
        streetAddress1: String,
        streetAddress2:String,
        landMark:String?,
        city: String,
        state: String,
        country:String,
        zCode: String,
        type: String
    )
    {
        if (validate(name, phone, streetAddress1,streetAddress2,landMark, city, state, country, zCode)) {
            if(NetworkUtil.isConnectingToInternet()){
                address.apply {
                    this.name = name
                    this.city = city
                    this.phoneNumber = phone
                    this.lane1 = streetAddress1
                    this.lane2 = streetAddress2
                    this.province = state
                    this.zipcode = zCode
                    this.country = country
                    this.landmark = landMark
                    this.type = type
                }
                launch(Dispatchers.Main){
                    view?.showProgressDialog()
                    val call = async(Dispatchers.IO){ manageShippingAddress?.updateShippingAddress(address) }
                    when(val result = call.await()){
                        is Result.Success->{view?.onSuccess(address)}
                        is Result.Error->{view?.onFailure(result.exception)}
                    }
                    view?.hideProgressDialog()
                }
            }
            else{ view?.noNetwork() }
        }
    }

    fun changePresentAddress(oldId:String,newId:String) {
        if(NetworkUtil.isConnectingToInternet()){
            launch(Dispatchers.Main){
                view?.showProgressDialog()
                val call = async(Dispatchers.IO){manageShippingAddress?.changePresentAddress(oldId,newId)}
                when(val result = call.await()){
                    //is Result.Success->{view?.onSuccess()}
                    is Result.Error->{view?.onFailure(result.exception)}
                }
                view?.hideProgressDialog()
            }
        }
    }

    fun getCurrentLocationAddress(latitude:Double,longitude:Double,shouldShowProgress:Boolean = true){
        if(NetworkUtil.isConnectingToInternet()){
            launch(Dispatchers.Main){
                if(shouldShowProgress){
                    view?.showProgressDialog()
                }
                val call = async(Dispatchers.IO){ manageShippingAddress?.getCurrentAddressLocation(GeoPoint(latitude, longitude)) }
                when(val result = call.await()){
                    is Result.Success->{view?.onLoadCurrentLocationAddress(result.data)}
                    is Result.Error->view?.showLocationFetchingError()
                }
                view?.hideProgressDialog()
            }
        }
        else{
            view?.noNetwork()
        }
    }

    fun getCurrentLocationAddress(ctx: Context,location:Location,callback:(address:android.location.Address?)->Unit){
        if(NetworkUtil.isConnectingToInternet()){
            launch {
                val call = async(Dispatchers.IO){ getAddressFromLocation(ctx,location) }
                callback(call.await())
            }
        }
    }

    private fun getAddressFromLocation(ctx:Context, location: Location):android.location.Address?{
        try{
            val geoCoder = Geocoder(ctx, Locale.getDefault())
            if(Geocoder.isPresent()){
                val addressList = geoCoder.getFromLocation(
                    location.latitude, location.longitude, 1
                )
                if (addressList != null && addressList.size > 0) {
                    return addressList[0]
                }
            }
        }catch (ex:IOException){
            ErrorHandler.printLog(ex.message)
        }
        return null
    }

    companion object{
        fun getFormedAddress(shippingAddress: ShippingAddress): String =
            String.format(AppController.appContext.getString(R.string.shipment_address_data),shippingAddress.lane1,shippingAddress.city,shippingAddress.province,shippingAddress.zipcode)

    }
    fun validate(
        name: String?,
        phone: String?,
        streetAddress1: String?,
        streetAddress2: String?,
        landMark:String?,
        city: String?,
        state: String?,
        country:String,
        zipCode: String?
    ): Boolean {
        var isValid = true
        if (name.isNullOrEmpty()) {
            isValid = false
            view?.fieldError(R.id.edName, R.string.login_required)
        } else if (phone.isNullOrEmpty()) {
            isValid = false
            view?.fieldError(R.id.edPhone, R.string.login_required)
        } else if (streetAddress1.isNullOrEmpty()) {
            isValid = false
            view?.fieldError(R.id.edStAddressOne, R.string.login_required)
        } /*else if (city.isNullOrEmpty()) {
            isValid = false
            view?.fieldError(R.id.edCity, R.string.required)
        }*/
        else if (state.isNullOrEmpty()) {
            isValid = false
            view?.fieldError(R.id.edState, R.string.login_required)
        } else if (zipCode.isNullOrEmpty()) {
            isValid = false
            view?.fieldError(R.id.edZipCode, R.string.login_required)
        }
        return isValid
    }
}