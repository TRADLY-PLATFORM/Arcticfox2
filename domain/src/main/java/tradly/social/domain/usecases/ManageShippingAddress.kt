package tradly.social.domain.usecases

import tradly.social.domain.entities.Constant
import tradly.social.domain.entities.GeoPoint
import tradly.social.domain.entities.ShippingAddress
import tradly.social.domain.repository.ShippingAddressRepository

class ManageShippingAddress(val shippingAddressRepository: ShippingAddressRepository) {
    suspend fun addShippingAddress(shippingAddress: ShippingAddress) = shippingAddressRepository.addShippingAddress(shippingAddress)
    suspend fun getCurrentAddress(shippingType: String) = shippingAddressRepository.getCurrentAddress(shippingType)
    suspend fun getShippingAddresses(shippingType:String) = shippingAddressRepository.getShippingAddresses(shippingType)
    suspend fun changePresentAddress(currentId:String,newId:String) = shippingAddressRepository.changePresentAddress(currentId,newId)
    suspend fun updateShippingAddress(address: ShippingAddress) = shippingAddressRepository.updateShippingAddress(address)
    suspend fun updatePickupAddress(orderId: String,shippingAddress: ShippingAddress) = shippingAddressRepository.updatePickupAddress(orderId,shippingAddress)
    suspend fun getCurrentAddressLocation(geoPoint: GeoPoint) = shippingAddressRepository.getCurrentAddressLocation(geoPoint)
    suspend fun searchLocation(key:String) = shippingAddressRepository.searchLocation(key)
    suspend fun getShippingMethods(accountId:String=Constant.EMPTY) = shippingAddressRepository.getShippingMethods(accountId)
}