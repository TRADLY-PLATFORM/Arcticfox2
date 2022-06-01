package tradly.social.domain.repository

import tradly.social.domain.dataSource.NotificationDataSource

class NotificationRepository(private val notificationDataSource: NotificationDataSource){
    suspend fun getNotifications(pagination:Int)= notificationDataSource.getNotifications(pagination)
}