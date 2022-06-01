package tradly.social.domain.entities

data class CategoryTranslation(
    var text: String? = Constant.EMPTY,
    var locale: String? = Constant.EMPTY,
    var category: Category? = null,
    override val id: String = Constant.EMPTY,
    override val createdAt: Long = 0,
    override val updatedAt: Long = 0
) : BaseInfo