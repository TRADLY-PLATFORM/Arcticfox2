package tradly.social.ui.search

import tradly.social.common.base.NetworkUtil
import tradly.social.common.network.feature.common.datasource.ProductDataSourceImpl
import tradly.social.domain.entities.*
import tradly.social.domain.repository.ProductRepository
import tradly.social.domain.usecases.GetProducts
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class SearchPresenter(var view: View?) : CoroutineScope {
    private var job: Job
    private var getProducts: GetProducts? = null

    interface View {
        fun noNetwork()
        fun onLoadList(productList: List<Product>, isLoadMore: Boolean)
        fun onFailure(appError: AppError)
        fun hideProgressLoader()
        fun showProgressLoader()
    }

    init {
        val parseProductDataSource = ProductDataSourceImpl()
        val productRepository = ProductRepository(parseProductDataSource)
        getProducts = GetProducts(productRepository)
        job = Job()
    }
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job


    fun search(categoryId:String?=Constant.EMPTY, sort:String?, keyword:String?, isLoadMore: Boolean, pagination:Int=0,shouldShowSearch:Boolean,latitude:Double,longitude:Double,filterRadius:String){
        if(NetworkUtil.isConnectingToInternet() && !keyword.isNullOrEmpty()){
            if(shouldShowSearch){
                view?.showProgressLoader()
            }
            launch(Dispatchers.Main) {
                val call = async(Dispatchers.IO){ getProducts?.getProducts(categoryId,sort = sort,key = keyword,pagination = pagination,latitude = latitude,longitude = longitude,filterRadius = filterRadius ) }
                when(val result = call.await()){
                    is Result.Success->{ view?.onLoadList(result.data,isLoadMore)}
                    is Result.Error->{view?.onFailure(result.exception)}
                }
                view?.hideProgressLoader()
            }
        }
    }
}