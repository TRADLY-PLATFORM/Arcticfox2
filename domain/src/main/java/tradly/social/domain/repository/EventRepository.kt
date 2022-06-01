package tradly.social.domain.repository

import tradly.social.domain.dataSource.EventDataSource
import tradly.social.domain.entities.GeoPoint

class EventRepository(private val eventDataSource: EventDataSource) {
    suspend fun getEvents(queryParamsMap: HashMap<String,Any?>) = eventDataSource.getEvents(queryParamsMap)
    suspend fun getFilters() = eventDataSource.getFilters()
    suspend fun getEvent(eventId:String) = eventDataSource.getEvent(eventId)
}