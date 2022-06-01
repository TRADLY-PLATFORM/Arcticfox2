package tradly.social.domain.usecases

import tradly.social.domain.repository.ParseConfigRepository

class GetAppConfig (private val parseConfigRepository: ParseConfigRepository) {
    suspend fun getAppConfig(key:String) = parseConfigRepository.getAppConfig(key)
    suspend fun syncAppGroupConfig(groups:List<String>) = parseConfigRepository.syncAppGroupConfig(groups)
    suspend fun syncAppConfig(keys: List<String>) = parseConfigRepository.syncAppConfig(keys)
}