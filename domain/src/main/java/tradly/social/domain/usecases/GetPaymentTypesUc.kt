package tradly.social.domain.usecases

import tradly.social.domain.repository.PaymentRepository

class GetPaymentTypesUc(private val paymentRepository: PaymentRepository){
    suspend fun getPaymentTypes() = paymentRepository.getPaymentTypes()
}