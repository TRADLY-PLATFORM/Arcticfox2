package tradly.social.domain.usecases

import tradly.social.domain.repository.OrderRepository

class CheckOutCartUc(private val orderRepository: OrderRepository){
    suspend fun checkOutOrder(shippingAddressId:Int,paymentTypeId:Int,shippingMethodId:Int) = orderRepository.checkOutOrder(shippingAddressId, paymentTypeId,shippingMethodId)
}