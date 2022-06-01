package tradly.social.ui.socialFeed

import tradly.social.common.base.AppConstant
import tradly.social.common.base.NetworkUtil
import tradly.social.common.network.feature.common.datasource.ProductDataSourceImpl
import tradly.social.domain.entities.AppError
import tradly.social.domain.entities.Product
import tradly.social.domain.entities.Result
import tradly.social.domain.repository.ProductRepository
import tradly.social.domain.usecases.GetProducts
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class SocialFeedPresenter(var view:View?): CoroutineScope {

    interface View {
        fun noNetwork()
        fun onLoadList(productList: List<Product>, isLoadMore: Boolean)
        fun onFailure(appError: AppError)
        fun hideProgressLoader()
    }

    private var job: Job
    private var getProducts: GetProducts? = null

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    init {
        val parseProductDataSource = ProductDataSourceImpl()
        val productRepository = ProductRepository(parseProductDataSource)
        getProducts = GetProducts(productRepository)
        job = Job()
    }


    fun getSocialFeed(pagination: Int = 0, isLoadMore: Boolean){
        if(NetworkUtil.isConnectingToInternet()){
            launch(Dispatchers.Main){
                val call = async(Dispatchers.IO){ getProducts?.getSocialFeeds(AppConstant.SocialFeedTypes.LISTING,pagination) }
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
        }
    }

    fun likeProduct(productId:String,isForLike:Boolean){
        launch(Dispatchers.IO){ getProducts?.likeProduct(productId,isForLike) }
    }

    fun onDestroy(){
        view = null
        job.cancel()
    }
}