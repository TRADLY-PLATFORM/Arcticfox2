package tradly.social.ui.settings

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import tradly.social.R
import tradly.social.common.base.NetworkUtil
import tradly.social.common.base.Utils
import tradly.social.common.base.AppController
import tradly.social.data.model.dataSource.ParseUserAuthenticateDataSource
import tradly.social.common.base.ErrorHandler
import tradly.social.domain.entities.AppError
import tradly.social.domain.entities.Country
import tradly.social.domain.entities.Result
import tradly.social.domain.entities.User
import tradly.social.domain.repository.UserAuthenticateRepository
import tradly.social.domain.usecases.LogoutUser
import tradly.social.ui.base.BaseView
import tradly.social.ui.sellerProfile.ProfilePresenter


class SettingsPresenter(private var view: View?):ProfilePresenter.ProfilePresenterView {
    interface View: BaseView {
        fun onProfileUploadSuccess(user: User)
        fun onProfileUploadFailure(appError: AppError)
        fun onLogoutSuccess()
        fun noInternet()
    }

    var profilePresenter: ProfilePresenter
    var logout:LogoutUser

    init {
        profilePresenter = ProfilePresenter(this)
        val parseUserAuthenticateDataSource = ParseUserAuthenticateDataSource()
        val userAuthenticateRepository = UserAuthenticateRepository(parseUserAuthenticateDataSource)
        logout = LogoutUser(userAuthenticateRepository)
    }

    fun uploadProfile(selectedPic:String?){
        AppController.appController.getUser()?.let {
            profilePresenter.uploadProfilePic(it.id,selectedPic)
        }
    }

    fun logout(){
        if(NetworkUtil.isConnectingToInternet(true)){
            CoroutineScope(Dispatchers.Main).launch {
                view?.showProgressDialog(R.string.logging_out)
                val call = async(Dispatchers.IO){ logout.logoutUser(Utils.getAndroidID())}
                when(val result = call.await()){
                    is Result.Success->view?.onLogoutSuccess()
                    is Result.Error->onError(result.exception)
                }
                view?.hideProgressDialog()
            }
        }
    }
    override fun onFailure(appError: AppError) {
        view?.hideProgressDialog()
       view?.onProfileUploadFailure(appError)
    }


    override fun onLoadCountries(list: List<Country>?) {

    }

    override fun onDisconnectSuccess() {

    }

    override fun onSuccess(user: User) {
        view?.hideProgressDialog()
        view?.onProfileUploadSuccess(user)
    }

    override fun inputError(id: Int, msg: Int) {

    }

    override fun onError(appError: AppError) {
        ErrorHandler.handleError(exception = appError)
    }

    override fun hideProgressDialog() {

    }
    override fun showProgressDialog(msg: Int) {
        view?.showProgressDialog(msg)
    }
    override fun noInternet() {
        view?.noInternet()
    }

    fun onDestroy(){
        view = null
        profilePresenter.onDestroy()
    }
}
