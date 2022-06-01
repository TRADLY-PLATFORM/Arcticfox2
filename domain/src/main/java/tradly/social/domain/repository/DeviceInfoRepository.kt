package tradly.social.domain.repository

import tradly.social.domain.dataSource.DeviceDataSource

class DeviceInfoRepository(val deviceDataSource: DeviceDataSource) {
    fun updateDeviceInfo(
        deviceName: String,
        deviceManufacturer: String,
        deviceModel: String,
        appVersion: String,
        osVersion: String,
        fcmToken: String?,
        language: String,
        clientType: Int
    ) = deviceDataSource.updateDeviceInfo(
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