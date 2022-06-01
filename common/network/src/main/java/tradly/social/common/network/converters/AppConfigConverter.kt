package tradly.social.common.network.converters

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import tradly.social.common.cache.ConfigCache
import tradly.social.common.network.entity.AppConfigEntity
import tradly.social.common.network.entity.ConfigEntity
import tradly.social.domain.entities.AppConfig
import tradly.social.domain.entities.Config
import tradly.social.domain.entities.Constant
import tradly.social.domain.entities.Key


class AppConfigConverter {
    companion object{
        fun mapFrom(from: AppConfigEntity): AppConfig =
            AppConfig().apply {
                authType = from.appConfig.authType
                key = Key(from.key.tenantKey)
                localeSupported = LocaleConverter.mapFromList(from.localeSupported)
                config = mapFrom(from.appConfig)
                module = from.module
            }

        fun mapFrom(configEntity: ConfigEntity) = Config(configEntity.authType,
            configEntity.branchLinkDescription,
            configEntity.faqUrl,
            configEntity.listingMapLocationSelectorEnabled,
            configEntity.accountMapLocationSelectorEnabled,
            configEntity.homeLocationEnabled,
            configEntity.enableFeedback,
            configEntity.branchLinkBaseUrl
        )

        fun persistAppConfigKey(requestedKey:String, apiConfig:String):Any{
            try{
                val json = JSONObject(apiConfig)
                val data = json.getJSONObject("data")
                val configs = data.opt("configs")
                if(configs is JSONObject){
                    val value = configs.opt(requestedKey)
                    if(value!=null){
                        ConfigCache.persistConfigKey(requestedKey,value)
                        return value
                    }
                }
            }catch (ex:Exception){}
            return Constant.EMPTY
        }


        fun persistConfigKeys(apiConfig: String){
            try{
                val json = JSONObject(apiConfig)
                val data = json.getJSONObject("data")
                val configs = data.opt("configs")
                ConfigCache.persistConfigGroup(configs)
            }catch (ex:Exception){}
        }


    }
}