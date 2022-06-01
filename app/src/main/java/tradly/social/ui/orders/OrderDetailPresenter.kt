package tradly.social.ui.orders

import kotlinx.coroutines.*
import tradly.social.common.base.AppConstant
import tradly.social.common.base.FileHelper
import tradly.social.common.base.NetworkUtil
import tradly.social.common.base.Utils
import tradly.social.common.base.ReviewHelper
import tradly.social.common.network.feature.common.datasource.OrderDataSourceImpl
import tradly.social.common.network.feature.common.datasource.ParseMediaDataSource
import tradly.social.common.network.feature.common.datasource.ProductDataSourceImpl
import tradly.social.domain.entities.ImageFeed
import tradly.social.data.model.CoroutinesManager
import tradly.social.data.model.dataSource.*
import tradly.social.domain.entities.AppError
import tradly.social.domain.entities.OrderDetail
import tradly.social.domain.entities.Result
import tradly.social.domain.entities.ShippingAddress
import tradly.social.domain.repository.*
import tradly.social.domain.usecases.*
import tradly.social.common.uiEntity.CustomPair
import kotlin.coroutines.CoroutineContext

class OrderDetailPresenter (var view:View?): CoroutineScope{
    interface View {
        fun getOrderDetails(orderDetail: OrderDetail)
        fun showProgressLoader()
        fun hideProgressLoader()
        fun showProgressDialogLoader()
        fun hideProgressDialogLoader()
        fun onFailure(appError: AppError)
    }

    private var job: Job
    private var getOrderDetailUseCase: GetOrderDetailUseCase
    private var updateStatus:UpdateOrderStatus
    private var getReviews: GetReviews
    private var uploadMedia:UploadMedia
    private var manageShippingAddress:ManageShippingAddress

    init {
        val orderDataSource = OrderDataSourceImpl()
        val orderRepository = OrderRepository(orderDataSource)
        val parseProductDataSource = ProductDataSourceImpl()
        val productRepository = ProductRepository(parseProductDataSource)
        val reviewRepository = ReviewRepository(ParseReviewDataSource())
        val mediaDataSource = ParseMediaDataSource()
        val mediaRepository = MediaRepository(mediaDataSource)
        getOrderDetailUseCase = GetOrderDetailUseCase(orderRepository)
        val shippingAddressDataSource = ParseShippingAddressDataSource()
        val shippingAddressRepository = ShippingAddressRepository(shippingAddressDataSource)
        updateStatus = UpdateOrderStatus(orderRepository)
        getReviews = GetReviews(reviewRepository)
        uploadMedia = UploadMedia(mediaRepository)
        manageShippingAddress = ManageShippingAddress(shippingAddressRepository)
        job = SupervisorJob()
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    fun getOrderDetails(orderId: String , accountId:String, showProgress:Boolean = true) {
        launch {
            if(showProgress){
                view?.showProgressLoader()
            }
            val call = async(Dispatchers.IO) { getOrderDetailUseCase.getOrderDetails(orderId,accountId) }
            when (val result = call.await()) {
                is Result.Success -> view?.getOrderDetails(result.data)
                is Result.Error -> view?.onFailure(result.exception)
            }
            view?.hideProgressLoader()
        }
    }

    fun updateOrderStatus(orderId: String,accountId:String,status: Int){
        if(NetworkUtil.isConnectingToInternet(true)){
            launch(Dispatchers.Main){
                view?.showProgressDialogLoader()
                val call = async(Dispatchers.IO){updateStatus.invoke(orderId,status)}
                when(val result = call.await()){
                    is Result.Success-> getOrderDetails(orderId,accountId,false)
                    is Result.Error->view?.onFailure(result.exception)
                }
                view?.hideProgressDialogLoader()
            }
        }
    }

    fun updatePickupAddress(orderId: String,accountId:String,shippingAddress: ShippingAddress){
        view?.showProgressDialogLoader()
        CoroutinesManager.ioThenMain({manageShippingAddress.updatePickupAddress(orderId,shippingAddress)}){
            when(it){
                is Result.Success->getOrderDetails(orderId,accountId,false)
                is Result.Error->view?.onFailure(it.exception)
            }
            view?.hideProgressDialogLoader()
        }
    }

    fun submitReview(listingId:String, title:Int, content:String, rating:Int, images:List<ImageFeed> = listOf(), callback:(success:Boolean)->Unit){
        if(NetworkUtil.isConnectingToInternet(true)){
            launch(Dispatchers.Main){
                view?.showProgressDialogLoader()
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
                val call = async(Dispatchers.IO){ getReviews.addReview(AppConstant.ModuleType.LISTINGS,listingId, ReviewHelper.getReviewTitle(title), content, rating, signedUrlList) }
                when(val result = call.await()){
                    is Result.Success->callback(result.data)
                    is Result.Error->{
                        callback(false)
                        view?.onFailure(result.exception)
                    }
                }
                view?.hideProgressDialogLoader()
            }
        }
    }

    fun getNextStatus(list: List<Int>):List<CustomPair<Boolean, CustomPair<Int, Int>>>{
        val mlist = mutableListOf<CustomPair<Boolean, CustomPair<Int, Int>>>()
        for(i in list){
            Utils.getOrderStatus(i).let {
                if(it !=0){
                    mlist.add(CustomPair((i==0), CustomPair(i,it)))
                }
            }
        }
        list.forEach { i ->

        }
        return mlist
    }

    fun onDestroy(){
        view = null
        job.cancelChildren()
    }
}