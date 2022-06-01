package tradly.social.domain.usecases

import tradly.social.domain.repository.EventRepository

class GetEventUc(private val eventRepository: EventRepository) {
    suspend fun getEvent(eventId:String) = eventRepository.getEvent(eventId)
}