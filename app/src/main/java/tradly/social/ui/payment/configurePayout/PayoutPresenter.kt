package tradly.social.ui.payment.configurePayout

import kotlinx.coroutines.*
import tradly.social.R
import tradly.social.common.base.AppConstant
import tradly.social.common.base.NetworkUtil
import tradly.social.data.model.dataSource.ParsePayoutDataSource
import tradly.social.data.model.dataSource.ParseUserAuthenticateDataSource
import tradly.social.domain.entities.AppError
import tradly.social.domain.entities.Result
import tradly.social.domain.repository.PayoutRepository
import tradly.social.domain.repository.UserAuthenticateRepository
import tradly.social.domain.usecases.GetUser
import tradly.social.domain.usecases.PayoutConfigure
import kotlin.coroutines.CoroutineContext

open class PayoutPresenter: CoroutineScope{

    interface View{
        fun showProgressLoader(msg:Int)
        fun hideProgressLoader()
        fun onSuccessAccountLink(oAuthUrl:String)
        fun onError(appError: AppError)
        fun onVerifySuccess(success:Boolean)
        fun onStripeStatus(isStripeOnBoardingConnected:Boolean,isPayoutEnabled:Boolean,errors:List<String>)
        fun onDisconnectSuccess()
        fun showProgressBar()
        fun hideProgressBar()
    }

    private var job: Job
    var payoutConfigure:PayoutConfigure
    var getUser:GetUser
    var view: View? = null
    constructor(view: View?) {
        this.view = view
    }

    init {
        job = SupervisorJob()
        val payoutDataSource = ParsePayoutDataSource()
        val userDataSource = ParseUserAuthenticateDataSource()
        getUser = GetUser(UserAuthenticateRepository(userDataSource))
        payoutConfigure = PayoutConfigure(PayoutRepository(payoutDataSource))
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    fun connectOAuth(stripeAccountType:String,accountId:String = AppConstant.EMPTY){
        if(NetworkUtil.isConnectingToInternet(true)){
            launch(Dispatchers.Main){
                view?.showProgressLoader(R.string.please_wait)
                val call = async(Dispatchers.IO){
                   if(AppConstant.StripeConnectAccountType.STANDARD==stripeAccountType){
                       payoutConfigure.initStripeConnect()
                   }
                    else{
                       payoutConfigure.getStripeAccountLink(accountId)
                   }
                }
                when(val result = call.await()){
                    is Result.Success-> view?.onSuccessAccountLink(result.data.url)
                    is Result.Error->view?.onError(result.exception)
                }
                view?.hideProgressLoader()
            }
        }
    }

    fun getStripeStatus(accountId: String){
        if(NetworkUtil.isConnectingToInternet(true)){
            launch {
                view?.showProgressBar()
                val call = async(Dispatchers.IO){ payoutConfigure.getStripeConnectStatus(accountId) }
                when(val result = call.await()){
                    is Result.Success-> with(result.data){
                        view?.onStripeStatus(isStripeOnBoardingConnected,isPayoutEnabled,errors)
                    }
                    is Result.Error->view?.onError(result.exception)
                }
                view?.hideProgressBar()
            }
        }
    }

    fun getStripeLink(accountId: String){
        if (NetworkUtil.isConnectingToInternet(true)){
            launch {
                view?.showProgressLoader(R.string.please_wait)
                val call = async(Dispatchers.IO){ payoutConfigure.getStripeLoginLink(accountId) }
                when(val result = call.await()){
                    is Result.Success->view?.onSuccessAccountLink(result.data.url)
                    is Result.Error->view?.onError(result.exception)
                }
                view?.hideProgressLoader()
            }
        }
    }

    fun disconnectOAuth(){
        if(NetworkUtil.isConnectingToInternet(true)){
            launch(Dispatchers.Main){
                view?.showProgressLoader(R.string.please_wait)
                val call = async(Dispatchers.IO){ payoutConfigure.disConnectOAuth()}
                when(val result = call.await()){
                    is Result.Success-> view?.onDisconnectSuccess()
                    is Result.Error->view?.onError(result.exception)
                }
                view?.hideProgressLoader()
            }
        }
    }

    fun verifyOAuth(url:String){
        launch(Dispatchers.Main){
            view?.showProgressLoader(R.string.please_wait)
            val call = async(Dispatchers.IO){ payoutConfigure.verifyStripeOAuth(url)}
            when(val result = call.await()){
                is Result.Success-> view?.onVerifySuccess(result.data)
                is Result.Error->view?.onError(result.exception)
            }
            view?.hideProgressLoader()
        }
    }

    fun fetchUserDetail(userId:String){
        launch(Dispatchers.Main){
            view?.showProgressLoader(R.string.please_wait)
            val call = async(Dispatchers.IO){ getUser.invoke(userId) }
            when(val result = call.await()){
                is Result.Success->{view?.onVerifySuccess(true)}
                is Result.Error->result
            }
            view?.hideProgressLoader()
        }
    }

    open fun onDestroy(){
        view = null
        job.cancel()
    }
}