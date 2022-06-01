package tradly.social.domain.usecases

import tradly.social.domain.repository.OrderRepository

class UpdateOrderStatus(private val orderRepository: OrderRepository){
    suspend operator fun invoke(orderId: String,status:Int) = orderRepository.updateStatus(orderId,status)
}