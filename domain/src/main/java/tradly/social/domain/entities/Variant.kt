package tradly.social.domain.entities

data class Variant(
    var id: String=Constant.EMPTY,
    var quantity: Int=0,
    var listDisplayPrice:String= Constant.EMPTY,
    var offerDisplayPrice:String= Constant.EMPTY,
    var offerPercent:Int=0,
    var maxQuantity:Int=0,
    var quantityPerUnit:Int=0,
    var images:List<String> = mutableListOf(),
    var values: List<VariantProperties> = mutableListOf(),
    var variantValues:List<VariantProperties> = mutableListOf(), // for events
    var variantName:String=Constant.EMPTY,
    var variantValue: String = Constant.EMPTY,
    var variantDescription:String = Constant.EMPTY,
    var listPrice:Price = Price(),
    var offerPrice:Price = Price()
):BaseSelection(){
    fun getCombinedVariantValue() = values.joinToString("/"){ it.variantValue}
}

data class VariantProperties(
    var id: String = Constant.EMPTY,
    var variantTypeId:Int = 0,
    var variantType: String = Constant.EMPTY,
    var variantValue: String = Constant.EMPTY,
    var variantValueId:Int = 0,
    var colorCode: String = Constant.EMPTY,
    var isSelected:Boolean = false
)

data class VariantType(
    val id:Int,
    val name:String,
    val variantTypeValues:List<VariantType>,
    val isActive:Boolean
)