package tradly.social.domain.usecases

import tradly.social.domain.repository.OrderRepository

class GetOrderDetailUseCase(private val orderRepository: OrderRepository){
    suspend fun getOrderDetails(orderId: String,accountId:String) = orderRepository.getOrderDetails(orderId,accountId)
}