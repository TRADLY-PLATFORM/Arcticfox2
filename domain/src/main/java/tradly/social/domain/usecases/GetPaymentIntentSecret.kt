package tradly.social.domain.usecases

import tradly.social.domain.repository.PaymentRepository

class GetPaymentIntentSecret(val paymentRepository: PaymentRepository){
    suspend fun getPaymentIntentSecret(orderReference:String) = paymentRepository.getPaymentIntentSecret(orderReference)
}