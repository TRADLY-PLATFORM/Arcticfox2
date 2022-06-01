package tradly.social.domain.dataSource

import tradly.social.domain.entities.Result
import tradly.social.domain.entities.SubscriptionProduct

interface SubscriptionDataSource {
    suspend fun getSubscriptionProducts(accountId:Int):Result<List<SubscriptionProduct>>
    suspend fun confirmSubscription(accountId: Int,subscriptionProductId:Int,purchaseToken:String):Result<Boolean>
}