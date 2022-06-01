package tradly.social.common.network.converters

import tradly.social.common.network.entity.ActivitiesEntity
import tradly.social.domain.entities.Notification

class NotificationConverter {
    companion object{
        fun mapFrom(notificationList:List<ActivitiesEntity>) = mutableListOf<Notification>().apply {
            notificationList.forEach {
                val notification = mapFrom(it)
                this.add(notification)
            }
        }

        fun mapFrom(activitiesEntity: ActivitiesEntity) = Notification(
            user = UserModelConverter().mapFrom(activitiesEntity.user),
            store = StoreModelConverter.mapFrom(activitiesEntity.account),
            product = ProductConverter.mapFrom(activitiesEntity.listing),
            referenceType = activitiesEntity.referenceType,
            referenceId = activitiesEntity.referenceId,
            orderStatus = activitiesEntity.metadata.orderStatus,
            accountId = activitiesEntity.metadata.accountId.toString(),
            referenceNumber = activitiesEntity.metadata.referenceNumber,
            type = activitiesEntity.type,
            createdAt = activitiesEntity.createdAt*1000L
        )
    }
}