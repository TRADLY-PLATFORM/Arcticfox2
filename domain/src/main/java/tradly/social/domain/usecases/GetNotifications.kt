package tradly.social.domain.usecases

import tradly.social.domain.repository.NotificationRepository

class GetNotifications(private val notificationRepository: NotificationRepository){
    suspend fun getNotifications(pagination:Int=0) = notificationRepository.getNotifications(pagination)
}