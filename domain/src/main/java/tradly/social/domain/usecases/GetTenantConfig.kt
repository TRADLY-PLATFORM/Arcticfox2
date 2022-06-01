package tradly.social.domain.usecases

import tradly.social.domain.entities.Constant
import tradly.social.domain.repository.ParseConfigRepository

class GetTenantConfig(private val parseConfigRepository: ParseConfigRepository) {
    suspend operator fun invoke(tenantID:String = Constant.EMPTY) = parseConfigRepository.getParseConfig(tenantID)
    suspend fun syncTenantConfig(tenantID: String) = parseConfigRepository.syncTenantConfig(tenantID)
}