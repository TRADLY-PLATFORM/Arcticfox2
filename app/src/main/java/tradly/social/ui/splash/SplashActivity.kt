package tradly.social.ui.splash

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_splash.*
import kotlinx.android.synthetic.main.activity_splash.btnLogin
import kotlinx.android.synthetic.main.activity_splash.parentLayout
import kotlinx.android.synthetic.main.activity_splash.txtWelcome
import tradly.social.R
import tradly.social.common.base.*
import tradly.social.BuildConfig
import tradly.social.common.resources.ResourceConfig
import tradly.social.common.util.common.LocaleHelper
import tradly.social.domain.entities.AppConfig
import tradly.social.domain.entities.AppError
import tradly.social.common.base.BaseActivity
import tradly.social.ui.intro.IntroActivity
import tradly.social.ui.locale.LocaleActivity
import tradly.social.ui.main.MainActivity

class SplashActivity : BaseActivity(), SplashPresenter.View {

    private val colorPrimary:Int by lazy{ ThemeUtil.getResourceDrawable(this, R.attr.colorPrimary)}
    private lateinit var splashPresenter: SplashPresenter
    private val isPlatformApp = AppController.appController.isPlatformApp()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        initView()
        splashPresenter = SplashPresenter(this)
        splashPresenter.getParseConfig(ResourceConfig.TENANT_ID)
        if(!isPlatformApp){
            splashLayout.visibility = View.VISIBLE
            tenantLayout.visibility = View.GONE
            if(BuildConfig.SHOW_SPLASH_LOGO_NAME){
                splashLogoName.visibility = View.VISIBLE
                splashLogoName.setImageResource(R.drawable.splash_name)
            }
        }
        else{
            splashLayout.visibility = View.GONE
            tenantLayout.visibility = View.VISIBLE
            txtWelcome.text = String.format(getString(R.string.splash_welcome_to_message),getString(R.string.app_label))
            btnLogin.safeClickListener {
                Utils.hideKeyBoard(this)
                if(splashPresenter.isValid(edTenantID.getString())){
                    splashPresenter.getTenantConfig(edTenantID.getString())
                }
            }
        }
    }

    //TODO Need Dynamic BG
    private fun initView(){
        parentLayout.setBackgroundColor(ContextCompat.getColor(this, colorPrimary))
        txtPoweredBy?.visibility = if(BuildConfig.FLAVOR.equals("client",true)) View.GONE else View.VISIBLE
    }
    override fun onConfigComplete(appConfig: AppConfig) {
        if(!tradly.social.common.persistance.shared.PreferenceSecurity.getBoolean(AppConstant.PREF_KEY_INTRO,false) && (tradly.social.common.persistance.shared.PreferenceSecurity.getBoolean(
                AppConstant.PREF_INTRO_CONFIG_FETCHED,false) || appConfig.localeSupported.size>1)){
            if(!tradly.social.common.persistance.shared.PreferenceSecurity.getBoolean(AppConstant.PREF_APP_INTRO_LANG_SELECTED,false)){
                if(appConfig.localeSupported.size>1){
                    launchScreen(LocaleActivity::class.java)
                    return
                }
                else{
                    LocaleHelper.initialize()
                }
            }
            launchScreen(IntroActivity::class.java)
        }
        else{
            launchScreen(MainActivity::class.java)
        }
    }

    override fun onFailure(appError: AppError) {
        hideProgressLoader()
        ErrorHandler.handleError(this, appError)
    }

    private fun <T> launchScreen(cls: Class<T>) {
        val intent = Intent(this, cls)
        startActivity(intent)
        this.finish()
    }

    override fun showIntroScreens() {
        launchScreen(IntroActivity::class.java)
    }

    override fun noInternet() {
        Utils.showAlertDialog(
            this,
            getString(R.string.no_internet),
            getString(R.string.no_internet_warning_message),
            false,
            false,
            object : Utils.DialogInterface {
                override fun onAccept() {
                }

                override fun onReject() {
                }
            })
    }

    override fun showProgressLoader() {
        if(isPlatformApp){
            showLoader(R.string.please_wait)
        }
        else{
            progress_circular?.visibility = View.VISIBLE
        }
    }

    override fun hideProgressLoader() {
        if(isPlatformApp){
            hideLoader()
        }
        else{
            progress_circular?.visibility = View.INVISIBLE
        }
    }

    override fun invalidInput(resId: Int) {
        edTenantID.error = getString(R.string.splash_required)
    }
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        this.intent = intent
        BranchHelper.branchSessionReInit(this,intent?.data)
    }
    override fun onDestroy() {
        super.onDestroy()
        splashPresenter.onDestroy()
    }

}
