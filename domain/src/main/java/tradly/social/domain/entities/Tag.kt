package tradly.social.domain.entities

data class Tag(
    var name: String = Constant.EMPTY,
    override val id: String = Constant.EMPTY,
    override val createdAt: Long = 0,
    override val updatedAt: Long = 0
) : BaseInfo