package tradly.social.domain.usecases

import tradly.social.domain.repository.VariantRepository

class DeleteVariantUc(private val variantRepository: VariantRepository) {
    suspend fun deleteVariant(listingId:String,variantId:String) = variantRepository.deleteVariant(listingId, variantId)

}
