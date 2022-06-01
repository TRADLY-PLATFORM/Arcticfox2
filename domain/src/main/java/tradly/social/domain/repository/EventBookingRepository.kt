package tradly.social.domain.repository

import tradly.social.domain.dataSource.EventBookingDataSource


class EventBookingRepository(private val eventBookingDataSource: EventBookingDataSource) {
    suspend fun confirmBooking(listingId:String,variantId:Int,paymentMethodId:Int,quantity:Int,type:String) = eventBookingDataSource.confirmBooking(listingId,variantId, paymentMethodId, quantity, type)
    suspend fun getBookingEvents(pageNo: String,accountId:String,type:String) = eventBookingDataSource.getBookingEvents(pageNo, accountId, type)
    suspend fun getBookingDetail(orderId:String,accountId:String?) = eventBookingDataSource.getBookingDetail(orderId, accountId)
}