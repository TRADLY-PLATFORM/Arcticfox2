package tradly.social.domain.entities

data class Collection(
    var id:String = Constant.EMPTY,
    var scopeType: Int = 0,
    var viewType:String = Constant.EMPTY,
    var backgroundColor:String = Constant.EMPTY,
    var backgroundUrl:String = Constant.EMPTY,
    var background: String = Constant.EMPTY,
    var title:String = Constant.EMPTY,
    var description:String = Constant.EMPTY,
    var imagePath:String = Constant.EMPTY,
    var stores: ArrayList<Store> = ArrayList(),
    var groups: ArrayList<Group> = ArrayList(),
    var brands: ArrayList<Brand> = ArrayList(),
    var products: ArrayList<Product> = ArrayList()
)