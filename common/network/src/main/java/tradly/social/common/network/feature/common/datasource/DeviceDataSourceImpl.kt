package tradly.social.common.network.feature.common.datasource

import tradly.social.common.cache.AppCache
import tradly.social.common.network.base.BaseService
import tradly.social.common.network.retrofit.DeviceAPI
import tradly.social.domain.entities.Device
import tradly.social.domain.dataSource.DeviceDataSource
import tradly.social.domain.entities.Result

class DeviceDataSourceImpl : DeviceDataSource,BaseService() {

    val apiService = getRetrofitService(DeviceAPI::class.java)

    override fun updateDeviceInfo(
        deviceName: String,
        deviceManufacturer: String,
        deviceModel: String,
        appVersion: String,
        osVersion: String,
        fcmToken: String?,
        language: String,
        clientType: Int
    ) {
        val map = getPayload(deviceName, deviceManufacturer, deviceModel, appVersion, osVersion, fcmToken, language, clientType)
        when(apiCall(apiService.updateDeviceDetails(map))){
            is Result.Success-> {
                val device = Device(deviceName,deviceManufacturer,deviceModel,appVersion = appVersion,osVersion = osVersion,fcmToken = fcmToken,locale = language,clientType = clientType)
                AppCache.cacheDeviceInfo(device)
            }
        }
    }

    private fun getPayload(
        deviceName: String,
        deviceManufacturer: String,
        deviceModel: String,
        appVersion: String,
        osVersion: String,
        fcmToken: String?,
        language: String,
        clientType: Int
    ): HashMap<String, Any?> {
        val deviceInfo = hashMapOf<String,Any?>()
        deviceInfo["device_name"] = deviceName
        deviceInfo["device_manufacturer"] = deviceManufacturer
        deviceInfo["device_model"] = deviceModel
        deviceInfo["app_version"] = appVersion
        deviceInfo["os_version"] = osVersion
        deviceInfo["push_token"] = fcmToken
        deviceInfo["language"] = language
        deviceInfo["client_type"] = clientType
        return hashMapOf("device_info" to deviceInfo)
    }
}