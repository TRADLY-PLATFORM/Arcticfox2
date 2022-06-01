package tradly.social.ui.product.addProduct.productList

import tradly.social.common.base.NetworkUtil
import tradly.social.common.network.feature.common.datasource.ProductDataSourceImpl
import tradly.social.domain.entities.AppError
import tradly.social.domain.entities.Result
import tradly.social.domain.repository.ProductRepository
import tradly.social.domain.usecases.GetProducts
import kotlinx.coroutines.*
import tradly.social.data.model.CoroutinesManager
import tradly.social.common.network.feature.common.datasource.StoreDataSourceImpl
import tradly.social.domain.entities.Constant
import tradly.social.domain.repository.StoreRepository
import tradly.social.domain.usecases.GetStores
import tradly.social.domain.usecases.ManageStore
import tradly.social.ui.groupListing.ListingPresenter

class ProductListPresenter(var view: View?):ListingPresenter(null) {
    private var job: Job
    private var getProducts: GetProducts
    private var getStores: GetStores
    private var manageStore:ManageStore
    private var coroutineScope: CoroutineScope

    interface View {
        fun noNetwork()
        fun onLoadList(any: Any, isLoadMore: Boolean)
        fun onFailure(appError: AppError)
        fun hideProgressLoader()
        fun showProgressLoader()
        fun onStoreBlockStatus(storeId:String)
        fun showProgressDialog()
        fun hideProgressDialog()
    }

    init {
        val parseProductDataSource = ProductDataSourceImpl()
        val productRepository = ProductRepository(parseProductDataSource)
        val parseStoreDataSource = StoreDataSourceImpl()
        val storeRepository = StoreRepository(parseStoreDataSource)
        getStores = GetStores(storeRepository)
        getProducts = GetProducts(productRepository)
        manageStore = ManageStore(storeRepository)
        job = SupervisorJob()
        coroutineScope = CoroutineScope(Dispatchers.Main + job)
    }


    fun getProductList(
        categoryId: String?,
        sort: String?,
        pagination: Int = 0,
        isLoadMore: Boolean,
        priceRangeFrom: Int = 0,
        priceRangeTo: Int = 0,
        key: String? = Constant.EMPTY,
        latitude: Double = 0.0,
        longitude: Double = 0.0,
        filterRadius: String = Constant.EMPTY,
        collectionId:String = Constant.EMPTY,
        allowLocation:Boolean = false
    ) {
        if (NetworkUtil.isConnectingToInternet(true)) {
            CoroutinesManager.ioThenMain(coroutineScope, {
                getProducts.getProducts(
                    categoryId = categoryId,
                    sort = sort,
                    pagination = pagination,
                    priceRangeFrom = priceRangeFrom,
                    priceRangeTo = priceRangeTo,
                    key = key,
                    latitude = latitude,
                    longitude = longitude,
                    filterRadius = filterRadius,
                    collectionId = collectionId,
                    allowLocation = allowLocation
                )
            }, { result ->
                view?.hideProgressLoader()
                when (result) {
                    is Result.Success -> view?.onLoadList(result.data, isLoadMore)
                    is Result.Error -> view?.onFailure(result.exception)
                }
            })
        }
    }

    fun getStoreList(pagination: Int, isLoadMore: Boolean, sort: String, searchKey: String) {
        CoroutinesManager.ioThenMain(coroutineScope,
            { getStores.getStores(pagination, searchKey = searchKey, sort = sort) },
            { result ->
                view?.hideProgressLoader()
                when (result) {
                    is Result.Success -> view?.onLoadList(result.data, isLoadMore)
                    is Result.Error -> view?.onFailure(result.exception)
                }
            })
    }

    fun getStoreProducts(storeId: String? , isLoadMore: Boolean , pagination:Int){
        CoroutinesManager.ioThenMain(coroutineScope,{getProducts.getStoreProducts(storeId,pagination)},{result->
            view?.hideProgressLoader()
            when (result) {
                is Result.Success -> view?.onLoadList(result.data, isLoadMore)
                is Result.Error -> view?.onFailure(result.exception)
            }
        })
    }

    fun getStoreFeeds(type:String,pagination: Int,isLoadMore: Boolean){
        if (NetworkUtil.isConnectingToInternet()) {
            launch(Dispatchers.Main){
                val call = async(Dispatchers.IO){ getStores.getStoreFeeds(type, pagination) }
                when(val result = call.await()){
                    is Result.Success->view?.onLoadList(result.data, isLoadMore)
                    is Result.Error -> view?.onFailure(result.exception)
                }
                view?.hideProgressLoader()
            }
        }
    }

    fun unBlockStore(storeId: String){
        if(NetworkUtil.isConnectingToInternet()){
            view?.showProgressDialog()
            launch(Dispatchers.Main){
                val call = async(Dispatchers.IO){ manageStore.unBlockStore(storeId) }
                when(val result = call.await()){
                    is Result.Success->  view?.onStoreBlockStatus(storeId)
                    is Result.Error->view?.onFailure(result.exception)
                }
                view?.hideProgressDialog()
            }
        }
    }

    fun getWishList(pagination: Int, isLoadMore: Boolean) {
        if (NetworkUtil.isConnectingToInternet()) {
            coroutineScope.launch(Dispatchers.Main) {
                val call = async(Dispatchers.IO) { getProducts.getWishList(pagination) }
                when (val result = call.await()) {
                    is Result.Success -> {
                        view?.onLoadList(result.data, isLoadMore)
                    }
                    is Result.Error -> {
                        view?.onFailure(result.exception)
                    }
                }
                view?.hideProgressLoader()
            }
        } else {
            view?.noNetwork()
        }
    }

    override fun onDestroy() {
        view = null
        job.cancelChildren()
    }
}