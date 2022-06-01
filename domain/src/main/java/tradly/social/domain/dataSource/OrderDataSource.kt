package tradly.social.domain.dataSource

import tradly.social.domain.entities.EventBooking
import tradly.social.domain.entities.Order
import tradly.social.domain.entities.OrderDetail
import tradly.social.domain.entities.Result

interface OrderDataSource{
    fun checkOutCart(shippingAddressId:Int,paymentTypeId:Int,shippingMethodId:Int):Result<String>
    fun getOrders(pageNo: String,accountId:String,startDate:String,endDate:String,orderStatus: List<Int>,orderReference:String,type:String): Result<List<Order>>
    fun getOrderDetail(orderId: String, accountId:String): Result<OrderDetail>
    fun updateStatus(orderId: String,status:Int):Result<Boolean>
    fun cancelOrder(orderReference: String,orderStatus:Int):Result<Boolean>
}