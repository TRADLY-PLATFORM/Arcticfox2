package tradly.social.domain.entities

data class Store(
    var user: User? = null,
    var storeName: String = Constant.EMPTY,
    var webAddress: String = Constant.EMPTY,
    var geoPoint: GeoPoint = GeoPoint(),
    var storeAddress:String = Constant.EMPTY,
    var type: Int = 0,
    var storeDescription: String = Constant.EMPTY,
    var followersCount: Int = 0,
    var listingsCount: Int = 0,
    var uniqueName:String = Constant.EMPTY,
    override var id: String = Constant.EMPTY,
    override val createdAt: Long = 0,
    override val updatedAt: Long = 0,
    var storePic:String = Constant.EMPTY,
    var userPic:String= Constant.EMPTY,
    var userName:String = Constant.EMPTY,
    var followed:Boolean = false,
    var active:Boolean = false,
    var userId:String = Constant.EMPTY,
    var address:Address = Address(),
    var categoryId:String = Constant.EMPTY,
    var category: Category? = null,
    var attributes:List<Attribute> = listOf(),
    var shippingMethods:List<ShippingMethod> = listOf(),
    var status:Int = 0,
    var subscription:Subscription = Subscription()
) : BaseInfo

data class Subscription(
    var isSubscriptionEnabled:Boolean = false,
    var subscriptionEnd:Long = 0,
    var subscriptionStarted: Long = 0
)