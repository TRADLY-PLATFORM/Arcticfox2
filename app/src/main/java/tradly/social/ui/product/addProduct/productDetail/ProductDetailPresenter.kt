package tradly.social.ui.product.addProduct.productDetail

import android.app.Activity
import tradly.social.common.*
import tradly.social.data.model.dataSource.*
import tradly.social.domain.entities.*
import tradly.social.domain.repository.*
import tradly.social.domain.usecases.*
import kotlinx.coroutines.*
import tradly.social.common.base.*
import tradly.social.common.cache.CurrencyCache
import tradly.social.common.network.CustomError
import tradly.social.common.network.converters.ProductConverter
import tradly.social.common.network.feature.common.datasource.StoreDataSourceImpl
import tradly.social.common.network.feature.common.datasource.ProductDataSourceImpl
import tradly.social.common.util.parser.extension.toJson
import kotlin.coroutines.CoroutineContext

class ProductDetailPresenter(var view: View?) : CoroutineScope {

    private var job: Job
    private var getProducts:GetProducts
    private var getProduct: GetProduct? = null
    private var followStore:FollowStore?=null
    private var getUser:GetUser?=null
    private var addCart:AddCart?=null
    private var getCart:GetCart?=null
    private var getReviews:GetReviews
    private var getNegotiation: GetNegotiation
    private var manageShippingAddress:ManageShippingAddress
    var user:User? = null
    var userId:String = AppConstant.EMPTY
    var userPic:String = AppConstant.EMPTY
    var userName:String = AppConstant.EMPTY
    interface View {
        fun onLoadProduct(product: Product)
        fun onLoadSimilarProducts(productList:List<Product>,isLoadMore:Boolean, pagination: Int)
        fun onFailure(appError: AppError)
        fun loadCurrency(currency:String?)
        fun showChatThread(chatRoom: ChatRoom?,provider: User)
        fun noInternet()
        fun onCartAdded()
        fun networkError(msg:Int)
        fun showProgressLoader()
        fun hideProgressLoader()
        fun showProgressDialog()
        fun hideProgressDialog()
        fun productDeleted()
        fun showReviewProgress()
        fun hideReviewProgress()
        fun showReviewInfo(rating: Rating)
        fun productMarkedAsSold()
        fun onUrlGenerated(url:String,channel: String,feature: String)
        fun onNegotiationInitiated(negotiationId: Int, negotiateAmount:String)
    }

    init {
        initUser()
        val parseCartDataSource = ParseCartDataSource()
        val cartRepository = CartRepository(parseCartDataSource)
        addCart = AddCart(cartRepository)
        getCart = GetCart(cartRepository)
        val parseProductDataSource = ProductDataSourceImpl()
        val productRepository = ProductRepository(parseProductDataSource)
        val reviewRepository= ReviewRepository(ParseReviewDataSource())
        val parseStoreDataSource = StoreDataSourceImpl()
        val storeRepository = StoreRepository(parseStoreDataSource)
        val parseUserAuthenticateDataSource = ParseUserAuthenticateDataSource()
        val userAuthenticateRepository = UserAuthenticateRepository(parseUserAuthenticateDataSource)
        val shippingAddressDataSource = ParseShippingAddressDataSource()
        val shippingAddressRepository = ShippingAddressRepository(shippingAddressDataSource)
        manageShippingAddress = ManageShippingAddress(shippingAddressRepository)
        getUser = GetUser(userAuthenticateRepository)
        followStore = FollowStore(storeRepository)
        getProduct = GetProduct(productRepository)
        getProducts = GetProducts(productRepository)
        getReviews = GetReviews(reviewRepository)
        getNegotiation = GetNegotiation(ChatRepository(ParseChatDataSource()))
        job = SupervisorJob()
    }

    private fun initUser(){
        user = AppController.appController.getUser()
        userId = user?.id?:Constant.EMPTY
        userName = user?.name?:Constant.EMPTY
        userPic = user?.profilePic?:Constant.EMPTY
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

   fun getShareUrl(activity: Activity, id: String, title: String, description: String, imageUrl: String, channel: String,feature:String,showProgress:Boolean){
       if (showProgress){
           view?.showProgressDialog()
       }
       BranchHelper.getBranchProductSharingUrl(activity, id, title, description, imageUrl,channel, feature){ url->
           view?.hideProgressDialog()
           view?.onUrlGenerated(url, channel,feature)
       }
   }

    fun getProduct(productId: String , shouldShowProgress:Boolean = true) {
        if (NetworkUtil.isConnectingToInternet()) {
            if(shouldShowProgress){
                view?.showProgressLoader()
            }
            launch(Dispatchers.Main) {
                val call = async(Dispatchers.IO) { getProduct?.invoke(productId, Utils.getAppLocale()) }
                when (val result = call.await()) {
                    is Result.Success -> {
                        view?.onLoadProduct(result.data)
                        getReviewAndSimilarProducts(productId)
                    }
                    is Result.Error -> {
                        view?.onFailure(result.exception)
                    }
                }
                view?.hideProgressLoader()
            }
        }
    }


    fun getSimilarProduct(productId: String, pagination:Int,isLoadMore: Boolean){
        if(NetworkUtil.isConnectingToInternet()){
            launch(Dispatchers.Main) {
                val call = async(Dispatchers.IO) { getProducts.getSimilarProducts(productId, pagination, AppConstant.ListPerPage.SIMILAR_PRODUCT_LIST) }
                when(val result = call.await()){
                    is Result.Success-> view?.onLoadSimilarProducts(result.data,isLoadMore,pagination)
                    is Result.Error->view?.onFailure(result.exception)
                }
            }
        }
    }


    fun followStore(storeId:String?){
        launch(Dispatchers.IO) { followStore?.followStore(storeId) }
    }

    fun unFollowStore(storeId: String?){
        launch(Dispatchers.IO) { followStore?.unfollowStore(storeId) }
    }

    fun likeListing(productId: String,isForLike:Boolean){
        launch(Dispatchers.IO){ getProducts.likeProduct(productId,isForLike)}
    }

    fun likeReview(reviewId:String,status: Int){
        launch(Dispatchers.IO){ getReviews.likeReview(reviewId, status)}
    }

    fun checkChatRoom(provider:User , storeName:String){
        if(NetworkUtil.isConnectingToInternet()){
            view?.showProgressDialog()
            initUser()
            checkChatRoom(provider,userName, userId, provider.id, storeName, userPic, provider.profilePic?:Constant.EMPTY)
        }
        else{
            view?.noInternet()
        }
    }

    fun deleteProduct(productId:String){
        if(NetworkUtil.isConnectingToInternet()){
            view?.showProgressDialog()
            launch(Dispatchers.Main){
                val call = async(Dispatchers.IO){ getProducts.deleteProduct(productId) }
                when(val result = call.await()){
                    is Result.Success->view?.productDeleted()
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
            view?.showProgressDialog()
            launch(Dispatchers.Main){
                val call = async(Dispatchers.IO){ getProducts.markAsSold(productId) }
                when(val result = call.await()){
                    is Result.Success->view?.productMarkedAsSold()
                    is Result.Error->view?.onFailure(AppError(code = CustomError.DELETE_PRODUCT_ERROR))
                }
                view?.hideProgressDialog()
            }
        }
        else{
            view?.noInternet()
        }
    }

    private fun checkChatRoom(provider: User,senderName:String,senderId:String,providerId:String,providerName:String,profilePic:String,providerProfilePic:String){
        MessageHelper.checkChatRoomAvailable(senderName,senderId,providerId,providerName,profilePic,providerProfilePic,object :
            MessageHelper.Companion.ChatListener{
            override fun onSuccess(any: Any?) {
                view?.hideProgressDialog()
                view?.showChatThread(any as? ChatRoom,provider)
            }
            override fun onFailure(message: String?,code:Int) {
                view?.hideProgressDialog()
                ErrorHandler.printLog(message)
            }
        })
    }

    fun initiateChatNegotiation(receiver:User, receiverName:String,product: Product,negotiateAmount:String,negotiateId:Int){
        if (NetworkUtil.isConnectingToInternet(true)){
            initUser()
            val map = hashMapOf<String,Any?>()
            map["id"] = product.id.toInt()
            map["title"] = product.title
            map["images"] = product.images
            map["offer_price"] = ProductConverter.mapFrom(product.offerPrice!!)
            map["list_price"] = ProductConverter.mapFrom(product.listPrice!!)
            map["offer_percent"] = product.offerPercent
            val reviewMap = HashMap<String,Any?>()
            reviewMap["rating_average"] = product.rating?.ratingAverage?.toString()
            reviewMap["rating_count"] = product.rating?.ratingCount?.toString()
            reviewMap["review_count"] = product.rating?.reviewCount?.toString()
            map["rating_data"] = reviewMap
            MessageHelper.initiateNegotiationChat(user!!,receiver,receiverName,map.toJson<Map<String,Any?>>(),negotiateAmount,negotiateId.toString(),object :MessageHelper.Companion.ChatListener{
                override fun onSuccess(any: Any?) {
                    view?.hideProgressDialog()
                    view?.showChatThread(any as? ChatRoom,receiver)
                }

                override fun onFailure(message: String?, code: Int) {
                    view?.hideProgressDialog()
                    ErrorHandler.printLog(message)
                }

            })
        }
    }

    fun getChatNegotiationId(productId: String, negotiateAmount:String){
        if (NetworkUtil.isConnectingToInternet(true)){
            launch(Dispatchers.Main) {
                view?.showProgressDialog()
                val call = async(Dispatchers.IO) { getNegotiation.makeNegotiation(productId,negotiateAmount.toInt()) }
                when(val result = call.await()){
                    is Result.Success-> {
                        val symbol = CurrencyCache.getDefaultCurrency()?.symbol
                        view?.onNegotiationInitiated(result.data, symbol+negotiateAmount)
                    }
                    is Result.Error->{
                        view?.onFailure(result.exception)
                    }
                }
                view?.hideProgressDialog()
            }
        }
    }

    fun addCartItem(id:String?,type:Int = 0,quantity:Int=1){
        if(NetworkUtil.isConnectingToInternet()){
            view?.showProgressDialog()
            launch(Dispatchers.Main){
                val call = async (Dispatchers.IO){ addCart?.addCart(id, quantity, type) }
                when(val result = call.await()){
                    is Result.Success->{view?.onCartAdded()}
                    is Result.Error->{view?.onFailure(result.exception)}
                }
                view?.hideProgressDialog()
            }
        }
    }

    fun clearCartItems(id: String?){
        if(NetworkUtil.isConnectingToInternet(true)){
            view?.showProgressDialog()
            launch(Dispatchers.Main){
                val result = async(Dispatchers.IO){ getCart?.clearCartItems() }.await()
                when(result){
                    is Result.Success->addCartItem(id)
                    is Result.Error->view?.onFailure(result.exception)
                }
            }
        }
    }

    private fun getReviewAndSimilarProducts(listingId:String){
        view?.showReviewProgress()
        launch(Dispatchers.Main){
            val reviewCall = async(Dispatchers.IO){ getReviews.getReviewList(listingId, AppConstant.ModuleType.LISTINGS,1) }
            if(AppConfigHelper.getConfigKey(AppConfigHelper.Keys.SIMILAR_LISTING_ENABLED)){
                val similarProductCall = async(Dispatchers.IO){ getProducts.getSimilarProducts(listingId,1,
                    AppConstant.ListPerPage.SIMILAR_PRODUCT_LIST) }
                when(val result = similarProductCall.await()){
                    is Result.Success-> view?.onLoadSimilarProducts(result.data,false,1)
                    is Result.Error->view?.onFailure(result.exception)
                }
            }
            when(val result = reviewCall.await()){
                is Result.Success->{
                    view?.showReviewInfo(result.data)
                }
            }
            view?.hideReviewProgress()
        }
    }

    fun isListingNotApproved(status:Int) = (status == AppConstant.ListingStatus.REJECTED || status == AppConstant.ListingStatus.WAITING_FOR_APPROVAL)

    fun onDestroy(){
        view =null
        job.cancelChildren()
    }

}