package tradly.social.domain.entities
data class Brand(
    var name: String? = Constant.EMPTY,
    var active: Boolean = false,
    var imagePath: String? = Constant.EMPTY,
    var storeType: String? = Constant.EMPTY,
    override val id: String = Constant.EMPTY,
    override val createdAt: Long = 0,
    override val updatedAt: Long = 0
) : BaseInfo