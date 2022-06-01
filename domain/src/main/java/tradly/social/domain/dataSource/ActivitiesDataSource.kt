package tradly.social.domain.dataSource

import tradly.social.domain.entities.Notification
import tradly.social.domain.entities.Result

interface NotificationDataSource {
    fun getNotifications(pagination:Int):Result<List<Notification>>
}