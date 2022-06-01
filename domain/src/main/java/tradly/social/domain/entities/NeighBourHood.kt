package tradly.social.domain.entities

data class NeighBourHood(
    var name: String?,
    var city: City?,
    override val id: String = Constant.EMPTY,
    override val createdAt: Long = 0,
    override val updatedAt: Long = 0
) : BaseInfo