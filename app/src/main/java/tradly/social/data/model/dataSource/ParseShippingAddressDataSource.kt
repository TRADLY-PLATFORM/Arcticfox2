package tradly.social.data.model.dataSource


import android.net.Uri
import tradly.social.common.network.converters.AddressConverter
import tradly.social.data.model.network.retrofit.RetrofitManager
import tradly.social.domain.dataSource.ShippingAddressDataSource
import tradly.social.domain.entities.*
import com.parse.ParseObject
import com.parse.ktx.putOrIgnore
import tradly.social.common.base.ErrorHandler
import tradly.social.common.network.CustomError
import tradly.social.common.network.parseHelper.ParseClasses
import tradly.social.common.network.parseHelper.ParseConstant
import tradly.social.common.network.parseHelper.ParseManager
import kotlin.collections.HashMap

class ParseShippingAddressDataSource : ShippingAddressDataSource {

    override fun getCurrentAddress(shippingType: String): Result<ShippingAddress> =
        when (val result = getShippingAddresses(shippingType)) {
            is Result.Success -> {
                if(result.data.isNotEmpty()){
                    Result.Success(result.data[0])
                }
                else{
                    Result.Error(AppError(code = CustomError.CODE_NO_ADDRESS_SELECTED))
                }
            }
            is Result.Error -> result
        }

    override fun getShippingAddresses(shippingType:String): Result<List<ShippingAddress>> =
        when (val result = RetrofitManager.getInstance().getShippingAddress(shippingType)) {
            is Result.Success -> Result.Success(data = AddressConverter.mapFrom(result.data.data.addresses))
            is Result.Error -> result
        }

    override fun addShippingAddress(shippingAddress: ShippingAddress): Result<ShippingAddress> =
        when (val result = RetrofitManager.getInstance().addShippingAddress(getRequestBody(shippingAddress))) {
            is Result.Success -> Result.Success(shippingAddress.also { it.id = result.data.data.address.id.toString() })
            is Result.Error -> result
        }


    override fun updateShippingAddress(shippingAddress: ShippingAddress): Result<ShippingAddress> =
        when (val result = RetrofitManager.getInstance().updateShippingAddress(getRequestBody(shippingAddress),shippingAddress.id)) {
            is Result.Success -> {
                Result.Success(shippingAddress.also { it.id = result.data.data.address.id.toString() })
            }
            is Result.Error -> result
        }


    override fun updatePickupAddress(orderId: String,shippingAddress: ShippingAddress): Result<Boolean> {
        return when(val result = RetrofitManager.getInstance().updatePickupAddress(orderId,getShippingAddressParam(shippingAddress.id.toInt()))){
            is Result.Success->Result.Success(result.data.status)
            is Result.Error->result
        }
    }

    override fun changePresentAddress(currentAddressId: String, addressId: String) :Result<Boolean>{
        val current = ParseObject.createWithoutData(ParseClasses.SHIPPING_ADDRESS, currentAddressId)
            .apply { putOrIgnore(ParseConstant.DEFAULT_ADDRESS, 0) }
        val update = ParseObject.createWithoutData(ParseClasses.SHIPPING_ADDRESS, addressId)
            .apply { putOrIgnore(ParseConstant.DEFAULT_ADDRESS, 1) }
       return when (val result = ParseManager.getInstance().changePresentAddress(current, update)) {
            is Result.Success->Result.Success(true)
            is Result.Error->result
        }
    }

    override fun getCurrentAddressLocation(geoPoint: GeoPoint): Result<Address> {
       return when(val result = RetrofitManager.getInstance().getAddressByLocation(geoPoint)){
            is Result.Success->{
                if(result.data.status){
                    val address = AddressConverter.mapFrom(result.data)
                    address.geoPoint = geoPoint
                    Result.Success(address)
                }
                else{
                    Result.Error(ErrorHandler.getErrorInfo(result.data.error))
                }
            }
            is Result.Error->{result}
        }
    }

    override fun searchLocation(key: String):Result<List<Address>> {
        return when(val result = RetrofitManager.getInstance().searchLocation(Uri.encode(key))){
            is Result.Success->Result.Success(AddressConverter.mapFromList(result.data.addressData.addresses))
            is Result.Error->result
        }
    }

    override fun getShippingMethods(accountId:String): Result<List<ShippingMethod>> {
        if(accountId.isEmpty()){
            return getTenantShipment()
        }
        else{
            return when(val result = RetrofitManager.getInstance().getShippingMethods(accountId)){
                is Result.Success->{
                    if(result.data.status){
                        if(result.data.data.shippingMethodList.isEmpty()){
                            getTenantShipment()
                        }
                        else{
                            Result.Success(AddressConverter.mapFrom(result.data.data))
                        }
                    }
                    else{
                        Result.Error(exception = AppError(code = CustomError.GET_SHIPMENT_FAILED))
                    }
                }
                is Result.Error->result
            }
        }
    }

    private fun getTenantShipment():Result<List<ShippingMethod>>{
        return when(val result = RetrofitManager.getInstance().getTenantShippingMethods()){
            is Result.Success->Result.Success(AddressConverter.mapFrom(result.data.data))
            is Result.Error->result
        }
    }

    private fun getShippingAddressParam(addressId:Int):HashMap<String,Any?>{
        val map = hashMapOf<String,Any?>()
        map["pickup_address_id"] = addressId
        val body = hashMapOf<String,Any?>()
        body["operation"] = "update_pickup_address"
        body["order"] = map
        return body
    }

    private fun getRequestBody(shippingAddress: ShippingAddress):HashMap<String,Any?>{
        val address = hashMapOf<String,Any?>()
        address["name"] = shippingAddress.name
        address["phone_number"] = shippingAddress.phoneNumber
        if(shippingAddress.lane1.isNotEmpty()){
            address["address_line_1"] = shippingAddress.lane1
        }
        if(shippingAddress.lane2.isNotEmpty()){
            address["address_line_2"] = shippingAddress.lane2
        }
        if(!shippingAddress.landmark.isNullOrEmpty()){
            address["landmark"] = shippingAddress.landmark
        }
        address["state"] = shippingAddress.province
        address["post_code"] = shippingAddress.zipcode
        address["country"] = shippingAddress.country
        address["type"] = shippingAddress.type
        return hashMapOf("address" to address)
    }

}