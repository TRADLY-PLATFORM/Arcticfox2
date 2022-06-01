package tradly.social.domain.dataSource

import tradly.social.domain.entities.EventBooking
import tradly.social.domain.entities.Result

interface EventBookingDataSource {
    suspend fun confirmBooking(listingId:String,variantId:Int,paymentMethodId:Int,quantity:Int,type:String): Result<String>
    suspend fun getBookingEvents(pageNo: String,accountId:String,type:String):Result<List<EventBooking>>
    suspend fun getBookingDetail(orderId:String,accountId:String?):Result<EventBooking>
}