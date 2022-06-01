package tradly.social.common.base

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.SystemClock
import androidx.multidex.MultiDex
import com.rollbar.android.Rollbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import tradly.social.common.cache.AppCache
import tradly.social.common.font.FontManager
import tradly.social.common.persistance.shared.PreferenceSecurity
import tradly.social.common.util.parser.extension.toObject
import tradly.social.domain.entities.*
import java.util.*


class AppController : Application() {

    private var mLastClickTime: Long = 0
    private var homeData: Home? = null
    private var device: Device?=null
    var currentChatRoomId:String? = AppConstant.EMPTY

    companion object {
        lateinit var appContext: Context
        lateinit var appController: AppController
    }

    override fun onCreate() {
        super.onCreate()
        appContext = this
        appController = this
        this.registerActivityLifecycleCallbacks(ActivityLifecycle())
        init()
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    @SuppressLint("MissingPermission")
    private fun init(){
        PreferenceSecurity.init(this)
        initUser()
        EventHelper.init(this)
        BranchHelper.init()
        FontManager.init(this)
        if (getString(R.string.admob_appid).isNotEmpty()){
          //  MobileAds.initialize(appContext)
        }
        Rollbar.init(this,getString(R.string.rollbar_access_token),BuildConfig.BUILD_TYPE)
        resetPreference()
    }

    private fun initUser(){
        GlobalScope.launch(Dispatchers.IO) {
            val userString = PreferenceSecurity.getString(AppConstant.PREF_USER)
            if (userString.isNotEmpty()) {
                val userData = PreferenceSecurity.getDecryptedString(userString)
                userData.toObject<User>()?.let { AppCache.cacheUser(it) }
            }
        }
    }

    fun getUser(): User? = AppCache.getCacheUser()

    fun getAppConfig() = AppCache.getCacheAppConfig()

    fun getUserStore(storeId:String = Constant.EMPTY) = AppCache.getUserStore(storeId)

    fun isStripeConnected():Boolean{
        val user = AppController.appController.getUser()
        if(user!=null){
            val stripeAccountType = AppConfigHelper.getConfigKey<String>(AppConfigHelper.Keys.STRIPE_CONNECT_ACCOUNT_TYPE)
            if(AppConstant.StripeConnectAccountType.EXPRESS == stripeAccountType && user.isStripeOnboardConnected){
                return true
            }
            else if(AppConstant.StripeConnectAccountType.STANDARD == stripeAccountType && user.isStripeConnected){
                return true
            }
            return false
        }
        return false
    }

    fun setHomeData(home: Home){
        this.homeData = home
    }

    fun getHome() = homeData

    fun getBusinessType() = AppCache.getCacheAppConfig()?.flavour?:0

    fun isPlatformApp() = BuildConfig.FLAVOR == "tradlyPlatform"

    fun getCurrentTenantID() = if(isPlatformApp()){
        PreferenceSecurity.getString(AppConstant.PREF_CURRENT_TENANT_ID)
    } else{
        BuildConfig.TENANT_ID
    }

    fun cacheUserData(user: User) {
        AppCache.cacheUser(user)
    }

    //region App data clear
    fun clearUserData() {
        AppCache.removeUser()
    }

    /*
    *  It should call only for platform app.
    */
    fun clearUserDataWithConfig() {
        AppCache.removeUser()
        AppCache.removeAppConfig()
        PreferenceSecurity.clearValue(AppConstant.PREF_KEY_INTRO)
        PreferenceSecurity.clearValue(AppConstant.PREF_CURRENT_TENANT_ID)
        PreferenceSecurity.clearValue(preferenceConstant.PREF_CURRENT_CURRENCIES)
        PreferenceSecurity.clearValue(AppConstant.PREF_APP_INTRO_LANG_SELECTED)
        AppConfigHelper.clearConfigs()
    }

    fun clearHome(){
        homeData = null
    }

    //endregion

    private fun resetPreference() {
        PreferenceSecurity.putBoolean(AppConstant.PREF_SETTINGS_CHANGED, false)
    }

    fun shouldAllowToClick(): Boolean {
        var shouldAllow = true
        if (SystemClock.elapsedRealtime() - mLastClickTime < 500) {
            shouldAllow = false
        }
        mLastClickTime = SystemClock.elapsedRealtime()
        return shouldAllow
    }
}