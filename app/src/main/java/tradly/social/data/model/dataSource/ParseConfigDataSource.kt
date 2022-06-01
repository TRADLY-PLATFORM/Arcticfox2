package tradly.social.data.model.dataSource
import tradly.social.common.network.converters.AppConfigConverter
import tradly.social.data.model.network.retrofit.RetrofitManager
import tradly.social.common.base.ErrorHandler
import tradly.social.common.cache.AppCache
import tradly.social.common.network.base.ConfigAPIConstant
import tradly.social.domain.dataSource.ConfigDataSource
import tradly.social.domain.entities.AppConfig
import tradly.social.domain.entities.Result
import java.util.*


class ParseConfigDataSource : ConfigDataSource {
    override fun getTenantConfig(tenantID:String): Result<AppConfig> {
        return when (val parseAppConfig = RetrofitManager.getInstance().getParseConfig(tenantID.toLowerCase(Locale.getDefault())
        )) {
            is Result.Success -> {
                val appConfig = AppConfigConverter.mapFrom(parseAppConfig.data.data)
                AppCache.cacheAppConfig(appConfig)
                syncAppConfigGroup(ConfigAPIConstant.getGroupList())
                syncAppConfig(listOf(ConfigAPIConstant.GENERAL,ConfigAPIConstant.ON_BOARDING)) // It will sync configs need for Sign-up.
                Result.Success(appConfig)

            }
            is Result.Error -> parseAppConfig
        }

    }

    override fun syncTenantConfig(tenantID: String) {
        when (val parseAppConfig = RetrofitManager.getInstance().getParseConfig(tenantID.toLowerCase(Locale.getDefault()))) {
            is Result.Success -> {
                val appConfig = AppConfigConverter.mapFrom(parseAppConfig.data.data)
                AppCache.cacheAppConfig(appConfig)
            }
        }
    }

    override fun getAppConfig(key: String):Result<Any> =
        when(val result = RetrofitManager.getInstance().getAppConfig(key)){
            is Result.Success->Result.Success(AppConfigConverter.persistAppConfigKey(key,result.data))
            is Result.Error->result
        }

    override fun syncAppConfig(keys: List<String>) {
        when(val result = RetrofitManager.getInstance().getAppConfig(keys.joinToString(separator = ","))){
            is Result.Success->Result.Success(AppConfigConverter.persistConfigKeys(result.data))
            is Result.Error->result
        }
    }

    override fun syncAppConfigGroup(groups: List<String>) {
        when(val result = RetrofitManager.getInstance().getAppGroupConfig(groups.joinToString(separator = ","))){
            is Result.Success->AppConfigConverter.persistConfigKeys(result.data)
            is Result.Error-> ErrorHandler.handleError(exception = result.exception)
        }
    }

}