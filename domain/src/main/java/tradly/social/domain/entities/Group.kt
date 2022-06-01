package tradly.social.domain.entities

data class Group(
    var groupName:String? = Constant.EMPTY,
    var createdBy:User? = null,
    var groupPic:String?= Constant.EMPTY,
    var location:GeoPoint?= null,
    var groupDescription:String? = Constant.EMPTY,
    var membersCount:Int = 0,
    var category:Category? = null,
    var archievd:Boolean=false,
    var blocked:Boolean =false,
    override val id: String=Constant.EMPTY,
    override val createdAt: Long = 0,
    override val updatedAt: Long = 0,
    var groupAddress:String? = Constant.EMPTY,
    var listing:List<Product>? = null,
    var isSelected :Boolean = false,
    var joined:Boolean = false,
    var groupType:Int?=null
):BaseInfo