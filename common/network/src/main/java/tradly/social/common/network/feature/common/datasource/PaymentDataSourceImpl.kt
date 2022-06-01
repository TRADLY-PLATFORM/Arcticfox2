package tradly.social.common.network.feature.common.datasource

import tradly.social.common.network.converters.PaymentConverter
import tradly.social.common.network.CustomError
import tradly.social.common.network.base.BaseService
import tradly.social.common.network.retrofit.PaymentAPI
import tradly.social.domain.dataSource.PaymentDataSource
import tradly.social.domain.entities.AppError
import tradly.social.domain.entities.Payment
import tradly.social.domain.entities.Result

class PaymentDataSourceImpl:PaymentDataSource,BaseService() {

    val apiService = getRetrofitService(PaymentAPI::class.java)

    override fun getPaymentTypes(): Result<List<Payment>> = when (val result = apiCall(apiService.getPaymentTypes())) {
        is Result.Success -> Result.Success(PaymentConverter.mapFromList(result.data.responseData.paymentMethodList))
        is Result.Error -> result
    }

    override fun getEphemeralKey(apiVersion: String): Result<String> {
        return when(val result = apiCall(apiService.getEphemeralKey(hashMapOf("api_version" to apiVersion)))){
            is Result.Success-> PaymentConverter.getEphemeralJsonString(result.data.string())
            is Result.Error->result
        }
    }

    override fun getPaymentIntentSecret(orderReference: String):Result<String> {
        return when (val result = apiCall(apiService.getPaymentIntent(getRequestBody(orderReference)))) {
            is Result.Success -> PaymentConverter.getParsedResponse(result.data.string())
            is Result.Error ->result
        }
    }

    override fun sendSubscribeMail(accountId:String):Result<Boolean> {
        return when(val result = apiCall(apiService.subscription(getSubscriptionRequestBody(accountId)))){
            is Result.Success->{
                val status = result.data.status
                if(status){
                    Result.Success(status)
                }
                else{
                    Result.Error(AppError(code = CustomError.SUBSCRIPTION_TRIGGER_MAIL_FAILED))
                }
            }
            is Result.Error->result
        }
    }

    private fun getRequestBody(orderReference:String) = hashMapOf<String,Any?>("order_reference" to orderReference)

    private fun getSubscriptionRequestBody(accountId:String) = hashMapOf<String,String>("account_id" to accountId)
}