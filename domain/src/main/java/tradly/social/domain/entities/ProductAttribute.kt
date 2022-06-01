package tradly.social.domain.entities

import tradly.social.domain.entities.Category
import tradly.social.domain.entities.Constant
import tradly.social.domain.entities.Value

data class ProductAttribute(
    var selectedValues:ArrayList<Value> = ArrayList(),
    var openValues:String = Constant.EMPTY,
    var id:Int =0,
    var name:String = Constant.EMPTY,
    var type:String = Constant.EMPTY,
    var fieldType:Int = 0,
    var categories:List<Category> = listOf(),
    var optional:Boolean = false,
    var attributeValues:List<Value> = listOf()
)