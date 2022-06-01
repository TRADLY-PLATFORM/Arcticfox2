package tradly.social.domain.entities

import tradly.social.domain.entities.Constant

data class Device(
    var deviceName: String=Constant.EMPTY,
    var deviceManufacturer:String = Constant.EMPTY,
    var deviceModel:String = Constant.EMPTY,
    var osType: Int=0,
    var appVersion: String=Constant.EMPTY,
    var osVersion: String=Constant.EMPTY,
    var locale: String=Constant.EMPTY,
    var fcmToken: String?=Constant.EMPTY,
    var clientType:Int= 2
)