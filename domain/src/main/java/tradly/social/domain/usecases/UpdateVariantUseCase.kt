package tradly.social.domain.usecases

import tradly.social.domain.entities.Variant
import tradly.social.domain.repository.VariantRepository

class UpdateVariantUseCase(private val variantRepository: VariantRepository) {
    suspend fun updateVariant(listingId:String,variant: Variant) = variantRepository.updateVariant(listingId, variant)

}