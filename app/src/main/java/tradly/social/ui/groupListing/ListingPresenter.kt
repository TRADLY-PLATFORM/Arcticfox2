package tradly.social.ui.groupListing

import tradly.social.common.base.NetworkUtil
import tradly.social.domain.entities.AppError
import tradly.social.domain.entities.Result
import tradly.social.domain.usecases.*
import com.parse.ParseException
import kotlinx.coroutines.*
import tradly.social.common.network.feature.common.datasource.StoreDataSourceImpl
import tradly.social.data.model.dataSource.*
import tradly.social.domain.entities.Constant
import tradly.social.domain.repository.*
import kotlin.coroutines.CoroutineContext

open class ListingPresenter(private var view: View?) : CoroutineScope {
    interface View {
        fun onLoadList(list: List<Any>,pagination: Int,isLoadMore:Boolean)
        fun showProgressLoader()
        fun hideProgressLoader()
        fun showProgressDialog()
        fun hideProgressDialog()
        fun onFailure(appError: AppError)
        fun networkError(msg: String)
        fun noNetwork()
    }

    private var job: Job
    private var getCart: GetCart? = null
    private var getGroups: GetGroups? = null
    private var getStores: GetStores? = null
    private var followStore:FollowStore?=null
    private var getNotifications:GetNotifications
    private var getReviews:GetReviews
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    init {
        val parseCartDataSource = ParseCartDataSource()
        val cartRepository = CartRepository(parseCartDataSource)
        val parseGroupDataSource = ParseGroupDataSource()
        val groupRepository = GroupRepository(parseGroupDataSource)
        val parseStoreDataSource = StoreDataSourceImpl()
        val storeRepository = StoreRepository(parseStoreDataSource)
        val notificationRepository = NotificationRepository(ParseNotificationDataSource())
        val reviewRepository = ReviewRepository(ParseReviewDataSource())
        getStores = GetStores(storeRepository)
        getGroups = GetGroups(groupRepository)
        followStore = FollowStore(storeRepository)
        getCart = GetCart(cartRepository)
        getReviews = GetReviews(reviewRepository)
        getNotifications = GetNotifications(notificationRepository)
        job = Job()
    }


    fun getNotifications(pagination: Int,isLoadMore: Boolean){
        if(NetworkUtil.isConnectingToInternet(true)){
            launch(Dispatchers.Main){
                val call  = async(Dispatchers.IO){ getNotifications.getNotifications(pagination) }
                when (val result = call.await()) {
                    is Result.Success -> view?.onLoadList(result.data,pagination,isLoadMore)
                    is Result.Error -> view?.onFailure(result.exception)
                }
                view?.hideProgressLoader()
            }
        }
    }

    fun getGroupList(pagination:Int, isLoadMore:Boolean) {
        if (NetworkUtil.isConnectingToInternet()) {
            launch(Dispatchers.Main) {
                val call = async(Dispatchers.IO) { getGroups?.invoke("join", pagination) }
                when (val result = call.await()) {
                    is Result.Success -> view?.onLoadList(result.data,pagination,isLoadMore)
                    is Result.Error -> view?.onFailure(result.exception)
                }
                view?.hideProgressLoader()
            }
        }
    }

    fun getMyGroups(pagination:Int,isLoadMore:Boolean) {
        if (NetworkUtil.isConnectingToInternet()) {
            launch(Dispatchers.Main) {
                val call = async(Dispatchers.IO) { getGroups?.getMyGroups(20, pagination) }
                when (val result = call.await()) {
                    is Result.Success -> view?.onLoadList(result.data,pagination,isLoadMore)
                    is Result.Error -> view?.onFailure(result.exception)
                }
                view?.hideProgressLoader()
            }
        }
    }

    fun addUserToGroup(groupId: String) {
        if (NetworkUtil.isConnectingToInternet()) {
            launch(Dispatchers.IO) {
                getGroups?.addUserToGroup(groupId)
            }
        }
    }

    fun removeUserFromGroup(groupId:String){
        if(NetworkUtil.isConnectingToInternet()){
            launch(Dispatchers.IO) { getGroups?.removeUserFromGroup(groupId) }
        }
    }

    fun getStores(pagination:Int,isLoadMore:Boolean,collectionId:String = Constant.EMPTY,latitude:Double = 0.0,longitude:Double=0.0){
        if(NetworkUtil.isConnectingToInternet()){
            launch(Dispatchers.Main) {
                val call = async(Dispatchers.IO) { getStores?.getStores(pagination,collectionId = collectionId,latitude = latitude,longitude = longitude) }
                when(val result = call.await()){
                    is Result.Success->view?.onLoadList(result.data,pagination,isLoadMore)
                    is Result.Error->view?.onFailure(result.exception)
                }
                view?.hideProgressLoader()
            }
        }
    }

    fun getReviewList(id:String,type:String,pagination:Int,isLoadMore:Boolean){
        launch(Dispatchers.Main){
            val call = async(Dispatchers.IO){ getReviews.getReviewList(id,type, pagination) }
            when(val result = call.await()){
                is Result.Success->{
                    view?.onLoadList(result.data.reviewList,pagination,isLoadMore)
                }
            }
            view?.hideProgressLoader()
        }
    }


    fun findItemInCart(id:String ,type:Int,callback:(isAdded:Boolean)->Unit){
        launch(Dispatchers.Main){
            view?.showProgressDialog()
            val call = async(Dispatchers.IO){ getCart?.findCartItem(id,type) }
            when(val result = call.await()){
                is Result.Success->{callback(true)}
                is Result.Error->{
                    if(result.exception.code == ParseException.OBJECT_NOT_FOUND){
                       callback(false)
                    }
                    else{
                        view?.onFailure(result.exception)
                    }
                }
            }
            view?.hideProgressDialog()
        }
    }


    fun followStore(storeId:String){
        launch(Dispatchers.IO) { followStore?.followStore(storeId) }
    }

    fun unFollowStore(storeId: String){
        launch(Dispatchers.IO) { followStore?.unfollowStore(storeId) }
    }
    open fun onDestroy() {
        job.cancel()
        view = null
    }
}