package tradly.social.domain.dataSource

import tradly.social.domain.entities.Result
import tradly.social.domain.entities.Variant
import tradly.social.domain.entities.VariantType

interface VariantDataSource {
    suspend fun getVariantTypes(): Result<List<VariantType>>
    suspend fun updateVariant(listingId:String,variant:Variant):Result<Boolean>
    suspend fun deleteVariant(listingId:String,variantId:String):Result<Boolean>
    suspend fun addVariant(eventId:String,variant: Variant):Result<Variant>
}