package tradly.social.domain.usecases

import tradly.social.domain.repository.DeviceInfoRepository

class UpdateDeviceInfo(private val deviceInfoRepository: DeviceInfoRepository) {
    suspend operator fun invoke(
        deviceName: String,
        deviceManufacturer: String,
        deviceModel: String,
        appVersion: String,
        osVersion: String,
        fcmToken: String?,
        language: String,
        clientType: Int
    ) = deviceInfoRepository.updateDeviceInfo(
        deviceName,
        deviceManufacturer,
        deviceModel,
        appVersion,
        osVersion,
        fcmToken,
        language,
        clientType
    )
}