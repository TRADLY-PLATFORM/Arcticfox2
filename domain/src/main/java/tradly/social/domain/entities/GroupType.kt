package tradly.social.domain.entities

data class GroupType(
    var type: Int = 0,
    var name:String=Constant.EMPTY,
    var groupTypeImage: String = Constant.EMPTY,
    override var id: String=Constant.EMPTY,
    override val createdAt: Long=0,
    override val updatedAt: Long=0,
    var isSelected:Boolean=false
) : BaseInfo