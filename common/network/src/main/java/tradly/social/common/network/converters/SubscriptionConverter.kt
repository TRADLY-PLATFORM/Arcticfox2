package tradly.social.common.network.converters

import tradly.social.common.network.feature.subscription.model.SubscriptionProductEntity
import tradly.social.domain.entities.SubscriptionProduct

class SubscriptionConverter {
    companion object {
        fun mapFrom(entity: SubscriptionProductEntity) =
            SubscriptionProduct(
                entity.id,
                entity.sku,
                entity.amount,
                entity.imagePath,
                entity.allowedListing,
                entity.feeFixedPerOrder,
                entity.commissionPercentPerOrder,
                entity.expiryDays,
                entity.type,
                entity.active,
                entity.title,
                entity.description,
                entity.subscriptionStatus,
            )

        fun mapFromList(list: List<SubscriptionProductEntity>) = list.map { mapFrom(it) }
    }
}