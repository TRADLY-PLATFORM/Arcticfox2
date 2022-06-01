package tradly.social.domain.repository

import tradly.social.domain.dataSource.PaymentDataSource
import tradly.social.domain.entities.Payment
import tradly.social.domain.entities.Result

class PaymentRepository(private val paymentDataSource: PaymentDataSource){
    suspend fun getPaymentTypes(): Result<List<Payment>> = paymentDataSource.getPaymentTypes()
    suspend fun getEphemeralKey(apiVersion:String) = paymentDataSource.getEphemeralKey(apiVersion)
    suspend fun getPaymentIntentSecret(orderReference:String) = paymentDataSource.getPaymentIntentSecret(orderReference)
    suspend fun sendSubscribeMail(accountId:String) = paymentDataSource.sendSubscribeMail(accountId)
}