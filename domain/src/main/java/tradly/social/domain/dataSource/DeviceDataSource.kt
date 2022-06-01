package tradly.social.domain.dataSource

interface DeviceDataSource{
    fun updateDeviceInfo(deviceName:String,
                         deviceManufacturer:String,
                         deviceModel:String,
                         appVersion:String,
                         osVersion:String,
                         fcmToken:String?,
                         language:String,
                         clientType:Int)
}