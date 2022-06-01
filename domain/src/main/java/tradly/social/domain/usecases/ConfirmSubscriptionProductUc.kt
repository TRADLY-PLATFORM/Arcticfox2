package tradly.social.domain.usecases

import tradly.social.domain.repository.SubscriptionRepository

class ConfirmSubscriptionProductUc(private val subscriptionRepository: SubscriptionRepository) {
    suspend fun confirmSubscription(accountId: Int,subscriptionProductId:Int,purchaseToken:String) = subscriptionRepository.confirmSubscription(accountId, subscriptionProductId, purchaseToken)
}