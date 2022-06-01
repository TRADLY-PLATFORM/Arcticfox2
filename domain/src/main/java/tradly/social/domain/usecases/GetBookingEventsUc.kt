package tradly.social.domain.usecases

import tradly.social.domain.entities.Constant
import tradly.social.domain.repository.EventBookingRepository
import tradly.social.domain.repository.OrderRepository

class GetBookingEventsUc(private val eventBookingRepository: EventBookingRepository) {
    suspend fun getEventBookingListUc(pageNo: String,accountId:String,type:String) = eventBookingRepository.getBookingEvents(pageNo, accountId, type)
}