package tradly.social.domain.repository

import tradly.social.domain.dataSource.ShippingAddressDataSource
import tradly.social.domain.entities.GeoPoint
import tradly.social.domain.entities.ShippingAddress

class ShippingAddressRepository(private val shippingAddressDataSource: ShippingAddressDataSource) {
    suspend fun getShippingAddresses(shippingType:String) = shippingAddressDataSource.getShippingAddresses(shippingType)
    suspend fun getCurrentAddress(shippingType: String) = shippingAddressDataSource.getCurrentAddress(shippingType)
    suspend fun addShippingAddress(shippingAddress: ShippingAddress) = shippingAddressDataSource.addShippingAddress(shippingAddress)
    suspend fun updateShippingAddress(shippingAddress: ShippingAddress) = shippingAddressDataSource.updateShippingAddress(shippingAddress)
    suspend fun changePresentAddress(currentAddressId: String, addressId: String) = shippingAddressDataSource.changePresentAddress(currentAddressId, addressId)
    suspend fun getCurrentAddressLocation(geoPoint: GeoPoint) = shippingAddressDataSource.getCurrentAddressLocation(geoPoint)
    suspend fun searchLocation(key:String) = shippingAddressDataSource.searchLocation(key)
    suspend fun getShippingMethods(accountId:String) = shippingAddressDataSource.getShippingMethods(accountId)
    suspend fun updatePickupAddress(orderId: String,shippingAddress: ShippingAddress) = shippingAddressDataSource.updatePickupAddress(orderId,shippingAddress)
}