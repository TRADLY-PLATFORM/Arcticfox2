package tradly.social.domain.entities

data class Category(
    var categotyTranslation:CategoryTranslation? = null,
    var active:Boolean = false,
    var orderBy:Int = 0,
    var imagePath:String? = Constant.EMPTY,
    var category: Category? = null,
    var hasSubCategory:Boolean = false,
    var isMore:Boolean = false,
    var name:String = Constant.EMPTY,
    val parent:Int = 0,
    val subCategory:List<Category> = listOf(),
    val hierarchy:List<CategoryHierarchy> = listOf(),
    override val id: String = Constant.EMPTY,
    override val createdAt: Long = 0,
    override val updatedAt: Long = 0,
    var isSelected:Boolean = false,
    var isExpanded:Boolean = false
) : BaseInfo


data class CategoryHierarchy(
    val id:Int=0,
    val name: String = Constant.EMPTY,
    val level:Int = 0
)