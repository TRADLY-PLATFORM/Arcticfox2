package tradly.social.domain.entities

data class Set(
    var id: String,
    var title: String,
    var description: String,
    var listPrice: Int,
    var offerPrice: Int,
    var offerPercent:Int,
    var maxQuantity:Int,
    var active:Boolean,
    var variants: List<Variant>,
    var defaultImage:String=Constant.EMPTY
)