package tradly.social.domain.dataSource

import tradly.social.domain.entities.Payout
import tradly.social.domain.entities.Result

interface PayoutDataSource {
    fun initStripeConnect():Result<Payout>
    fun getStripeAccountLink(accountId:String):Result<Payout>
    fun getStripeStatus(accountId: String):Result<Payout>
    fun getStripeLoginLink(accountId:String):Result<Payout>
    fun disConnectOAuth():Result<Boolean>
    fun verifyStripeOAuth(url:String):Result<Boolean>
}