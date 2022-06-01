package tradly.social.common.network.feature.addEvent.datasource

import tradly.social.common.network.NetworkError
import tradly.social.common.network.base.BaseService
import tradly.social.common.network.converters.VariantConverter
import tradly.social.common.network.retrofit.VariantAPI
import tradly.social.domain.dataSource.VariantDataSource
import tradly.social.domain.entities.AppError
import tradly.social.domain.entities.Result
import tradly.social.domain.entities.Variant
import tradly.social.domain.entities.VariantType

class VariantDataSourceImpl:VariantDataSource,BaseService() {

    private val apiService = getRetrofitService(VariantAPI::class.java)

    override suspend fun addVariant(eventId: String, variant: Variant): Result<Variant> {
        return when(val result = apiCall(apiService.addVariant(getVariantQueryParams(variant),eventId))){
            is Result.Success->{
                if (result.data.status){
                    Result.Success(variant.also { it.id = result.data.data.variant.id.toString() })
                }
                else{
                    Result.Error(AppError(errorType = NetworkError.API_UNKNOWN_ERROR))
                }
            }
            is Result.Error->result
        }
    }

    override suspend fun getVariantTypes(): Result<List<VariantType>> =
        when(val result = apiCall(apiService.getVariantTypes())){
            is Result.Success->{
                if (result.data.status){
                    Result.Success(VariantConverter.mapFrom(result.data.variantTypeData.variantTypes))
                }
                else{
                    Result.Error(AppError(errorType = NetworkError.API_UNKNOWN_ERROR))
                }
            }
            is Result.Error->result

        }

    override suspend fun updateVariant(listingId:String,variant: Variant): Result<Boolean> =
        when(val result = apiCall(apiService.updateVariant(getVariantQueryParams(variant),listingId,variant.id))){
            is Result.Success->{
                if (result.data.status){
                    Result.Success(true)
                }
                else{
                    Result.Error(AppError(errorType = NetworkError.API_UNKNOWN_ERROR))
                }
            }
            is Result.Error->result
        }

    override suspend fun deleteVariant(listingId: String, variantId: String):Result<Boolean> =
        when(val result = apiCall(apiService.deleteVariant(listingId,variantId))){
            is Result.Success->{
                if (result.data.status){
                    Result.Success(true)
                }
                else{
                    Result.Error(AppError(errorType = NetworkError.API_UNKNOWN_ERROR))
                }
            }
            is Result.Error->result
        }

    private fun getVariantQueryParams(variant:Variant):HashMap<String,Any?>{
        val queryMap = hashMapOf<String,Any?>()
        queryMap["active"] = true
        queryMap["stock"] = variant.quantity
        queryMap["images"] = variant.images
        queryMap["title"] = variant.variantName
        queryMap["description"] = variant.variantDescription
        queryMap["list_price"] = variant.offerDisplayPrice
        queryMap["offer_percent"] = variant.offerPercent
        queryMap["variant_values"] = variant.values.map{ hashMapOf<String,Any?>("variant_type_id" to it.variantTypeId,"variant_type_value_id" to it.variantValueId) }
        val variantMap = hashMapOf<String,Any?>()
        variantMap["variant"] = queryMap
        return variantMap
    }

}