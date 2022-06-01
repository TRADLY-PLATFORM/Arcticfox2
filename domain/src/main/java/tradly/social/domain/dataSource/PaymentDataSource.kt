package tradly.social.domain.dataSource

import tradly.social.domain.entities.Payment
import tradly.social.domain.entities.Result

interface PaymentDataSource {
    fun getPaymentTypes(): Result<List<Payment>>
    fun getEphemeralKey(apiVersion:String):Result<String>
    fun getPaymentIntentSecret(orderReference:String):Result<String>
    fun sendSubscribeMail(accountId:String):Result<Boolean>
}