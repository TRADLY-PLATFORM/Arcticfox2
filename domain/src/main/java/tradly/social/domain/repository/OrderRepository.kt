package tradly.social.domain.repository

import tradly.social.domain.dataSource.OrderDataSource
import tradly.social.domain.entities.Order
import tradly.social.domain.entities.OrderDetail
import tradly.social.domain.entities.Result

class OrderRepository(private val orderDataSource: OrderDataSource){
    suspend fun checkOutOrder(shippingAddressId:Int,paymentTypeId:Int,shippingMethodId:Int):Result<String> = orderDataSource.checkOutCart(shippingAddressId, paymentTypeId,shippingMethodId)
    suspend fun getOrders(pageNo: String,accountId:String,startDate:String,endDate:String,orderStatus: List<Int>,orderReference:String,type:String): Result<List<Order>> = orderDataSource.getOrders(pageNo,accountId,startDate, endDate,orderStatus,orderReference,type)
    suspend fun getOrderDetails(orderId: String,accountId:String): Result<OrderDetail> = orderDataSource.getOrderDetail(orderId,accountId)
    suspend fun updateStatus(orderId: String,status:Int):Result<Boolean> = orderDataSource.updateStatus(orderId,status)
    suspend fun cancelOrder(orderReference: String,orderStatus:Int) = orderDataSource.cancelOrder(orderReference,orderStatus)
}