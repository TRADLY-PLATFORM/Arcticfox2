package tradly.social.domain.dataSource

import tradly.social.domain.entities.Event
import tradly.social.domain.entities.Filter
import tradly.social.domain.entities.GeoPoint
import tradly.social.domain.entities.Result

interface EventDataSource {
    suspend fun getEvents(queryParamsMap: HashMap<String,Any?>):Result<List<Event>>
    suspend fun getEvent(eventId:String):Result<Event>
    suspend fun getFilters():Result<List<Filter>>
}