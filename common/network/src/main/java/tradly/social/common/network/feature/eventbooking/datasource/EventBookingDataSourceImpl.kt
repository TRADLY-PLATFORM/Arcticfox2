package tradly.social.common.network.feature.eventbooking.datasource

import tradly.social.common.network.NetworkConstant
import tradly.social.common.network.NetworkError
import tradly.social.common.network.base.AppConstant
import tradly.social.common.network.base.BaseService
import tradly.social.common.network.converters.EventBookingConverter
import tradly.social.common.network.retrofit.EventBookingAPI
import tradly.social.domain.dataSource.EventBookingDataSource
import tradly.social.domain.entities.AppError
import tradly.social.domain.entities.EventBooking
import tradly.social.domain.entities.Result

class EventBookingDataSourceImpl:EventBookingDataSource,BaseService() {

    private val apiService = getRetrofitService(EventBookingAPI::class.java)
    override suspend fun confirmBooking(
        listingId:String,
        variantId: Int,
        paymentMethodId: Int,
        quantity: Int,
        type: String
    ): Result<String> {
        return when(val result = apiCall(apiService.checkOutCart(getDirectCheckoutParams(variantId, paymentMethodId, quantity, type),listingId))){
            is Result.Success->{
                if (result.data.status){
                    Result.Success(result.data.checkoutData.orderReference)
                }
                else{
                    Result.Error(AppError(errorType = NetworkError.API_UNKNOWN_ERROR))
                }
            }
            is Result.Error -> result
        }
    }

    override suspend fun getBookingEvents(
        pageNo: String,
        accountId: String,
        type:String
    ): Result<List<EventBooking>> =
        when (val result = apiCall(apiService.getEventBookings(getEventListQuery(pageNo,accountId,type)))) {
            is Result.Success -> Result.Success(EventBookingConverter.mapFrom(result.data.data.eventBookingList))
            is Result.Error -> result
        }

    override suspend fun getBookingDetail(
        orderId: String,
        accountId: String?
    ): Result<EventBooking> {
        return when(val result = apiCall(apiService.getBookingDetail(orderId, accountId))){
            is Result.Success-> Result.Success(EventBookingConverter.mapFrom(result.data.data.eventBooking))
            is Result.Error -> result
        }
    }


    private fun getDirectCheckoutParams(variantId:Int,paymentMethodId:Int,quantity:Int,type:String):HashMap<String,Any?>{
        val map = hashMapOf<String, Any>()
        map[NetworkConstant.QueryParam.QUANTITY] = quantity
        map[NetworkConstant.QueryParam.PAYMENT_METHOD_ID] = paymentMethodId
        map[NetworkConstant.QueryParam.TYPE] = type
        if (variantId!=0){
            map[NetworkConstant.QueryParam.VARIANT_ID] = variantId
        }
        return hashMapOf(NetworkConstant.QueryParam.ORDER to map)
    }

    private fun getEventListQuery(pageNo: String,accountId:String,type:String): HashMap<String, Any> {
        val map = hashMapOf<String, Any>()
        map["page"] = pageNo
        map["per_page"] = AppConstant.LIST_PER_PAGE
        if(accountId.isNotEmpty()){
            map["account_id"] = accountId
        }
        if (type.isNotEmpty()){
            map["type"] = type
        }
        return map
    }



}