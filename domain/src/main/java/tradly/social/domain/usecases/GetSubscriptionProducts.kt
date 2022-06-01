package tradly.social.domain.usecases

import tradly.social.domain.repository.SubscriptionRepository

class GetSubscriptionProducts(private val subscriptionRepository: SubscriptionRepository) {
    suspend fun getSubscriptionProducts(accountId:Int) = subscriptionRepository.getSubscriptionProducts(accountId)
}