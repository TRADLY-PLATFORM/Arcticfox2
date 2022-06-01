package tradly.social.ui.store.storeDetail

import tradly.social.R
import tradly.social.common.base.NetworkUtil
import tradly.social.common.base.Utils
import tradly.social.domain.entities.*
import kotlinx.coroutines.*
import tradly.social.common.base.AppConstant
import tradly.social.common.base.FileHelper
import tradly.social.common.base.ReviewHelper
import tradly.social.common.cache.AppCache
import tradly.social.common.network.CustomError
import tradly.social.common.network.feature.common.datasource.ParseMediaDataSource
import tradly.social.common.network.feature.common.datasource.StoreDataSourceImpl
import tradly.social.common.network.feature.common.datasource.PaymentDataSourceImpl
import tradly.social.common.network.feature.common.datasource.ProductDataSourceImpl
import tradly.social.domain.entities.ImageFeed
import tradly.social.data.model.dataSource.*
import tradly.social.domain.repository.*
import tradly.social.domain.usecases.*
import kotlin.coroutines.CoroutineContext

class StorePresenter(var view:View?) : CoroutineScope {
    private var job: Job
    private var getStore:GetStores
    private var followStore:FollowStore?=null
    private var getProducts:GetProducts
    private var getProduct: GetProduct
    private var manageStore:ManageStore
    private var getReviews:GetReviews
    private var uploadMedia:UploadMedia
    private var sendSubscribeMail:SendSubscribeMail
    interface View{
        fun onFailure(appError: AppError)
        fun noInternet()
        fun hideProgressLoader()
        fun showProgressLoader()
        fun onSuccess(result:Store)
        fun storeNotFound()
        fun onStoreBlockStatus(status:Boolean)
        fun showProgressDialog(msg:Int)
        fun hideProgressDialog()
        fun loadStoreProducts(shouldRefresh:Boolean)
        fun productDeleted(productId: String)
        fun productMarkedAsSold(productId: String)
        fun loadStoreReview(rating: Rating)
        fun onMailTriggerSuccess()
        fun showActivationStatus(status:Boolean)
    }

    init {
        val parseStoreDataSource = StoreDataSourceImpl()
        val storeRepository = StoreRepository(parseStoreDataSource)
        val parseProductDataSource = ProductDataSourceImpl()
        val productRepository = ProductRepository(parseProductDataSource)
        val reviewRepository = ReviewRepository(ParseReviewDataSource())
        val mediaDataSource = ParseMediaDataSource()
        val mediaRepository = MediaRepository(mediaDataSource)
        val paymentDataSource = PaymentDataSourceImpl()
        val paymentRepository = PaymentRepository(paymentDataSource)
        sendSubscribeMail = SendSubscribeMail(paymentRepository)
        getProducts = GetProducts(productRepository)
        getStore = GetStores(storeRepository)
        followStore = FollowStore(storeRepository)
        getProduct = GetProduct(productRepository)
        manageStore = ManageStore(storeRepository)
        getReviews = GetReviews(reviewRepository)
        uploadMedia = UploadMedia(mediaRepository)
        job = SupervisorJob()
    }
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    fun getStore(isUserStore:Boolean = false, storeId:String? ,shouldGetStoreProduct:Boolean = true){
        if(NetworkUtil.isConnectingToInternet()){
            launch(Dispatchers.Main) {
                val storeCall = async(Dispatchers.IO){ getStore.getStore(storeId) }
                when(val result = storeCall.await()){
                    is Result.Success-> {
                        view?.onSuccess(result.data)
                        view?.loadStoreProducts(shouldGetStoreProduct)
                    }
                    is Result.Error->{view?.onFailure(result.exception)}
                }

                if(isUserStore){
                    syncUserStore()
                }
            }
        }
        else{
            view?.noInternet()
        }
    }

    fun followStore(storeId:String?){
        launch(Dispatchers.Main){
            val followCall = async(Dispatchers.IO) { followStore?.followStore(storeId)}
            when(val result = followCall.await()){
                is Result.Success->{
                    val storeCall = async(Dispatchers.IO){getStore.getStore(storeId)}
                    when(val storeResult = storeCall.await()){
                        is Result.Success->view?.onSuccess(storeResult.data)
                    }
                }
            }

        }
    }

    fun unFollowStore(storeId: String?){
        launch(Dispatchers.Main){
            val unFollowCall = async(Dispatchers.IO) { followStore?.unfollowStore(storeId)}
            when(val result = unFollowCall.await()){
                is Result.Success->{
                    val storeCall = async(Dispatchers.IO){getStore.getStore(storeId)}
                    when(val storeResult = storeCall.await()){
                        is Result.Success->view?.onSuccess(storeResult.data)
                    }
                }
            }
        }
    }

    fun deleteProduct(productId:String){
        if(NetworkUtil.isConnectingToInternet()){
            view?.showProgressDialog(R.string.please_wait)
            launch(Dispatchers.Main){
                val call = async(Dispatchers.IO){ getProducts.deleteProduct(productId) }
                when(val result = call.await()){
                    is Result.Success->view?.productDeleted(productId)
                    is Result.Error->view?.onFailure(AppError(code = CustomError.DELETE_PRODUCT_ERROR))
                }
                view?.hideProgressDialog()
            }
        }
        else{
            view?.noInternet()
        }
    }

    fun markAsSold(productId: String){
        if(NetworkUtil.isConnectingToInternet()){
            view?.showProgressDialog(R.string.please_wait)
            launch(Dispatchers.Main){
                val call = async(Dispatchers.IO){ getProducts.markAsSold(productId) }
                when(val result = call.await()){
                    is Result.Success->view?.productMarkedAsSold(productId)
                    is Result.Error->view?.onFailure(AppError(code = CustomError.DELETE_PRODUCT_ERROR))
                }
                view?.hideProgressDialog()
            }
        }
        else{
            view?.noInternet()
        }
    }

    fun getProduct(productId: String,callback:(product:Product)->Unit){
        if(NetworkUtil.isConnectingToInternet()){
            view?.showProgressDialog(R.string.please_wait)
            launch(Dispatchers.Main){
                val call = async(Dispatchers.IO){ getProduct.invoke(productId, Utils.getAppLocale()) }
                when(val result = call.await()){
                    is Result.Success->callback(result.data)
                    is Result.Error->view?.onFailure(result.exception)
                }
                view?.hideProgressDialog()
            }
        }
    }

    fun blockStore(storeId: String){
        if(NetworkUtil.isConnectingToInternet()){
            view?.showProgressDialog(R.string.please_wait)
            launch(Dispatchers.Main){
                val call = async(Dispatchers.IO){ manageStore.blockStore(storeId) }
                when(val result = call.await()){
                    is Result.Success->  view?.onStoreBlockStatus(result.data)
                    is Result.Error->view?.onFailure(result.exception)
                }
                view?.hideProgressDialog()
            }
        }
    }

    fun getStoreReviews(storeId: String){
        launch{
            val call = async(Dispatchers.IO){ getReviews.getReviewList(storeId, AppConstant.ModuleType.ACCOUNTS,1) }
            when(val result = call.await()){
                is Result.Success->view?.loadStoreReview(result.data)
                is Result.Error->view?.onFailure(result.exception)
            }
        }
    }

    fun likeReview(reviewId:String,status:Int) = launch(Dispatchers.IO) { getReviews.likeReview(reviewId, status) }

    fun submitReview(listingId:String, title:Int, content:String, rating:Int, images:List<ImageFeed> = listOf(), callback:(success:Boolean)->Unit){
        if(NetworkUtil.isConnectingToInternet(true)){
            launch(Dispatchers.Main){
                view?.showProgressDialog(R.string.please_wait)
                val signedUrlList = mutableListOf<String>()
                if(images.isNotEmpty()){
                    when(val uploadImageResult = async(Dispatchers.IO) { uploadMedia.invoke(
                        FileHelper.convertListToFileInfoList(images),true) }.await()){
                        is Result.Success-> signedUrlList.addAll(uploadImageResult.data.map { it.uploadedUrl } )
                        is Result.Error-> {
                            view?.onFailure(uploadImageResult.exception)
                            return@launch
                        }
                    }
                }
                val call = async(Dispatchers.IO){ getReviews.addReview(AppConstant.ModuleType.ACCOUNTS,listingId, ReviewHelper.getReviewTitle(title), content, rating, signedUrlList) }
                when(val result = call.await()){
                    is Result.Success->callback(result.data)
                    is Result.Error->{
                        callback(false)
                        view?.onFailure(result.exception)
                    }
                }
                view?.hideProgressDialog()
            }
        }
    }

    fun accountActivate(storeId: String,active:Boolean){
        launch {
            view?.showProgressDialog(R.string.please_wait)
            val call = async(Dispatchers.IO){ getStore.activateAccount(storeId,active) }
            when(val result = call.await()){
                is Result.Success->view?.showActivationStatus(true)
                is Result.Error-> view?.onFailure(result.exception)
            }
            view?.hideProgressDialog()
        }
    }

    fun sendSubscribeMail(accoutId:String){
        launch {
            if(NetworkUtil.isConnectingToInternet()){
                view?.showProgressDialog(R.string.please_wait)
                val call = async(Dispatchers.IO) { sendSubscribeMail.sendMail(accoutId) }
                when(val result = call.await()){
                    is Result.Success->view?.onMailTriggerSuccess()
                    is Result.Error-> view?.onFailure(result.exception)
                }
            }
        }
    }

    private fun syncUserStore() = GlobalScope.launch(Dispatchers.IO){ getStore.syncUserStore(AppCache.getCacheUser()?.id)}

    fun onDestroy(){
        view = null
        job.cancelChildren()
    }
}