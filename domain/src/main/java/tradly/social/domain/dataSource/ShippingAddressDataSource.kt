package tradly.social.domain.dataSource

import tradly.social.domain.entities.*

interface ShippingAddressDataSource {
    fun getShippingAddresses(shippingType:String):Result<List<ShippingAddress>>
    fun getCurrentAddress(shippingType: String):Result<ShippingAddress>
    fun addShippingAddress(shippingAddress: ShippingAddress):Result<ShippingAddress>
    fun updateShippingAddress(shippingAddress: ShippingAddress):Result<ShippingAddress>
    fun updatePickupAddress(orderId: String,shippingAddress: ShippingAddress):Result<Boolean>
    fun changePresentAddress(currentAddressId: String, addressId: String):Result<Boolean>
    fun getCurrentAddressLocation(geoPoint: GeoPoint):Result<Address>
    fun searchLocation(key:String):Result<List<Address>>
    fun getShippingMethods(accountId:String):Result<List<ShippingMethod>>
}