package tradly.social.domain.usecases

import tradly.social.domain.repository.PaymentRepository

class GetEphemeralKey(val paymentRepository: PaymentRepository){
    suspend fun getEphemeralKey(apiVersion:String) = paymentRepository.getEphemeralKey(apiVersion)
}