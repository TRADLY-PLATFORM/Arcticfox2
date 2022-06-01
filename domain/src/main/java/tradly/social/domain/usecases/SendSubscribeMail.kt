package tradly.social.domain.usecases

import tradly.social.domain.repository.PaymentRepository

class SendSubscribeMail(val paymentRepository: PaymentRepository) {
    suspend fun sendMail(accountId:String) = paymentRepository.sendSubscribeMail(accountId)
}