package tradly.social.domain.usecases

import tradly.social.domain.repository.EventBookingRepository

class GetBookingDetailUc(private val bookingRepository: EventBookingRepository) {
    suspend fun getBookingDetail(orderId:String,accountId:String?) = bookingRepository.getBookingDetail(orderId,accountId)
}