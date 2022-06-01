package tradly.social.domain.dataSource

import tradly.social.domain.entities.AppConfig
import tradly.social.domain.entities.Result

interface ConfigDataSource {
    fun getTenantConfig(tenantID:String):Result<AppConfig>
    fun syncTenantConfig(tenantID:String)
    fun getAppConfig(key: String):Result<Any>
    fun syncAppConfig(keys: List<String>)
    fun syncAppConfigGroup(groups:List<String>)
}