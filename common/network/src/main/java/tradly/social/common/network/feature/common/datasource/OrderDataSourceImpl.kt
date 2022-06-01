package tradly.social.common.network.feature.common.datasource

import tradly.social.common.network.converters.OrderConverter
import tradly.social.common.network.CustomError
import tradly.social.common.network.NetworkConstant
import tradly.social.common.network.NetworkError
import tradly.social.common.network.base.AppConstant
import tradly.social.common.network.base.BaseService
import tradly.social.common.network.retrofit.OrderAPI
import tradly.social.domain.dataSource.OrderDataSource
import tradly.social.domain.entities.*

class OrderDataSourceImpl : OrderDataSource,BaseService() {

    val apiService = getRetrofitService(OrderAPI::class.java)

    override fun getOrderDetail(orderId: String, accountId:String): Result<OrderDetail> =
        when (val result = apiCall(apiService.getOrderDetails(orderId,if(accountId.isEmpty())null else accountId))) {
            is Result.Success -> Result.Success(OrderConverter.mapFrom(result.data.responseData.order))
            is Result.Error -> result
        }

    override fun getOrders(pageNo: String,accountId:String,startDate:String,endDate:String,orderStatus: List<Int>,orderReference:String,type:String): Result<List<Order>> =
        when (val result = apiCall(apiService.getOrders(getOrderListQuery(pageNo,accountId, startDate, endDate,orderStatus,orderReference,type)))) {
            is Result.Success -> Result.Success(OrderConverter.mapFrom(result.data.data.orders))
            is Result.Error -> result
        }

    override fun cancelOrder(orderReference: String,orderStatus: Int): Result<Boolean> {
        val result = getOrders("1",Constant.EMPTY,Constant.EMPTY,Constant.EMPTY, listOf(),orderReference,AppConstant.EMPTY)
        return when(result){
            is Result.Success->{
                if(result.data.isNotEmpty()){
                    updateStatus(result.data[0].id,orderStatus)
                }
                else{
                    Result.Error(exception = AppError(code = CustomError.NOT_ABLE_TO_FETCH_ORDERS_ON_CANCEL_ORDER))
                }
            }
            is Result.Error->result
        }
    }

    override fun checkOutCart(shippingAddressId: Int, paymentTypeId: Int,shippingMethodId: Int) =
        when (val result = apiCall(apiService.checkOutCart(getRequestBody(shippingAddressId, paymentTypeId,shippingMethodId)))) {
            is Result.Success -> if(result.data.status){
                Result.Success(result.data.checkoutData.orderReference)
            }
            else{
                Result.Error(exception = AppError(code = CustomError.CHECKOUT_FAILED))
            }
            is Result.Error -> result
        }


    override fun updateStatus(orderId: String,status: Int) = when(val result = apiCall(apiService.updateStatus(orderId,getUpdateStatusBody(status)))){
        is Result.Success->{
            if(result.data.status){
                Result.Success(result.data.status)
            }
            else{
                Result.Error(AppError(code = CustomError.UPDATE_STATUS_FAILED))
            }
        }
        is Result.Error->result
    }


    private fun getRequestBody(shippingAddressId: Int, paymentTypeId: Int, shippingMethodId:Int): HashMap<String, Any> {
        val body = hashMapOf<String, Any>()
        if(shippingAddressId!=0)
        {
            body["shipping_address_id"] = shippingAddressId
            body["billing_address_id"] = shippingAddressId
        }
        body["payment_method_id"] = paymentTypeId
        body["shipping_method_id"] = shippingMethodId
        return hashMapOf("order" to body)
    }

    private fun getOrderListQuery(pageNo: String,accountId:String,startDate:String,endDate:String,orderStatus:List<Int>,orderReference:String,type:String): HashMap<String, Any> {
        val map = hashMapOf<String, Any>()
        map["page"] = pageNo

        if(accountId.isNotEmpty()){
            map["account_id"] = accountId
        }
        if(startDate.isNotEmpty()){
            map["start_date"] = startDate
            map["end_date"] = endDate
        }
        if(orderStatus.isNotEmpty()){
            map["order_status"] = orderStatus.joinToString(",")
        }
        if (type.isNotEmpty()){
            map["type"] = type
        }
        if (orderReference.isNotEmpty()){
            map["order_reference"] = orderReference
        }
        else{
            map["per_page"] = AppConstant.LIST_PER_PAGE
        }
        return map
    }



    private fun getUpdateStatusBody(status:Int):HashMap<String,Any?>{
        val map = hashMapOf<String, Any?>()
        map["status"] = status
        return hashMapOf("order" to map)
    }
}