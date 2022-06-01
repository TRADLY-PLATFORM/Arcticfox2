package tradly.social.ui.login

import kotlinx.coroutines.*
import tradly.social.R
import tradly.social.common.*
import tradly.social.common.base.*
import tradly.social.data.model.AppDataSyncHelper
import tradly.social.data.model.dataSource.ParseCountryDataSource
import tradly.social.data.model.dataSource.ParseConfigDataSource
import tradly.social.common.network.feature.common.datasource.StoreDataSourceImpl
import tradly.social.data.model.dataSource.ParseUserAuthenticateDataSource
import tradly.social.common.network.parseHelper.ParseConstant
import tradly.social.common.cache.AppCache
import tradly.social.common.common.AppConstant
import tradly.social.common.persistance.shared.PreferenceSecurity
import tradly.social.domain.entities.*
import tradly.social.domain.repository.CountryRepository
import tradly.social.domain.repository.ParseConfigRepository
import tradly.social.domain.repository.StoreRepository
import tradly.social.domain.repository.UserAuthenticateRepository
import tradly.social.domain.usecases.*
import tradly.social.ui.base.BaseView
import tradly.social.ui.base.FieldView
import kotlin.coroutines.CoroutineContext

class LoginPresenter(private var view: View?) : CoroutineScope {
    private var job: Job
    private var getAppConfig: GetAppConfig? = null
    private var getUserDetail: GetUserDetail? = null
    private var getCountry: GetCountry? = null
    private lateinit var syncStoreDetail: GetStores

    interface View : BaseView, FieldView {
        fun onSuccess(user: User)
        fun launchVerificationPage(verification:Verification,uuid:String = Constant.EMPTY,mobile:String = Constant.EMPTY,country: Country? = null)
        fun onFailure(appError: AppError)
        fun doSocialLogout(provider:String)
        fun noInternet()
        fun onPasswordSetSuccess()
        fun onLoadCountries(list: List<Country>?)
    }

    init {
        val storeDataSource = StoreDataSourceImpl()
        val parseManagerConfigDataSource = ParseConfigDataSource()
        val parseConfigRepository = ParseConfigRepository(parseManagerConfigDataSource)
        val parseUserAuthenticateDataSource = ParseUserAuthenticateDataSource()
        val userAuthenticateRepository = UserAuthenticateRepository(parseUserAuthenticateDataSource)
        val parseCountryDataSource = ParseCountryDataSource()
        val countryRepository = CountryRepository(parseCountryDataSource)
        val storeRepository = StoreRepository(storeDataSource)
        getAppConfig = GetAppConfig(parseConfigRepository)
        getUserDetail = GetUserDetail(userAuthenticateRepository)
        syncStoreDetail = GetStores(storeRepository)
        getCountry = GetCountry(countryRepository)
        job = Job()
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job


    fun initLogin(authType: Int?,uuid:String,mobile:String,country:Country){
        if(NetworkUtil.isConnectingToInternet()){
            if(isValidInput(authType,mobile,null,country)){
                view?.showProgressDialog(R.string.please_wait)
                launch(Dispatchers.Main){
                    val call  = async(Dispatchers.IO){ getUserDetail?.initLogin(uuid,mobile,country.dialCode) }
                    when(val result = call.await()){
                        is Result.Success->view?.launchVerificationPage(result.data,uuid,mobile,country)
                        is Result.Error->view?.onFailure(result.exception)
                    }
                    view?.hideProgressDialog()
                }
            }
        }
        else{
            view?.noInternet()
        }
    }


    fun login(
        authType: Int?,
        uuid:String,
        emailOrMobile: String = Constant.EMPTY,
        password: String = Constant.EMPTY,
        country: Country? = null) {
        if (isValidInput(authType, emailOrMobile, password, country)) {
            if (NetworkUtil.isConnectingToInternet()) {
                view?.showProgressDialog(R.string.logging_in)
                launch(Dispatchers.Main) {
                    val call = async(Dispatchers.IO) {
                        if (ParseConstant.AuthType.EMAIL == authType) {
                            getUserDetail?.login(uuid, email = emailOrMobile, password = password)
                        } else {
                            getUserDetail?.login(uuid, mobile = emailOrMobile, password = password, countryId = country?.dialCode)
                        }
                    }
                    when (val result = call.await()) {
                        is Result.Success -> {
                            AppDataSyncHelper.syncAppConfig()
                            AppDataSyncHelper.syncDeviceDetail(true)
                            AppDataSyncHelper.syncCurrencies()
                            val syncCall = async(Dispatchers.IO){ syncStoreDetail.syncUserStore(AppCache.getCacheUser()?.id) }
                            syncCall.await()
                            view?.onSuccess(result.data)
                            trackMoEngageAttributes(result.data)
                        }
                        is Result.Error -> view?.onFailure(result.exception)
                    }
                    view?.hideProgressDialog()
                }
            }
            else {
                view?.noInternet()
            }
        }
        }

    fun socialLogin(token:String,provider:String,uuid: String){
        if (NetworkUtil.isConnectingToInternet(true)){
            launch(Dispatchers.Main) {
                view?.showProgressDialog(R.string.logging_in)
                val call = async(Dispatchers.IO) { getUserDetail?.socialLogin(token, provider,uuid) }
                when (val result = call.await()) {
                    is Result.Success -> {
                        PreferenceSecurity.putString(AppConstant.PREF_APP_LOGIN_TYPE,provider)
                        AppDataSyncHelper.syncAppConfig()
                        AppDataSyncHelper.syncDeviceDetail(true)
                        AppDataSyncHelper.syncCurrencies()
                        val syncCall = async(Dispatchers.IO){ syncStoreDetail.syncUserStore(AppCache.getCacheUser()?.id) }
                        syncCall.await()
                        view?.onSuccess(result.data)
                        trackMoEngageAttributes(result.data)
                    }
                    is Result.Error ->{
                        view?.doSocialLogout(provider)
                        view?.onFailure(result.exception)
                    }
                }
                view?.hideProgressDialog()
            }
        }
    }

    fun getCountries() {
        try {
            launch(Dispatchers.Main) {
                val call = async(Dispatchers.IO) { getCountry?.invoke() }
                when (val result = call.await()) {
                    is Result.Success -> view?.onLoadCountries(result.data)
                    is Result.Error -> view?.onFailure(result.exception)
                }
            }
        } catch (exception: Exception) {
            view?.onFailure(AppError(exception.message))
        }
    }

    fun recoverPassword(country: Country,mobile: String){
            if(NetworkUtil.isConnectingToInternet(true)){
                when(val res = InputValidator.validateMobile(country, mobile)){
                    is InputResult.Success->{
                        view?.showProgressDialog(R.string.please_wait)
                        launch(Dispatchers.Main){
                            val call = async(Dispatchers.IO){ getUserDetail?.doPasswordRecover(dialCode = country.dialCode,mobileNo = mobile) }
                            when(val result = call.await()){
                                is Result.Success-> view?.launchVerificationPage(result.data,mobile = mobile,country = country)
                                is Result.Error-> view?.onFailure(result.exception)
                            }
                            view?.hideProgressDialog()
                        }
                    }
                    is InputResult.Invalid-> view?.inputError(R.id.edInput,res.msg)
                }
            }
    }

    fun recoverPassword(email:String){
        if(NetworkUtil.isConnectingToInternet(true)){
            when(val res = InputValidator.validateEmail(email)){
                is InputResult.Success->{
                    view?.showProgressDialog(R.string.please_wait)
                    launch(Dispatchers.Main){
                        val call = async(Dispatchers.IO){ getUserDetail?.doPasswordRecover(email) }
                        when(val result = call.await()){
                            is Result.Success-> view?.launchVerificationPage(result.data)
                            is Result.Error-> view?.onFailure(result.exception)
                        }
                        view?.hideProgressDialog()
                    }
                }
                is InputResult.Invalid-> view?.inputError(R.id.edInput,res.msg)
            }
        }
    }

    fun resetPassword(verifyId:String,code:Pair<Int,String>,password: Pair<Int,String>,rePassword:Pair<Int,String>){
        if(NetworkUtil.isConnectingToInternet(true)){
            if(InputValidator.validatePasswordReset(view,code,password,rePassword)){
                view?.showProgressDialog(R.string.please_wait)
                launch(Dispatchers.Main){
                    val call = async(Dispatchers.IO){ getUserDetail?.passwordReset(password.second,verifyId,code.second) }
                    when(val result = call.await()){
                        is Result.Success-> view?.onPasswordSetSuccess()
                        is Result.Error-> view?.onFailure(result.exception)
                    }
                    view?.hideProgressDialog()
                }
            }
        }
    }

   private fun isValidInput(
        authType: Int?,
        inputOne: String?,
        password: String?,
        country: Country?
    ): Boolean {
        var isValid = true
        if (authType == ParseConstant.AuthType.EMAIL) {
            if (inputOne.isNullOrEmpty()) {
                isValid = false
                view?.inputError(R.id.edMobileNumber, R.string.login_required)
            } else if (password.isNullOrEmpty()) {
                isValid = false
                view?.inputError(R.id.edPassword, R.string.login_required)
            }
        } else if(authType == ParseConstant.AuthType.MOBILE) {
            if (inputOne.isNullOrEmpty()) {
                isValid = false
                view?.inputError(R.id.edMobileNumber, R.string.login_required)
            } else if (inputOne.length != country?.mobileNumberLength) {
                isValid = false
                view?.inputError(R.id.edMobileNumber, R.string.invalid_no)
            }
            else if(!Utils.isValidNumberFormat(country.mobileNumberRegex,country.dialCode.plus(inputOne))){
                isValid = false
                view?.inputError(R.id.edMobileNumber, R.string.invalid_no)
            }
        }
        else if (authType == ParseConstant.AuthType.MOBILE_PASSWORD || authType== ParseConstant.AuthType.MOBILE_PASSWORD_NO_OTP) {
            if (inputOne.isNullOrEmpty()) {
                isValid = false
                view?.inputError(R.id.edMobileNumber, R.string.login_required)
            } else if (password.isNullOrEmpty()) {
                isValid = false
                view?.inputError(R.id.edPassword, R.string.login_required)
            }
            else if (country != null) {
                if(!Utils.isValidNumberFormat(country.mobileNumberRegex,country.dialCode.plus(inputOne))){
                    isValid = false
                    view?.inputError(R.id.edMobileNumber, R.string.invalid_no)
                }
            }
        }
        return isValid
    }


    private fun trackMoEngageAttributes(user: User) {
        EventHelper.setUserAttribute(AppController.appContext,user)
    }


    fun onDestroy() {
        job.cancel()
        view = null
    }
}