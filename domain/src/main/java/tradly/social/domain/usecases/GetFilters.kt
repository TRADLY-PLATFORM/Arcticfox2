package tradly.social.domain.usecases

import tradly.social.domain.repository.EventRepository

class GetFilters(private val eventRepository: EventRepository) {
    suspend fun getFilters() = eventRepository.getFilters()
}