package tradly.social.common.base


import android.app.ProgressDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup


/**
 * A simple [Fragment] subclass.
 */
open class BaseFragment : Fragment() {

    private var progressDialog: ProgressDialog? = null
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    fun showLoader(message: Int) {
        progressDialog = ProgressDialog(requireContext())
        progressDialog?.setMessage(getString(message))
        progressDialog?.setCancelable(false)
        progressDialog?.show()
    }

    fun hideLoader() {
        progressDialog?.hide()
        progressDialog?.dismiss()
    }

    fun checkAndUpdateApp(appVersion:Int){
        if(appVersion>BuildConfig.VERSION_CODE){
            Utils.showAppUpdateDialog(requireContext())
        }
    }

    protected fun isHomeLocationEnabled():Boolean{
        val homeLocationConfigEnabled = AppConfigHelper.getTenantConfigKey<Boolean>(AppConfigHelper.Keys.HOME_LOCATION_ENABLED)
        return(homeLocationConfigEnabled && tradly.social.common.persistance.shared.PreferenceSecurity.getBoolean(
            AppConstant.PREF_HOME_LOCATION_ENABLED,false) && PermissionHelper.checkPermission(requireContext(), PermissionHelper.Permission.LOCATION))
    }

    fun isUserLoggedIn() = AppController.appController.getUser() != null

    fun getLoggedInUser() = AppController.appController.getUser()

    fun isUserHavingStore() = AppController.appController.getUserStore()!= null

}
