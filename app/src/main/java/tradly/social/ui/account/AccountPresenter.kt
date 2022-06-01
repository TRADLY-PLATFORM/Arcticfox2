package tradly.social.ui.account

import tradly.social.R
import tradly.social.common.base.NetworkUtil
import tradly.social.common.base.Utils
import tradly.social.data.model.dataSource.ParseCountryDataSource
import tradly.social.data.model.dataSource.ParseUserAuthenticateDataSource
import tradly.social.common.network.parseHelper.ParseConstant
import tradly.social.domain.entities.*
import tradly.social.domain.repository.CountryRepository
import tradly.social.domain.repository.UserAuthenticateRepository
import tradly.social.domain.usecases.GetCountry
import tradly.social.domain.usecases.UserSignUp
import tradly.social.ui.base.BaseView
import kotlinx.coroutines.*
import tradly.social.data.model.AppDataSyncHelper
import kotlin.coroutines.CoroutineContext

open class AccountPresenter(private var view: AccountInteractor?) :
    CoroutineScope {
    private var job: Job
    private var getCountry: GetCountry? = null
    private var userSignUp: UserSignUp

    init {
        val parseCountryDataSource = ParseCountryDataSource()
        val countryRepository = CountryRepository(parseCountryDataSource)
        getCountry = GetCountry(countryRepository)
        val parseUserAuthenticateDataSource = ParseUserAuthenticateDataSource()
        val userAuthenticateRepository = UserAuthenticateRepository(parseUserAuthenticateDataSource)
        userSignUp = UserSignUp(userAuthenticateRepository)
        job = Job()
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    interface AccountInteractor : BaseView {
        fun onSuccess(verification: Verification ,uuid:String, email:String,mobile:String,password:String, country: Country? = null)
        fun onFailure(appError: AppError?)
        fun onComplete(user: User)
        fun onLoadCountries(list: List<Country>?)
        fun inputError(id: Int, msg: Int)
        fun noInternet()
    }

    fun signUp(
        authType: Int?,
        uuid:String,
        firstName: String,
        lastName: String,
        email: String = Constant.EMPTY,
        password: String = Constant.EMPTY,
        confirmPassword: String? = Constant.EMPTY,
        mobileNumber: String = Constant.EMPTY,
        country: Country? = null,
        isTermsAgreed:Boolean = true
    ) {
        if (isValid(authType, firstName, lastName, email, mobileNumber, password, confirmPassword, country) && isTermsAccepted(isTermsAgreed)) {
            if (NetworkUtil.isConnectingToInternet()) {
                try {
                    view?.showProgressDialog(R.string.please_wait)
                    launch(Dispatchers.Main) {
                        val call = async(Dispatchers.IO) {
                            if(authType == ParseConstant.AuthType.MOBILE_PASSWORD_NO_OTP){
                                userSignUp.signUpNoOtp(uuid,firstName,lastName,mobileNumber,email,password,country?.dialCode)
                            }
                            else{
                                userSignUp.signUp(uuid,firstName,lastName,mobileNumber,email,password,country?.dialCode)
                            }
                        }
                        when (val result = call.await()) {
                            is Result.Success -> {
                                if(authType == ParseConstant.AuthType.MOBILE_PASSWORD_NO_OTP){
                                    AppDataSyncHelper.syncAppConfig()
                                    AppDataSyncHelper.syncDeviceDetail(true)
                                    AppDataSyncHelper.syncCurrencies()
                                    view?.onComplete(result.data as User)
                                }
                                else{
                                    view?.onSuccess((result.data as Verification),uuid,email,mobileNumber,password,country)
                                }
                            }
                            is Result.Error -> view?.onFailure(result.exception)
                        }
                        view?.hideProgressDialog()
                    }
                } catch (exception: Exception) {
                    view?.onFailure(AppError(exception.message))
                    view?.hideProgressDialog()
                }
            } else {
                view?.noInternet()
            }
        }
    }

    fun getCountries() {
        try {
            view?.showProgressDialog(R.string.please_wait)
            launch(Dispatchers.Main) {
                val call = async(Dispatchers.IO) { getCountry?.invoke() }
                val result = call.await()
                when (result) {
                    is Result.Success -> view?.onLoadCountries(result.data)
                    is Result.Error -> view?.onFailure(result.exception)
                }
                view?.hideProgressDialog()
            }
        } catch (exception: Exception) {
            view?.onFailure(AppError(exception.message))
        }
    }

    private fun isValid(
        authType: Int?,
        firstName: String?,
        lastName: String?,
        email: String?,
        mobileNumber: String?,
        password: String?,
        confirmPassword: String?,
        country: Country?) = when {
            firstName.isNullOrEmpty() -> {
                view?.inputError(R.id.edFirstName, R.string.signup_required)
                false
            }
            lastName.isNullOrEmpty() -> {
                view?.inputError(R.id.edLastName, R.string.signup_required)
                false
            }
            ParseConstant.AuthType.MOBILE == authType -> mobileValidation(mobileNumber, country)
            ParseConstant.AuthType.EMAIL == authType ->  {
                if(!Utils.validateEmail(email)){
                    view?.inputError(R.id.edEmail,R.string.signup_invalid_email)
                    false
                }
                else{
                    passwordValidation(password, confirmPassword)
                }
            }
            ParseConstant.AuthType.MOBILE_PASSWORD == authType || ParseConstant.AuthType.MOBILE_PASSWORD_NO_OTP == authType
            -> mobileValidation(mobileNumber,country) && passwordValidation(password, confirmPassword)
            else -> false
        }

    private fun isTermsAccepted(isTermsAgreed:Boolean) = if(!isTermsAgreed){
        view?.inputError(R.id.termsCheckBox,R.string.signup_accept_terms_condition)
        false
    }
    else{
        true
    }

    private fun passwordValidation(password: String?,confirmPassword: String?) = when{
        password.isNullOrEmpty()->{
            view?.inputError(R.id.edPassword, R.string.signup_required)
            false
        }
        password.length <= 5->{
            view?.inputError(R.id.edPassword, R.string.signup_min_password_validation)
            false
        }
        confirmPassword.isNullOrEmpty()->{
            view?.inputError(R.id.edConfirmPassword, R.string.signup_required)
            false
        }
        !password.equals(confirmPassword)->{
            view?.inputError(R.id.edConfirmPassword, R.string.signup_password_mismatch)
            false
        }
        else->true
    }

    private fun mobileValidation(mobileNumber: String?, country: Country?) = when {
        mobileNumber.isNullOrEmpty() -> {
            view?.inputError(R.id.edMobileNumber, R.string.signup_required)
            false
        }
        (country?.mobileNumberLength!=0 && mobileNumber.length != country?.mobileNumberLength) || !Utils.isValidNumberFormat(country.mobileNumberRegex,country.dialCode.plus(mobileNumber)) -> {
            view?.inputError(R.id.edMobileNumber, R.string.invalid_no)
            false
        }

        else -> true
    }


    fun onDestroy() {
        job.cancel()
        view = null
    }
}



