package tradly.social.common.base

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentTransaction
import tradly.social.common.util.common.LocaleHelper
import tradly.social.domain.entities.Constant

open class BaseActivity : AppCompatActivity() {

    private var progressDialog: ProgressDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(ThemeUtil.getSelectedTheme())
        LocaleHelper.onAttach(this)
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase))
    }

    fun setToolbar(toolbar: Toolbar?, title: Int = 0, backNavIcon: Int = 0,titleTxt:String?=Constant.EMPTY) {
        toolbar?.let {
            if(title != 0){
                toolbar.setTitle(title)
            }
            if(!titleTxt.isNullOrEmpty()){
                toolbar.title = titleTxt
            }
            if(backNavIcon != 0){
                toolbar.setNavigationIcon(backNavIcon)
            }
            toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.colorWhite))
            setSupportActionBar(toolbar)
            toolbar.setNavigationOnClickListener { onBackPressed() }
        }
    }

    fun setToolbarTitle(toolbar: Toolbar?, title: Int , style:Int = R.style.Toolbar_Home_Regular){
        toolbar?.let {
            toolbar.setTitle(title)
            toolbar.setTitleTextAppearance(this,style)
        }
    }

    fun setToolbarTitle(toolbar: Toolbar?, title: String= Constant.EMPTY,style:Int = R.style.Toolbar_Home_Regular){
        toolbar?.let {
            toolbar.title = title
        }
    }

     fun commitTransaction(fragmentTransaction: FragmentTransaction){
        if(!supportFragmentManager.isStateSaved){
            fragmentTransaction.commit()
        }
    }

    fun showLoader(message: Int , title: Int = 0) {
        progressDialog?.let {
            if(!it.isShowing){
                createLoader(message, title)
            }
        }?:run{
            createLoader(message, title)
        }
    }

    protected fun isHomeLocationEnabled():Boolean{
        val homeLocationConfigEnabled = AppConfigHelper.getTenantConfigKey<Boolean>(AppConfigHelper.Keys.HOME_LOCATION_ENABLED)
        return(homeLocationConfigEnabled && tradly.social.common.persistance.shared.PreferenceSecurity.getBoolean(
            AppConstant.PREF_HOME_LOCATION_ENABLED,false) && PermissionHelper.checkPermission(this, PermissionHelper.Permission.LOCATION))
    }

    private fun createLoader(message: Int , title: Int = 0){
        progressDialog = ProgressDialog(this)
        if(title != 0){
            progressDialog?.setTitle(getString(title))
        }
        progressDialog?.setMessage(getString(message))
        progressDialog?.setCancelable(false)
        progressDialog?.show()
    }

    fun hideLoader() {
        progressDialog?.hide()
        progressDialog?.dismiss()
    }

    fun changeLoaderMessage(message: Int , title: Int){
       if(message != 0){
           progressDialog?.setMessage(getString(message))
       }

        if(title != 0){
            progressDialog?.setTitle(getString(message))
        }
    }
    fun <T>showLoginActivity(cls:Class<T>,requestCode:Int,isFor:String){
        startActivityForResult(cls, requestCode,Bundle().apply { putString("isFor", isFor) })
    }

    fun refreshActivity(activity: Activity){
        val intent = Intent(activity, activity.javaClass)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        activity.finish()
        activity.overridePendingTransition(0, 0)
        activity.startActivity(intent)
    }

    fun isUserLoggedIn() = AppController.appController.getUser() != null

    fun getLoggedInUser() = AppController.appController.getUser()


    fun isUserHavingStore() = AppController.appController.getUserStore()!= null

    override fun onBackPressed() {
        super.onBackPressed()
    }
}
