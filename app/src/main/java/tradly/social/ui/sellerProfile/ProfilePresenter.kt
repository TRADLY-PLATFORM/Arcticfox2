package tradly.social.ui.sellerProfile

import tradly.social.R
import tradly.social.data.model.dataSource.ParseCountryDataSource
import tradly.social.common.network.feature.common.datasource.ParseMediaDataSource
import tradly.social.data.model.dataSource.ParseUserAuthenticateDataSource
import tradly.social.domain.entities.*
import tradly.social.domain.repository.CountryRepository
import tradly.social.domain.repository.MediaRepository
import tradly.social.domain.repository.UserAuthenticateRepository
import tradly.social.domain.usecases.GetCountry
import tradly.social.domain.usecases.UpdateUserDetail
import tradly.social.domain.usecases.UploadMedia
import tradly.social.ui.base.BaseView
import kotlinx.coroutines.*
import tradly.social.common.base.NetworkUtil
import tradly.social.common.base.Utils
import tradly.social.common.base.FileHelper
import tradly.social.ui.payment.configurePayout.PayoutPresenter
import java.io.File
import kotlin.coroutines.CoroutineContext

class ProfilePresenter(view: ProfilePresenterView?) : CoroutineScope,PayoutPresenter.View {

    private var job: Job
    private  var updateUserDetail: UpdateUserDetail
    private  var getCountry:GetCountry
    private var uploadMedia:UploadMedia
    var profilePresenterView:ProfilePresenterView? = view
    var payoutPresenter: PayoutPresenter

    init {
        val parseUserAuthenticateDataSource = ParseUserAuthenticateDataSource()
        val userAuthenticateRepository = UserAuthenticateRepository(parseUserAuthenticateDataSource)
        val parseCountryDataSource = ParseCountryDataSource()
        val countryRepository = CountryRepository(parseCountryDataSource)
        val mediaDataSource = ParseMediaDataSource()
        val mediaRepository = MediaRepository(mediaDataSource)
        uploadMedia = UploadMedia(mediaRepository)
        getCountry = GetCountry(countryRepository)
        updateUserDetail = UpdateUserDetail(userAuthenticateRepository)
        payoutPresenter = PayoutPresenter(this)
        job = Job()
    }

    interface ProfilePresenterView : BaseView {
        fun onSuccess(user: User)
        fun onFailure(appError: AppError)
        fun noInternet()
        fun inputError(id: Int, msg: Int)
        fun onError(appError: AppError)
        fun onLoadCountries(list: List<Country>?)
        fun onDisconnectSuccess()
    }



    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    fun updateProfile(userId:String,isFor: String, inputOne: String? = Constant.EMPTY, inputTwo: String? = Constant.EMPTY ,country: Country?=null,selectedPic:String? = Constant.EMPTY) {
        if(isValid(isFor, inputOne, inputTwo,country)){
            if(NetworkUtil.isConnectingToInternet()){
                profilePresenterView?.showProgressDialog(R.string.please_wait)
                launch(Dispatchers.Main){
                    val call = async(Dispatchers.IO){ updateUserDetail.invoke(userId,getRequestBody(isFor, inputOne, inputTwo,country,selectedPic)) }
                    when(val result = call.await()){
                        is Result.Success->profilePresenterView?.onSuccess(result.data)
                        is Result.Error->profilePresenterView?.onFailure(result.exception)
                    }
                    profilePresenterView?.hideProgressDialog()
                }
            }
            else{
                profilePresenterView?.noInternet()
            }
        }
    }

    fun uploadProfilePic(userId: String,selectedPic:String?){
        if(NetworkUtil.isConnectingToInternet()){
            profilePresenterView?.showProgressDialog(R.string.uploading_profile)
            launch(Dispatchers.IO){
                val uploadUrlResult = uploadMedia.invoke(listOf(FileInfo(fileUri = selectedPic,type = FileHelper.getMimeType(selectedPic),name = File(selectedPic).name)),false)
                when(uploadUrlResult){
                    is Result.Success->{
                        val uploadProfile = updateUserDetail.invoke(userId,getRequestBody("profile",selectedPic = uploadUrlResult.data[0].uploadedUrl))
                        launch(Dispatchers.Main){
                            when(uploadProfile){
                                is Result.Success->profilePresenterView?.onSuccess(uploadProfile.data)
                                is Result.Error->profilePresenterView?.onFailure(uploadProfile.exception)
                            }
                        }
                    }
                    is Result.Error->{
                        launch(Dispatchers.Main){ profilePresenterView?.onFailure(uploadUrlResult.exception) }
                    }
                }
            }
        }
        else{profilePresenterView?.noInternet()}
    }

    fun disConnectStripe() = payoutPresenter.disconnectOAuth()

    fun getStripeStatus(accountId: String) = payoutPresenter.getStripeStatus(accountId)


    fun getCountries() {
        try {
            launch(Dispatchers.Main) {
                val call = async(Dispatchers.IO) { getCountry.invoke() }
                when (val result = call.await()) {
                    is Result.Success -> profilePresenterView?.onLoadCountries(result.data)
                    is Result.Error -> profilePresenterView?.onFailure(result.exception)
                }
            }
        } catch (exception: Exception) {
            profilePresenterView?.onFailure(AppError(exception.message))
        }
    }

    private fun isValid(isFor: String, inputOne: String?, inputTwo: String?,country:Country?):Boolean {
        var isValid = true
        when(isFor){
            "name"->{
                if(inputOne.isNullOrEmpty()){
                    isValid = false
                    profilePresenterView?.inputError(R.id.edOne,R.string.login_required)

                }
            }
            "email"->{
                if(inputOne.isNullOrEmpty()){
                    isValid = false
                    profilePresenterView?.inputError(R.id.edOne,R.string.login_required)

                }
                else if(!Utils.validateEmail(inputOne)){
                    isValid = false
                    profilePresenterView?.inputError(R.id.edOne,R.string.editprofile_invalid_email)
                }
            }
            "profile"->{}
            else->{
                 if (inputOne?.length != country?.mobileNumberLength) {
                    isValid = false
                     profilePresenterView?.inputError(R.id.edMobileNumber, R.string.invalid_no)
                }
            }
        }
        return isValid
    }

    private fun getRequestBody(isFor: String, inputOne: String? = Constant.EMPTY, inputTwo: String? =Constant.EMPTY,country: Country? = null,selectedPic: String?):HashMap<String,Any?>{
        val map = hashMapOf<String,Any?>()
        val user = hashMapOf<String,Any?>()
        when(isFor){
            "name"->{
                user["first_name"] = inputOne
                if(!inputTwo.isNullOrEmpty()){user["last_name"] = inputTwo}
            }
            "email"->{
                user["email"] = inputOne
            }
            "profile"-> user["profile_pic"] = selectedPic
            else->{
                user["mobile"] = inputTwo
                user["country_id"] = country?.id
            }
        }
        map["user"] = user
        return map
    }

    fun onDestroy() {
        job.cancel()
        profilePresenterView = null
    }

    override fun showProgressLoader(msg: Int) {
        profilePresenterView?.showProgressDialog(msg)
    }

    override fun hideProgressLoader() {
       profilePresenterView?.hideProgressDialog()
    }

    override fun onSuccessAccountLink(oAuthUrl: String) {

    }

    override fun onError(appError: AppError) {
       profilePresenterView?.onError(appError)
    }

    override fun onVerifySuccess(success: Boolean) {

    }

    override fun showProgressBar() {

    }

    override fun hideProgressBar() {

    }
    override fun onStripeStatus(
        isStripeOnBoardingConnected: Boolean,
        isPayoutEnabled: Boolean,
        errors: List<String>
    ) {
    }
    override fun onDisconnectSuccess() {
       profilePresenterView?.onDisconnectSuccess()
    }
}

