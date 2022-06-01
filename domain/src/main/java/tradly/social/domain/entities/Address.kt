package tradly.social.domain.entities

data class Address(
    var locality:String = Constant.EMPTY,
    var country:String = Constant.EMPTY,
    var postalCode:String = Constant.EMPTY,
    var formattedAddress:String = Constant.EMPTY,
    var city:String = Constant.EMPTY,
    var state:String = Constant.EMPTY,
    var countryCode:String = Constant.EMPTY,
    var geoPoint: GeoPoint = GeoPoint(),
    var type:String = Constant.EMPTY
)