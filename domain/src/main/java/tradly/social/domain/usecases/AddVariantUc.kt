package tradly.social.domain.usecases

import tradly.social.domain.entities.Variant
import tradly.social.domain.repository.VariantRepository

class AddVariantUc(private val variantRepository: VariantRepository) {
    suspend fun addVariant(eventId:String,variant:Variant) = variantRepository.addVariant(eventId,variant)
}