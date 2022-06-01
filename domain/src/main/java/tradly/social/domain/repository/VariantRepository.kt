package tradly.social.domain.repository

import tradly.social.domain.dataSource.VariantDataSource
import tradly.social.domain.entities.Variant

class VariantRepository(private val variantDataSource: VariantDataSource) {
    suspend fun addVariant(eventId:String,variant: Variant) = variantDataSource.addVariant(eventId, variant)
    suspend fun getVariantTypes() = variantDataSource.getVariantTypes()
    suspend fun updateVariant(listingId:String,variant: Variant) = variantDataSource.updateVariant(listingId, variant)
    suspend fun deleteVariant(listingId:String,variantId:String) = variantDataSource.deleteVariant(listingId, variantId)
}