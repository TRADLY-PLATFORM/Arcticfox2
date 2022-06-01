package tradly.social.domain.repository

import tradly.social.domain.dataSource.ConfigDataSource

class ParseConfigRepository(private val parseConfigDataSource: ConfigDataSource) {
    suspend fun getParseConfig(tenantID:String) = parseConfigDataSource.getTenantConfig(tenantID)
    suspend fun syncTenantConfig(tenantID:String) = parseConfigDataSource.syncTenantConfig(tenantID)
    suspend fun getAppConfig(key:String) = parseConfigDataSource.getAppConfig(key)
    suspend fun syncAppConfig(keys: List<String>) = parseConfigDataSource.syncAppConfig(keys)
    suspend fun syncAppGroupConfig(groups:List<String>) = parseConfigDataSource.syncAppConfigGroup(groups)
}