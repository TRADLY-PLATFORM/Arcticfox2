package tradly.social.event.explore.ui.filter.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import tradly.social.common.base.BaseViewModel
import tradly.social.common.base.SingleEvent
import tradly.social.common.base.UIState
import tradly.social.common.network.RequestID
import tradly.social.common.network.feature.common.datasource.ProductDataSourceImpl
import tradly.social.common.network.feature.exploreEvent.datasource.EventDataSourceImpl
import tradly.social.domain.entities.*
import tradly.social.domain.repository.EventRepository
import tradly.social.domain.repository.ProductRepository
import tradly.social.domain.usecases.*

class EventExploreViewModel:BaseViewModel() {

    private val getEventsUC = GetEventsUc(EventRepository(EventDataSourceImpl()))
    private val getProduct = GetProducts(ProductRepository(ProductDataSourceImpl()))

    private var isLoading:Boolean = false

    private val _showCurrentLocation by lazy { MutableLiveData<SingleEvent<Unit>>() }
    val showCurrentLocation:LiveData<SingleEvent<Unit>> = _showCurrentLocation

    private val _fragmentNavigation by lazy { MutableLiveData<SingleEvent<String>>() }
    val fragmentNavigation:LiveData<SingleEvent<String>> = _fragmentNavigation

    private val _fragmentPopup by lazy { MutableLiveData<SingleEvent<Unit>>() }
    val fragmentPopup:LiveData<SingleEvent<Unit>> = _fragmentPopup

    private val _eventListLiveData by lazy { MutableLiveData<Triple<Boolean,Int,List<Event>>>() }
    val eventListLiveData:LiveData<Triple<Boolean,Int,List<Event>>> = _eventListLiveData

    private val _refreshData by lazy { MutableLiveData<SingleEvent<Unit>>() }
    val refreshData:LiveData<SingleEvent<Unit>> = _refreshData

    private var _currentlySelectedCalenderPosition:Int = 0

    fun getEvents(isLoadMore:Boolean,pagination:Int,queryParamMap:HashMap<String,Any?>){
        getApiResult(viewModelScope,{getEventsUC.getEvents(queryParamMap)},{ list->
            _eventListLiveData.value  = Triple(isLoadMore,if (list.isEmpty())pagination else pagination+1,list)
        },true,RequestID.GET_EVENTS)
    }

    fun likeEvent(eventId:String,isLike:Boolean){
        getApiResult(viewModelScope,{getProduct.likeProduct(eventId,isLike)},apiId = RequestID.NONE)
    }

    fun setEvents(triple:Triple<Boolean,Int,List<Event>>?){
        _eventListLiveData.value = triple
    }

    fun setLoading(loading:Boolean){
        this.isLoading = loading
    }

    fun getPagination() = this.eventListLiveData.value!!.second

    fun isLoading() = this.isLoading

    fun getEventsLiveData() = this._eventListLiveData.value

    fun setRefreshData(){
        this._refreshData.value = SingleEvent(Unit)
    }

    fun setSelectedCalenderPosition(pos:Int){
        this._currentlySelectedCalenderPosition = pos
    }

    fun getSelectedCalenderPosition() = this._currentlySelectedCalenderPosition

    fun showCurrentLocation(){
        this._showCurrentLocation.value = SingleEvent(Unit)
    }

    fun setFragmentNavigation(tag:String){
        _fragmentNavigation.value = SingleEvent(tag)
    }

    fun setFragmentPopup(){
        _fragmentPopup.value = SingleEvent(Unit)
    }

    override fun showAPIProgress(show: Boolean, apiId: Int) {
        _uiState.value = SingleEvent(UIState.Loading(show,apiId))
    }
    override fun onFailure(apiError: AppError) {
        showAPIProgress(false,apiError.apiId)
        _uiState.value = SingleEvent(UIState.Failure(apiError))
    }
}