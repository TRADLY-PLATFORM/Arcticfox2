package tradly.social.domain.repository

import tradly.social.domain.dataSource.SubscriptionDataSource
import tradly.social.domain.entities.Result
import tradly.social.domain.entities.SubscriptionProduct

class SubscriptionRepository(private val subscriptionDataSource:SubscriptionDataSource){
    suspend fun getSubscriptionProducts(accountId:Int) = subscriptionDataSource.getSubscriptionProducts(accountId)
    suspend fun confirmSubscription(accountId: Int,subscriptionProductId:Int,purchaseToken:String) = subscriptionDataSource.confirmSubscription(accountId, subscriptionProductId, purchaseToken)
}