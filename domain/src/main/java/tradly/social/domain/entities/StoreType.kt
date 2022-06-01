package tradly.social.domain.entities

data class StoreType(
    var name: String? = Constant.EMPTY, var type: Int = 0,
    override var id: String=Constant.EMPTY,
    override var createdAt: Long =0, override var updatedAt: Long = 0
) : BaseInfo