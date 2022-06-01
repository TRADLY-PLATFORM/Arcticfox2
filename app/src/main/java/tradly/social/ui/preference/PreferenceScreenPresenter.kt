package tradly.social.ui.preference

import tradly.social.R
import tradly.social.common.base.NetworkUtil
import tradly.social.data.model.dataSource.ParseUserAuthenticateDataSource
import tradly.social.common.network.parseHelper.ParseConstant
import tradly.social.domain.entities.AppError
import tradly.social.domain.entities.Result
import tradly.social.domain.entities.User
import tradly.social.domain.repository.UserAuthenticateRepository
import tradly.social.domain.usecases.UpdateUserDetail
import tradly.social.ui.base.BaseView
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class PreferenceScreenPresenter(var view: View?) : CoroutineScope {

    private var job: Job
    private var updateUserDetail: UpdateUserDetail? = null

    interface View : BaseView {
        fun onSuccess(user: User)
        fun onFailure(appError: AppError)
        fun noInternet()
    }

    init {
        val parseUserAuthenticateDataSource = ParseUserAuthenticateDataSource()
        val userAuthenticateRepository = UserAuthenticateRepository(parseUserAuthenticateDataSource)
        updateUserDetail = UpdateUserDetail(userAuthenticateRepository)
        job = Job()
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    fun updateUserDetail(userId:String,userType: Int) {
        if(NetworkUtil.isConnectingToInternet()){
            view?.showProgressDialog(R.string.please_wait)
            launch(Dispatchers.Main) {
                val call = async(Dispatchers.IO) { updateUserDetail?.invoke(userId,hashMapOf(
                    ParseConstant.USER_TYPE to userType)) }
                val result = call.await()
                view?.hideProgressDialog()
                when(result){
                    is Result.Success->view?.onSuccess(result.data)
                    is Result.Error->view?.onFailure(result.exception)
                }
            }
        }
        else{
            view?.noInternet()
        }
    }

    fun onDestroy() {
        job.cancel()
        view = null
    }
}