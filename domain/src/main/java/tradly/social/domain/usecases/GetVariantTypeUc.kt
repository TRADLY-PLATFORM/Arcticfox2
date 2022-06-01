package tradly.social.domain.usecases

import tradly.social.domain.entities.VariantType
import tradly.social.domain.repository.VariantRepository

class GetVariantTypeUc(private val variantRepository: VariantRepository):BaseUseCase<List<VariantType>>() {
    override suspend fun execute() = variantRepository.getVariantTypes()
}