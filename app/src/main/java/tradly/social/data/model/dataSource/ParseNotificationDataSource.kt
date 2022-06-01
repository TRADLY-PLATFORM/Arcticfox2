package tradly.social.data.model.dataSource

import tradly.social.common.network.converters.NotificationConverter
import tradly.social.data.model.network.retrofit.RetrofitManager
import tradly.social.domain.dataSource.NotificationDataSource
import tradly.social.domain.entities.Notification
import tradly.social.domain.entities.Result

class ParseNotificationDataSource:NotificationDataSource{
    override fun getNotifications(pagination: Int):Result<List<Notification>> =
        when(val result = RetrofitManager.getInstance().getNotification(pagination)){
        is Result.Success-> Result.Success(NotificationConverter.mapFrom(result.data.data.activities))
        is Result.Error->result
    }
}