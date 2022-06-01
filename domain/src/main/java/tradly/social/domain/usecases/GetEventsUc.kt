package tradly.social.domain.usecases

import tradly.social.domain.entities.GeoPoint
import tradly.social.domain.repository.EventRepository

class GetEventsUc(private val eventRepository: EventRepository) {
    suspend fun getEvents(queryParamsMap: HashMap<String,Any?>) = eventRepository.getEvents(queryParamsMap)
}