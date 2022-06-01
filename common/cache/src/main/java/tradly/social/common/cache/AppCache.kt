package tradly.social.common.cache

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import tradly.social.common.common.AppConstant
import tradly.social.common.persistance.shared.PreferenceSecurity
import tradly.social.common.util.parser.extension.toJson
import tradly.social.common.util.parser.extension.toList
import tradly.social.common.util.parser.extension.toObject
import tradly.social.domain.entities.*
import java.util.ArrayList

object AppCache {

    private var user:User?=null
    private var appConfig:AppConfig? = null
    private var userStoreList:List<Store>?=null
    private var deviceInfo:Device?=null

    fun getCacheUser() = user

    fun cacheUser(user: User){
        this.user = user
        PreferenceSecurity.putString(
            AppConstant.PREF_USER,
            PreferenceSecurity.getEncryptedString(user.toJson<User>())
        )
    }

    fun removeUser(){
        this.user = null
        this.userStoreList = null
        PreferenceSecurity.clearValue(AppConstant.PREF_USER_STORE)
        PreferenceSecurity.clearValue(AppConstant.PREF_USER)
        PreferenceSecurity.clearValue(AppConstant.PREF_LAST_CAPTURED_LOCATION)
        PreferenceSecurity.clearValue(AppConstant.PREF_APP_LOGIN_TYPE)
    }

    fun removeAppConfig(){
        this.appConfig = null
        PreferenceSecurity.clearValue(AppConstant.PREF_KEY_APP_CONFIG)
    }

    fun getCacheAppConfig():AppConfig?{
        if (appConfig == null){
            initAppConfig()
        }
        return appConfig
    }

    fun cacheAppConfig(appConfig: AppConfig){
        this.appConfig = appConfig
        val encString = PreferenceSecurity.getEncryptedString(appConfig.toJson<AppConfig>())
        PreferenceSecurity.putString(AppConstant.PREF_KEY_APP_CONFIG, encString)
    }

    fun initUser(){
        GlobalScope.launch(Dispatchers.IO) {
            val userString = PreferenceSecurity.getString(AppConstant.PREF_USER)
            if (userString.isNotEmpty()) {
                val userData = PreferenceSecurity.getDecryptedString(userString)
                user = userData.toObject<User>()
            }
        }
    }

    private fun initAppConfig(){
        val appConfigString = PreferenceSecurity.getString(AppConstant.PREF_KEY_APP_CONFIG)
        val appConfigData = PreferenceSecurity.getDecryptedString(appConfigString)
        appConfig = appConfigData.toObject<AppConfig>()
    }

    fun setUserStoreList(storeList: List<Store>){
        PreferenceSecurity.putStringWithEncrypt(AppConstant.PREF_USER_STORE, storeList.toJson<List<Store>>())
        this.userStoreList = storeList
    }

    fun getUserStore(storeId:String = Constant.EMPTY): Store? {
        userStoreList?.let {
            return if(it.size>0){
                val store = it[0]
                if(storeId.isNotEmpty()) if(store.id==storeId) store else null else it[0]
            } else null
        } ?: run {
            val userStoreString = PreferenceSecurity.getString(AppConstant.PREF_USER_STORE)
            if (userStoreString.isNotEmpty()) {
                userStoreList = PreferenceSecurity.getDecryptedString(userStoreString).toList<Store>() as ArrayList<Store>?
                userStoreList?.let { return if(it.size>0)it[0] else null }
            }
            return null
        }
    }

    fun cacheDeviceInfo(device: Device?){
        this.deviceInfo = device
        PreferenceSecurity.putString(AppConstant.DEVICE_INFO, PreferenceSecurity.getEncryptedString(device.toJson<Device>()))
    }

    fun getDeviceInfo() = this.deviceInfo
}