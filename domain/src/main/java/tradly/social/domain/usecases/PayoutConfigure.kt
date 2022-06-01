package tradly.social.domain.usecases

import tradly.social.domain.repository.PayoutRepository

class PayoutConfigure(val payoutRepository: PayoutRepository){
    suspend fun initStripeConnect() = payoutRepository.initStripeConnect()
    suspend fun getStripeAccountLink(accountId:String) = payoutRepository.getStripeAccountLink(accountId)
    suspend fun getStripeConnectStatus(accountId: String) = payoutRepository.getStripeConnectStatus(accountId)
    suspend fun getStripeLoginLink(accountId:String) = payoutRepository.getStripeLoginLink(accountId)
    suspend fun disConnectOAuth() = payoutRepository.disConnectOAuth()
    suspend fun verifyStripeOAuth(url:String) = payoutRepository.verifyStripeOAuth(url)
}