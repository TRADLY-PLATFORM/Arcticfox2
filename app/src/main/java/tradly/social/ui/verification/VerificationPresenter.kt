package tradly.social.ui.verification

import tradly.social.R
import tradly.social.common.base.NetworkUtil
import tradly.social.data.model.dataSource.ParseUserAuthenticateDataSource
import tradly.social.domain.repository.UserAuthenticateRepository
import tradly.social.domain.usecases.GetUserDetail
import tradly.social.domain.usecases.UserSignUp
import tradly.social.ui.base.BaseView
import kotlinx.coroutines.*
import tradly.social.common.cache.AppCache
import tradly.social.data.model.AppDataSyncHelper
import tradly.social.common.network.feature.common.datasource.StoreDataSourceImpl
import tradly.social.domain.entities.*
import tradly.social.domain.repository.StoreRepository
import tradly.social.domain.usecases.GetStores
import tradly.social.ui.account.AccountPresenter
import kotlin.coroutines.CoroutineContext

class VerificationPresenter(var view: View?) : CoroutineScope,AccountPresenter.AccountInteractor {
    interface View : BaseView {
        fun onComplete(user:User)
        fun onFailure(appError: AppError?)
        fun noNetwork()
        fun otpResend(verification: Verification)
    }

    private var job: Job
    private var userSignUp: UserSignUp? = null
    private var getUserDetail: GetUserDetail? = null
    private var accountPresenter:AccountPresenter
    private var syncStoreDetail: GetStores

    init {
        job = Job()
        val parseUserAuthenticateDataSource = ParseUserAuthenticateDataSource()
        val userAuthenticateRepository = UserAuthenticateRepository(parseUserAuthenticateDataSource)
        userSignUp = UserSignUp(userAuthenticateRepository)
        getUserDetail = GetUserDetail(userAuthenticateRepository)
        val storeRepository = StoreRepository(StoreDataSourceImpl())
        accountPresenter = AccountPresenter(this)
        syncStoreDetail = GetStores(storeRepository)
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    fun verifyAuthentication(code: String, verificationId: String?) {
        if (NetworkUtil.isConnectingToInternet()) {
            view?.showProgressDialog(R.string.otp_otp_verifying)
            launch(Dispatchers.Main) {
                val call = async(Dispatchers.IO) { getUserDetail?.verifyAuthentication(verificationId,code)}
                when(val result = call.await()){
                    is Result.Success->{
                        AppDataSyncHelper.syncAppConfig()
                        AppDataSyncHelper.syncUserStore()
                        AppDataSyncHelper.syncCurrencies()
                        AppDataSyncHelper.syncDeviceDetail(true)
                        async(Dispatchers.IO) { syncStoreDetail.syncUserStore(AppCache.getCacheUser()?.id) }.await()
                        view?.onComplete(result.data)
                    }
                    is Result.Error->view?.onFailure(result.exception)
                }
                view?.hideProgressDialog()
            }
        }
    }


    fun initLogin(uuid:String,mobile:String,country:Country){
        if(NetworkUtil.isConnectingToInternet(true)){
            view?.showProgressDialog(R.string.please_wait)
            launch(Dispatchers.Main){
                val call  = async(Dispatchers.IO){ getUserDetail?.initLogin(uuid,mobile,country.id) }
                when(val result = call.await()){
                    is Result.Success->view?.otpResend(result.data)
                    is Result.Error->view?.onFailure(result.exception)
                }
                view?.hideProgressDialog()
            }
        }
    }

    fun signUp(authType: Int?,
                uuid:String,
                firstName: String,
                lastName: String,
                email: String = Constant.EMPTY,
                password: String = Constant.EMPTY,
                confirmPassword: String? = Constant.EMPTY,
                mobileNumber: String = Constant.EMPTY,
                country: Country? = null){
        accountPresenter.signUp(authType, uuid, firstName, lastName, email, password, confirmPassword, mobileNumber, country)
    }

    fun onDestroy(){
        job.cancel()
        view = null
    }

    override fun onSuccess(
        verification: Verification,
        uuid: String,
        email: String,
        mobile: String,
        password:String,
        country: Country?
    ) {
       view?.otpResend(verification)
    }

    override fun onFailure(appError: AppError?) {
        view?.onFailure(appError)
    }

    override fun onComplete(user: User) {

    }

    override fun onLoadCountries(list: List<Country>?) {

    }

    override fun inputError(id: Int, msg: Int) {

    }

    override fun noInternet() {
        view?.noNetwork()
    }

    override fun showProgressDialog(msg: Int) {
        view?.showProgressDialog(msg)
    }

    override fun hideProgressDialog() {
        view?.hideProgressDialog()
    }
}