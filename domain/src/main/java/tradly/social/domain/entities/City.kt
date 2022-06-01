package tradly.social.domain.entities

data class City(
    var name: String?,
    var country: Country?,
    override val id: String = Constant.EMPTY,
    override val createdAt: Long = 0,
    override val updatedAt: Long = 0
) : BaseInfo