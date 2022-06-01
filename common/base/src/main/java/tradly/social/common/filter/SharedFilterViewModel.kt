package tradly.social.common.filter

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import tradly.social.common.base.AppConstant
import tradly.social.common.base.BaseViewModel
import tradly.social.common.base.SingleEvent
import tradly.social.common.base.UIState
import tradly.social.common.network.NetworkConstant
import tradly.social.common.network.RequestID
import tradly.social.common.network.feature.common.datasource.CategoryDataSourceImpl
import tradly.social.common.network.feature.exploreEvent.datasource.EventDataSourceImpl
import tradly.social.domain.entities.*
import tradly.social.domain.repository.CategoryRepository
import tradly.social.domain.repository.EventRepository
import tradly.social.domain.usecases.*

class SharedFilterViewModel:BaseViewModel() {

    private val getFilters = GetFilters(EventRepository(EventDataSourceImpl()))
    private val getCategory = GetCategories(CategoryRepository(CategoryDataSourceImpl()))

    private val queryMap by lazy { HashMap<String,Any?>() }
    private val finalQueryMap by lazy { HashMap<String,Any?>() }
    private val _selectedFilter by lazy { MutableLiveData<Filter>() }
    val selectedFilter: LiveData<Filter> = _selectedFilter
    private val _filterLiveData by lazy { MutableLiveData<List<Filter>>() }
    val filterLiveData:LiveData<List<Filter>> = _filterLiveData
    private val _onFilterApplyLiveData by lazy { MutableLiveData<SingleEvent<Unit>>() }
    val onFilterApplyLiveData: LiveData<SingleEvent<Unit>> = _onFilterApplyLiveData
    private val _onRefreshFilterList by lazy { MutableLiveData<SingleEvent<Unit>>() }
    val onRefreshFilterList:LiveData<SingleEvent<Unit>> = _onRefreshFilterList
    private val _onSortClickLiveData by lazy { MutableLiveData<SingleEvent<Unit>>() }
    val onSortClickLiveData:LiveData<SingleEvent<Unit>> = _onSortClickLiveData
    private val _fragmentNavigation by lazy { MutableLiveData<SingleEvent<String>>() }
    val fragmentNavigation:LiveData<SingleEvent<String>> = _fragmentNavigation
    private val _fragmentPopup by lazy { MutableLiveData<SingleEvent<Unit>>() }
    val fragmentPopup:LiveData<SingleEvent<Unit>> = _fragmentPopup
    private val queryNameMap by lazy { HashMap<String,String?>() }
    private val finalQueryNameMap by lazy { HashMap<String,String?>() }
    private val _categoryList by lazy { MutableLiveData<SingleEvent<List<Category>>>() }
    val categoryList:LiveData<SingleEvent<List<Category>>> = _categoryList

    private val _toolbarTitleLiveData by lazy { MutableLiveData<SingleEvent<String>>() }
    val toolbarTitleLiveData:LiveData<SingleEvent<String>> = _toolbarTitleLiveData

    private var eventList:List<Event> = listOf()
    private var storeList:List<Store> = listOf()
    private var productList:List<Product> = listOf()

    private var finalEventList:MutableList<Event> = mutableListOf()
    private var finalStoreList:MutableList<Store> = mutableListOf()
    private var finalProductList:MutableList<Product> = mutableListOf()

    fun setToolbarTitle(title:String){
        _toolbarTitleLiveData.value = SingleEvent(title)
    }

    fun getFilterQueryMap() = this.queryMap

    fun getFinalFilterQueryMap() = this.finalQueryMap

    fun getFilterQueryNameMap() = this.queryNameMap

    fun getFinalFilterQueryNameMap() = this.finalQueryNameMap

    fun clearAllQuery(){
        this.finalQueryMap.clear()
        this.queryMap.clear()
        this.finalQueryNameMap.clear()
        this.queryNameMap.clear()
    }
    fun setQueryNameMap(key:String,value:String?){
        this.queryNameMap[key] = value
    }

    fun setFinalQueryNameMap(key:String,value:String?){
        this.finalQueryNameMap[key] = value
    }

    fun getFilterFinalEventList() = this.finalEventList
    fun getFilterFinalStoreList() = this.finalStoreList
    fun getFilterFinalProductList() = this.finalProductList

    fun setFinalList(){
        this.finalStoreList.clear()
        this.finalProductList.clear()
        this.finalEventList.clear()
        this.finalEventList.addAll(this.eventList)
        this.finalProductList.addAll(this.productList)
        this.finalStoreList.addAll(this.storeList)
    }

    fun setFilterEventList(list: List<Event>){
        eventList = list
    }

    fun setFilterStoreList(list: List<Store>){
        storeList = list
    }

    fun setFilterProductList(list: List<Product>){
        productList = list
    }

    fun setQuery(key:String,value:Any?){
        this.queryMap[key] = value
    }

    fun setFinalQuery(key:String,value:Any?){
        this.finalQueryMap[key] = value
    }

    fun setRefreshFilterList(){
        this._onRefreshFilterList.value = SingleEvent(Unit)
    }

    fun setFragmentNavigation(tag:String){
        _fragmentNavigation.value = SingleEvent(tag)
    }

    fun setFragmentPopup(){
        _fragmentPopup.value = SingleEvent(Unit)
    }

    fun setOnSortClick(){
        this._onSortClickLiveData.value = SingleEvent(Unit)
    }

    fun setFilterApply(){
        _onFilterApplyLiveData.value = SingleEvent(Unit)
    }

    fun getCategories(type:String){
        getApiResult(viewModelScope,{getCategory.getCategories(type = type)},{ list->
            _categoryList.value = SingleEvent(list)
        },true, RequestID.GET_CATEGORIES)
    }

    fun setCategories(list: List<Category>){
        _filterLiveData.value!!.find { it.viewType == FilterUtil.ViewType.MULTI_SELECT }?.let {
            it.categoryValues = list
        }
    }

    fun getFilters(){
        getApiResult(viewModelScope,{getFilters.getFilters()},{ list->
            list.toMutableList().let { mutableList->
                _filterLiveData.value = mutableList
            }
        },true,RequestID.GET_FILTERS)
    }

    fun clearQueryParam(){
        this.getFilterQueryMap().keys.retainAll(listOf(
            NetworkConstant.QueryParam.START_AT,
            NetworkConstant.QueryParam.END_AT,
            NetworkConstant.QueryParam.LATITUDE,
            NetworkConstant.QueryParam.LONGITUDE,
            NetworkConstant.QueryParam.PAGE,
            NetworkConstant.QueryParam.SEARCH_KEY,
            NetworkConstant.QueryParam.SORT
        ))
    }

    fun isFilterApplied() =
        this.finalQueryMap.filterNot {
            it.key == NetworkConstant.QueryParam.START_AT ||
            it.key ==  NetworkConstant.QueryParam.END_AT ||
            it.key ==  NetworkConstant.QueryParam.LATITUDE ||
            it.key ==  NetworkConstant.QueryParam.LONGITUDE ||
            it.key == NetworkConstant.QueryParam.PAGE ||
            it.key ==  NetworkConstant.QueryParam.SEARCH_KEY ||
            it.key == NetworkConstant.QueryParam.SORT
        }.isNotEmpty()


    fun setSelectedEventFilter(eventFilter: Filter){
        this._selectedFilter.value = eventFilter
    }

    override fun showAPIProgress(show: Boolean, apiId: Int) {
        _uiState.value = SingleEvent(UIState.Loading(show,apiId))
    }
    override fun onFailure(apiError: AppError) {
        showAPIProgress(false,apiError.apiId)
        _uiState.value = SingleEvent(UIState.Failure(apiError))
    }
}