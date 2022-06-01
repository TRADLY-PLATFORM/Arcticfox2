package tradly.social.search.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import tradly.social.common.base.AppConstant
import tradly.social.common.base.BaseViewModel
import tradly.social.common.base.SingleEvent
import tradly.social.common.base.UIState
import tradly.social.common.network.NetworkConstant
import tradly.social.common.network.RequestID
import tradly.social.common.network.feature.common.datasource.ProductDataSourceImpl
import tradly.social.common.network.feature.common.datasource.StoreDataSourceImpl
import tradly.social.domain.entities.AppError
import tradly.social.domain.entities.Event
import tradly.social.domain.entities.Product
import tradly.social.domain.entities.Store
import tradly.social.domain.repository.ProductRepository
import tradly.social.domain.repository.StoreRepository
import tradly.social.domain.usecases.FollowStore
import tradly.social.domain.usecases.GetProducts
import tradly.social.domain.usecases.GetStores

class SearchViewModel :BaseViewModel(){

    private val storeRepository by lazy { StoreRepository(StoreDataSourceImpl()) }
    private val getProducts = GetProducts(ProductRepository(ProductDataSourceImpl()))
    private val getStores = GetStores(storeRepository)
    private val followStore = FollowStore(storeRepository)
    private val _productListLiveData by lazy { MutableLiveData<Triple<Boolean,Int,List<Product>>>() }
    val productListLiveData:LiveData<Triple<Boolean,Int,List<Product>>> = _productListLiveData

    private val _storeListLiveData by lazy { MutableLiveData<Triple<Boolean,Int,List<Store>>>() }
    val storeListLiveData:LiveData<Triple<Boolean,Int,List<Store>>> = _storeListLiveData

    fun getSearchList(searchBy:Int,isLoadMore:Boolean,pagination:Int,queryMaps:HashMap<String,Any?>){
        when(searchBy){
            AppConstant.FilterType.PRODUCTS -> getProducts(isLoadMore, pagination, queryMaps)
            AppConstant.FilterType.ACCOUNTS -> getStores(isLoadMore, pagination, queryMaps)
        }
    }

    private fun getProducts(isLoadMore:Boolean,pagination:Int,queryMaps:HashMap<String,Any?>){
        getApiResult(viewModelScope,{getProducts.getProducts(queryMaps)},{
            _productListLiveData.value = Triple(isLoadMore,if (it.isNotEmpty()) pagination+1 else pagination,it)
        },!isLoadMore,RequestID.GET_PRODUCTS)
    }

    private fun getStores(isLoadMore:Boolean,pagination:Int,queryMaps:HashMap<String,Any?>){
        queryMaps[NetworkConstant.QueryParam.TYPE] = NetworkConstant.Param.ACCOUNT
        getApiResult(viewModelScope,{getStores.getStores(queryMaps)},{
            _storeListLiveData.value = Triple(isLoadMore,if (it.isNotEmpty()) pagination+1 else pagination,it)
        },!isLoadMore,RequestID.GET_STORES)
    }

    fun followStore(storeId:String){
        getApiResult(viewModelScope,{followStore.followStore(storeId)},apiId = RequestID.FOLLOW_STORE)
    }

    fun unFollowStore(storeId:String){
        getApiResult(viewModelScope,{followStore.unfollowStore(storeId)},apiId = RequestID.UN_FOLLOW_STORE)
    }

    fun setProductList(list: List<Product>){
        _productListLiveData.value = Triple(false,1,list)
    }

    fun setStoreList(list: List<Store>){
        _storeListLiveData.value = Triple(false,1,list)
    }

    override fun showAPIProgress(show: Boolean, apiId: Int) {
        _uiState.value = SingleEvent(UIState.Loading(show,apiId))
    }

    override fun onFailure(apiError: AppError) {
        showAPIProgress(false,apiError.apiId)
        _uiState.value = SingleEvent(UIState.Failure(apiError))
    }
}