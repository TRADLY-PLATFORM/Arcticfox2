package tradly.social.domain.entities

data class Attribute(
    var id:Int =0,
    var name:String = Constant.EMPTY,
    var type:String = Constant.EMPTY,
    var fieldType:Int = 0,
    var optional:Boolean = false,
    var categories:List<Category> = listOf(),
    var attributeValues:List<Value> = listOf(),
    var attributeValueList: List<String> = listOf()
)

data class Value(
    val id:Int=0,
    val name: String = Constant.EMPTY,
    var any: Any?=null
)
