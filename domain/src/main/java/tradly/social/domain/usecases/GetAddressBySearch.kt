package tradly.social.domain.usecases

import tradly.social.domain.repository.ShippingAddressRepository

class GetAddressBySearch(private val shippingAddressRepository: ShippingAddressRepository) {
    suspend fun search(key:String) = shippingAddressRepository.searchLocation(key)
}