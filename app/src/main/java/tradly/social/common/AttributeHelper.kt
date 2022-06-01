package tradly.social.common

import tradly.social.common.base.AppConstant
import tradly.social.domain.entities.*

class AttributeHelper {
    companion object{
        fun getAttributeValues(attributeList: List<ProductAttribute>?):List<HashMap<String,Any?>>{
            val list = mutableListOf<HashMap<String,Any?>>()
            attributeList?.forEach { item ->
                when(item.fieldType){
                    AppConstant.AttrTypes.SINGLE_SELECT,
                    AppConstant.AttrTypes.MULTI_SELECT->{
                        if(item.selectedValues.isNotEmpty()){
                            val attrValue = hashMapOf<String,Any?>()
                            attrValue["id"] = item.id
                            attrValue["values"] = item.selectedValues.map { it.id }
                            list.add(attrValue)
                        }
                    }
                    AppConstant.AttrTypes.UPLOAD_FIELD->{
                        if(item.selectedValues.isNotEmpty()){
                            val attrValue = hashMapOf<String,Any?>()
                            attrValue["id"] = item.id
                            attrValue["values"] = item.selectedValues.map { (it.any as FileInfo).fileUri }
                            list.add(attrValue)
                        }
                    }
                    AppConstant.AttrTypes.OPEN_VALUE,
                    AppConstant.AttrTypes.OPEN_VALUES->{
                        if(item.openValues.isNotEmpty()){
                            val attrValue = hashMapOf<String,Any?>()
                            attrValue["id"] = item.id
                            attrValue["values"] = item.openValues.split(",")
                            list.add(attrValue)
                        }
                    }
                }
            }
            return list
        }

        fun validateAttribute(attributeList: List<ProductAttribute>?, result:(isValidAttribute:Boolean, fieldName:String?)->Unit){
            var isValid = true
            var fieldName = Constant.EMPTY
            attributeList?.apply {
                for (it in this){
                    if(!it.optional){
                        when(it.fieldType){
                            AppConstant.AttrTypes.SINGLE_SELECT,
                            AppConstant.AttrTypes.MULTI_SELECT,
                            AppConstant.AttrTypes.UPLOAD_FIELD->{
                                if(it.selectedValues.isEmpty()){
                                    isValid = false
                                    fieldName = it.name
                                    return
                                }
                            }
                            AppConstant.AttrTypes.OPEN_VALUE,
                            AppConstant.AttrTypes.OPEN_VALUES->{
                                if(it.openValues.isEmpty()){
                                    isValid = false
                                    fieldName = it.name
                                    return
                                }
                            }
                        }
                    }
                }
            }
            result(isValid, fieldName)
        }

        private fun mapFrom(from:List<Attribute>):List<ProductAttribute>{
            val mList = ArrayList<ProductAttribute>()
            from.forEach { i->
                    ProductAttribute().apply {
                        id = i.id
                        name = i.name
                        type = i.type
                        fieldType = i.fieldType
                        categories = i.categories
                        optional = i.optional
                        attributeValues = i.attributeValues
                    }.also {
                        mList.add(it)
                    }
            }
            return mList
        }

        /**
         *  Attribute mappings for Listings
         */

        fun mapFrom(from:List<Attribute>, selectedAttributes:List<Attribute>? = listOf()):List<ProductAttribute>{
            val mList = mapFrom(from)
            if(selectedAttributes != null && selectedAttributes.isNotEmpty()){
                mList.forEach { productAttr->
                    selectedAttributes.find { a->a.id == productAttr.id }?.let {
                        when(it.fieldType){
                            AppConstant.AttrTypes.SINGLE_SELECT,
                            AppConstant.AttrTypes.MULTI_SELECT->{
                                productAttr.selectedValues = it.attributeValues as ArrayList<Value>
                            }
                            AppConstant.AttrTypes.OPEN_VALUE->{
                                productAttr.openValues = if(it.attributeValueList.isNotEmpty()) it.attributeValueList[0] else Constant.EMPTY
                            }
                            AppConstant.AttrTypes.OPEN_VALUES->{
                                productAttr.openValues = it.attributeValueList.joinToString(",")
                            }
                            AppConstant.AttrTypes.UPLOAD_FIELD->{
                                productAttr.selectedValues =  it.attributeValueList.map { Value(any = FileInfo(fileUri = it)) } as ArrayList<Value>
                            }
                        }
                    }
                }
            }
            return mList
        }

    }
}