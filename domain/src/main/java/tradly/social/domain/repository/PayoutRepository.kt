package tradly.social.domain.repository

import tradly.social.domain.dataSource.PayoutDataSource

class PayoutRepository(private val payoutDataSource: PayoutDataSource){
    suspend fun initStripeConnect() = payoutDataSource.initStripeConnect()
    suspend  fun getStripeAccountLink(accountId:String) = payoutDataSource.getStripeAccountLink(accountId)
    suspend fun getStripeConnectStatus(accountId: String) = payoutDataSource.getStripeStatus(accountId)
    suspend fun getStripeLoginLink(accountId:String) = payoutDataSource.getStripeLoginLink(accountId)
    suspend fun disConnectOAuth() = payoutDataSource.disConnectOAuth()
    suspend fun verifyStripeOAuth(url:String)= payoutDataSource.verifyStripeOAuth(url)
}