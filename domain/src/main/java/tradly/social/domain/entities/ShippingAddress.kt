package tradly.social.domain.entities

data class ShippingAddress(
    var defaultAddress:Boolean,
    var name: String,
    var phoneNumber: String,
    var lane1: String,
    var lane2:String,
    var landmark: String?,
    var city: String,
    var country:String,
    var formattedAddress:String = Constant.EMPTY,
    var type:String,
    var province: String,
    var zipcode: String,
    override var id: String = Constant.EMPTY,
    override val createdAt: Long = 0,
    override val updatedAt: Long = 0
) : BaseInfo

data class ShippingMethod(
    var id:Int,
    var name:String,
    var logo:String,
    var type:String,
    var default:Boolean
)