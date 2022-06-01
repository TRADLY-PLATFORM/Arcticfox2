package tradly.social.common.filter

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import tradly.social.common.base.AppConstant
import tradly.social.common.base.BaseViewModel
import tradly.social.common.base.SingleEvent
import tradly.social.common.base.UIState
import tradly.social.common.network.RequestID
import tradly.social.common.network.feature.common.datasource.StoreDataSourceImpl
import tradly.social.common.network.feature.common.datasource.ProductDataSourceImpl
import tradly.social.common.network.feature.exploreEvent.datasource.EventDataSourceImpl

import tradly.social.domain.entities.AppError
import tradly.social.domain.entities.Event
import tradly.social.domain.entities.Product
import tradly.social.domain.entities.Store
import tradly.social.domain.repository.EventRepository
import tradly.social.domain.repository.ProductRepository
import tradly.social.domain.repository.StoreRepository
import tradly.social.domain.usecases.*

class FilterViewModel :BaseViewModel(){

    private val getStoresUc = GetStores(StoreRepository(StoreDataSourceImpl()))
    private val getProductsUc = GetProducts(ProductRepository(ProductDataSourceImpl()))
    private val getEventsUC = GetEventsUc(EventRepository(EventDataSourceImpl()))

    private val _onFilterEventListLiveData by lazy { MutableLiveData<List<Event>>() }
    val onFilterEventListLiveData get() = _onFilterEventListLiveData
    private val _onFilterStoreListLiveData by lazy { MutableLiveData<List<Store>>() }
    val onFilterStoreListLiveData get() = _onFilterStoreListLiveData
    private val _onFilterProductListLiveData by lazy { MutableLiveData<List<Product>>() }
    val onFilterProductListLiveData get() = _onFilterProductListLiveData

    var filterType:Int = 0

    fun getFilterResult(queryParamMap:HashMap<String,Any?>){
        when(filterType){
            AppConstant.FilterType.EVENTS-> getEvents(queryParamMap)
            AppConstant.FilterType.ACCOUNTS -> getStores(queryParamMap)
            AppConstant.FilterType.PRODUCTS -> getProducts(queryParamMap)
        }
    }

    private fun getEvents(queryParamMap:HashMap<String,Any?>){
        getApiResult(viewModelScope,{getEventsUC.getEvents(queryParamMap)},{
            _onFilterEventListLiveData.value = it
        },true, RequestID.GET_EVENTS)
    }

    private fun getStores(queryParamMap:HashMap<String,Any?>){
        getApiResult(viewModelScope,{getStoresUc.getStores(queryParamMap)},{
            _onFilterStoreListLiveData.value = it
        },true, RequestID.GET_STORES)
    }

    private fun getProducts(queryParamMap:HashMap<String,Any?>){
        getApiResult(viewModelScope,{getProductsUc.getProducts(queryParamMap)},{
            _onFilterProductListLiveData.value = it
        },true, RequestID.GET_PRODUCTS)
    }

    override fun showAPIProgress(show: Boolean, apiId: Int) {
        _uiState.value = SingleEvent(UIState.Loading(show,apiId))
    }
    override fun onFailure(apiError: AppError) {
        showAPIProgress(false,apiError.apiId)
        _uiState.value = SingleEvent(UIState.Failure(apiError))
    }
}