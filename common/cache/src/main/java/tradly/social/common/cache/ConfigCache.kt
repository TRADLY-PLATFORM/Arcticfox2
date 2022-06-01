package tradly.social.common.cache

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import tradly.social.common.common.AppConstant
import tradly.social.common.persistance.shared.PreferenceSecurity
import tradly.social.common.util.parser.extension.toJsonObject
import java.lang.Exception

object ConfigCache {

    var hashMap = hashMapOf<String, Any>()

    init {
        val decrypted = PreferenceSecurity.getString(
            AppConstant.PREF_KEY_APP_SYNC_CONFIG)
        val appConfig = PreferenceSecurity.getDecryptedString(decrypted)
        updateMap(appConfig.toJsonObject())
    }

    private fun updateMap(appConfigJson: JSONObject) {
        hashMap.clear()
        update(appConfigJson)
    }

    private fun getPersistedAppConfig():JSONObject{
        val appConfig = PreferenceSecurity.getDecryptedString(
            PreferenceSecurity.getString(AppConstant.PREF_KEY_APP_SYNC_CONFIG))
        return appConfig.toJsonObject()
    }

    private fun updateKeyInMap(appConfigJson: JSONObject) = update(appConfigJson)

    private fun update(appConfigJson: JSONObject) {
        try {
            val iter = appConfigJson.keys()
            while (iter.hasNext()) {
                val key = iter.next()
                try {
                    val value = appConfigJson[key]
                    hashMap[key] = value
                } catch (e: JSONException) {
                }
            }
        } catch (ex: Exception) {
        }
    }

    @Synchronized
    fun persistConfigGroup(appConfig: Any) {
        val cacheConfig = getPersistedAppConfig()
        if(appConfig is JSONObject){
            parseJsonKeys(cacheConfig,appConfig)
        }
        else if(appConfig is JSONArray){
            for(i in 0 until (appConfig.length())){
                val any = appConfig.get(i)
                if(any is JSONObject){
                    parseJsonKeys(cacheConfig,any)
                }

            }
        }
        updateMap(cacheConfig)
        PreferenceSecurity.getEncryptedString(cacheConfig.toString()).also { encryptedString ->
            PreferenceSecurity.putString(AppConstant.PREF_KEY_APP_SYNC_CONFIG, encryptedString)
        }
    }

    @Synchronized
    fun persistConfigKey(key:String,value:Any) {
        val appConfigJson = getPersistedAppConfig()
        appConfigJson.put(key,value)
        updateKeyInMap(appConfigJson)
        PreferenceSecurity.getEncryptedString(appConfigJson.toString()).also { encryptedString ->
            PreferenceSecurity.putString(AppConstant.PREF_KEY_APP_SYNC_CONFIG, encryptedString)
        }
    }

    private fun parseJsonKeys(appConfigJson:JSONObject,resultJson:JSONObject):JSONObject{
        try {
            val iter = resultJson.keys()
            while (iter.hasNext()) {
                val key = iter.next()
                try {
                    val value = resultJson[key]
                    appConfigJson.put(key,value)
                } catch (e: JSONException) {
                }
            }
        }
        catch (ex:Exception){ }
        return appConfigJson
    }

    fun clearCache(){
        hashMap.clear()
        PreferenceSecurity.clearValue(AppConstant.PREF_KEY_APP_SYNC_CONFIG)
    }

}