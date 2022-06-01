package tradly.social.domain.usecases

import tradly.social.domain.entities.Constant.Companion.EMPTY
import tradly.social.domain.repository.OrderRepository

class GetOrderUc(private val orderRepository: OrderRepository){
    suspend fun getOrders(pageNo: String,accountId:String = EMPTY,startDate:String = EMPTY,endDate:String = EMPTY,orderStatus:List<Int>,orderReference:String = EMPTY,type:String = EMPTY) = orderRepository.getOrders(pageNo,accountId,startDate, endDate,orderStatus,orderReference,type)

    suspend fun cancelOrder(orderReference: String,orderStatus: Int) = orderRepository.cancelOrder(orderReference,orderStatus)
}