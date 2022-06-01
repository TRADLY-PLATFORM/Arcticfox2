package tradly.social.domain.entities

data class PromoBanner(
    var type: String = Constant.EMPTY,
    var reference: String = Constant.EMPTY,
    var imagePath: String = Constant.EMPTY,
    override val id: String = Constant.EMPTY,
    override val createdAt: Long = 0,
    override val updatedAt: Long = 0
) : BaseInfo