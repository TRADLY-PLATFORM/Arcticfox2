package tradly.social.domain.entities

data class BaseResult(
    var success: Boolean=false,
    var error: Int=0,
    val groups: ArrayList<Group> = arrayListOf(),
    var stores:ArrayList<Store> = arrayListOf(),
    var products:ArrayList<Product> = arrayListOf(),
    var promoBanners:ArrayList<PromoBanner> = arrayListOf(),
    var store:Store = Store(),
    var group:Group = Group(),
    var product:Product = Product(),
    var data:List<FileInfo>
)