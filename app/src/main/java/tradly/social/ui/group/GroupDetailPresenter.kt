package tradly.social.ui.group

import tradly.social.common.base.NetworkUtil
import tradly.social.data.model.dataSource.ParseGroupDataSource
import tradly.social.common.network.parseHelper.ParseClasses
import tradly.social.common.network.parseHelper.ParseConstant
import tradly.social.domain.entities.*
import tradly.social.domain.repository.GroupRepository
import tradly.social.domain.usecases.GetGroups
import com.parse.ParseObject
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class GroupDetailPresenter(private var view: View?) : CoroutineScope {
    private var job: Job
    private var getGroups: GetGroups? = null

    interface View {
        fun showProgressLoader()
        fun hideProgressLoader()
        fun onSuccess(result: BaseResult,isLoadMore:Boolean)
        fun onFailure(appError: AppError)
        fun noNetwork()
        fun loadRelatedProduct(list: List<Product>)
        fun loadMembers(list: List<User>)
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    init {
        val parseGroupDataSource = ParseGroupDataSource()
        val groupRepository = GroupRepository(parseGroupDataSource)
        getGroups = GetGroups(groupRepository)
        job = Job()
    }

    fun getGroup(groupId: String? = Constant.EMPTY, sort: String? = Constant.EMPTY, pagination: Int = 0, isMyProduct: Boolean = false,isLoadMore: Boolean) {
        if (NetworkUtil.isConnectingToInternet()) {
            view?.showProgressLoader()
            launch(Dispatchers.Main) {
                val call = async(Dispatchers.IO) { getGroups?.getGroup(groupId, sort, pagination, isMyProduct) }
                when (val result = call.await()) {
                    is Result.Success -> {
                        view?.onSuccess(result.data,isLoadMore)
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

    fun getMembers(groupId: String?) {
        launch(Dispatchers.Main) {
            val memberCall = async(Dispatchers.IO) { getGroups?.getGroupMembers(groupId) }
            val memberResult = memberCall.await()
            when(memberResult){
                is Result.Success -> {
                    view?.loadMembers(memberResult.data)
                }
                is Result.Error -> {
                    view?.onFailure(memberResult.exception)
                }
            }
        }
    }

    fun addUserToGroup(groupId: String?) {
        if (NetworkUtil.isConnectingToInternet()) {
            launch(Dispatchers.IO) {
                getGroups?.addUserToGroup(groupId)
            }
        }
    }

    fun removeUserFromGroup(groupId:String?){
        if(NetworkUtil.isConnectingToInternet()){
            launch(Dispatchers.IO) { getGroups?.removeUserFromGroup(groupId) }
        }
    }

    fun onDestroy(groupId:String?){
        ParseObject.createWithoutData(ParseClasses.GROUPS,groupId).unpin(ParseConstant.GROUP)
        job.cancel()
        view = null
    }

}