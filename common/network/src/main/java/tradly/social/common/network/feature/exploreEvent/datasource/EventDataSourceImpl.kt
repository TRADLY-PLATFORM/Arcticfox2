package tradly.social.common.network.feature.exploreEvent.datasource

import tradly.social.common.network.NetworkConstant
import tradly.social.common.network.NetworkError
import tradly.social.common.network.base.BaseService
import tradly.social.common.network.converters.EventConverter
import tradly.social.common.network.converters.ProductConverter
import tradly.social.common.network.entity.ProductDetailResponse
import tradly.social.common.network.feature.exploreEvent.model.EventDetailResponse
import tradly.social.common.network.retrofit.EventAPI
import tradly.social.common.network.retrofit.RetrofitManager
import tradly.social.common.util.parser.extension.toObject
import tradly.social.domain.dataSource.EventDataSource
import tradly.social.domain.entities.*

class EventDataSourceImpl:EventDataSource,BaseService() {

    private val apiService = getRetrofitService(EventAPI::class.java)

    override suspend fun getEvents(
        queryParamsMap: HashMap<String,Any?>
    ): Result<List<Event>> =
        when (val result = RetrofitManager.getResponse(apiService.getEvents(queryParamsMap))) {
            is Result.Success-> if (result.data.status){
                Result.Success(EventConverter.mapFrom(result.data.dataListing.productEntity))
            }
            else{
                Result.Error(AppError(errorType = NetworkError.API_UNKNOWN_ERROR))
            }
            is Result.Error->result
    }

    override suspend fun getFilters(): Result<List<Filter>> {
        val list = mutableListOf(
            Filter(
                filterName = "Time",
                viewType = 1,
                minValue = 0,
                maxValue = 1440,
                queryKey = NetworkConstant.QueryParam.CREATED_FROM
            ),
            Filter(
                filterName = "Sort By",
                viewType = 4,
                subViewType = "sort_by",
                filterValue = arrayListOf(
                    FilterValue(filterName = "Price - Low to High",filterId = "price_low_to_high"),
                    FilterValue(filterName = "Price - High to Low",filterId = "price_high_to_low"),
                    FilterValue(filterName = "Popularity",filterId = "newest_first")
                ),
                queryKey = NetworkConstant.QueryParam.SORT
            ),
            /*EventFilter(
                filterName = "Date Posted",
                viewType = 2,
                filterValue = arrayListOf(
                    FilterValue(filterName = "Any Time"),
                    FilterValue(filterName = "Paste Year"),
                    FilterValue(filterName = "Paste Month"),
                    FilterValue(filterName = "Paste Week"),
                    FilterValue(filterName = "Paste 24 hours")
                )
            ),*/
            Filter(
                filterName = "Ratings",
                viewType = 6,
                filterValue = arrayListOf(
                    FilterValue(filterName = "5",filterId = "5"),
                    FilterValue(filterName = "4",filterId = "4"),
                    FilterValue(filterName = "3",filterId = "3"),
                    FilterValue(filterName = "2",filterId = "2"),
                    FilterValue(filterName = "1",filterId = "1")
                ),
                queryKey = NetworkConstant.QueryParam.RATING
            ),
            Filter(
                filterName = "Distance",
                viewType = 3,
                minValue = 0,
                maxValue = 100,
                unit = "KM",
                queryKey = NetworkConstant.QueryParam.MAX_DISTANCE
            ),
            Filter(
                filterName = "Category",
                viewType = 5,
                filterValue = arrayListOf(
                    FilterValue(filterName = "Online"),
                    FilterValue(filterName = "Clubs"),
                    FilterValue(filterName = "Regular Class"),
                    FilterValue(filterName = "Events Type")
                ),
                queryKey = NetworkConstant.QueryParam.CATEGORY_ID
            )
        )
        return Result.Success(list)
    }

    override suspend fun getEvent(eventId: String): Result<Event> {
      return when(val result = apiCall(apiService.getEvent(eventId))){
            is Result.Success->{
                val responseString = result.data.string()
                val eventDetailResponse = responseString.toObject<EventDetailResponse>()
                if (eventDetailResponse!=null){
                    val event = EventConverter.mapFrom(eventDetailResponse.listing.productEntity,responseString)
                    Result.Success(event)
                }
                else{
                    Result.Error(AppError(errorType = NetworkError.API_UNKNOWN_ERROR))
                }

            }
            is Result.Error-> result
        }
    }

}