package tradly.social.common.network.converters


import tradly.social.common.network.CustomError
import tradly.social.common.network.entity.PaymentMethodEntity
import tradly.social.common.util.parser.extension.toJsonObject
import tradly.social.domain.entities.AppError
import tradly.social.domain.entities.Payment
import tradly.social.domain.entities.Result

object PaymentConverter {
    fun mapFrom(paymentMethodEntity: PaymentMethodEntity) =
         Payment(
             paymentMethodEntity.id,
             paymentMethodEntity.name,
             paymentMethodEntity.logoPath,
             paymentMethodEntity.type,
             paymentMethodEntity.channel,
             paymentMethodEntity.default,
             paymentMethodEntity.minAmount
        )

    fun mapFromList(list: List<PaymentMethodEntity>):List<Payment> {
        val mList = mutableListOf<Payment>()
        list.forEach { mList.add(mapFrom(it))}
        return mList
    }

    fun getEphemeralJsonString(res:String):Result<String>{
        return try{
            val json = res.toJsonObject()
            if(json.getBoolean("status")){
                Result.Success(json.getJSONObject("data").getJSONObject("ephemeral_key").toString())
            } else{
                Result.Error(exception = AppError(code = CustomError.INIT_PAYMENT_FAILED))
            }
        }catch (ex:Exception){
            Result.Error(exception = AppError(code = CustomError.INIT_PAYMENT_FAILED))
        }
    }

    fun getParsedResponse(response:String):Result<String>{
        return try{
            val json = response.toJsonObject()
            if(json.getBoolean("status")){
                Result.Success(json.getJSONObject("data").getString("client_secret"))
            } else{
                Result.Error(exception = AppError(code = CustomError.STRIPE_PAYMENT_INTENT_FETCH_FAILED))
            }
        }catch (ex:Exception){
            Result.Error(exception = AppError(code = CustomError.STRIPE_PAYMENT_INTENT_FETCH_FAILED))
        }
    }
}