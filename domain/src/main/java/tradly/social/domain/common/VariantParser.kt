package tradly.social.domain.common

import tradly.social.domain.entities.Variant
import tradly.social.domain.entities.VariantProperties

class VariantParser {
    companion object{
        fun parseVariant(variantList: List<Variant>?, callback:(propertyCollection:List<Variant>, variantCollection:List<HashMap<String,Any?>>)->Unit){
            variantList?.let {
                if(it.count()>0){
                    val variants = variantList
                    val propertySize = variants[0].values.count()
                    val propertyCollection = mutableListOf<Variant>()
                    for(i in 0 until propertySize){
                        val mVariant = Variant()
                        val propertyList = mutableListOf<VariantProperties>()
                        for(j in 0 until variantList.count()){
                            if(j==0){
                                mVariant.variantName = variantList[j].values[i].variantType
                                mVariant.variantValue = variantList[j].values[i].variantValue
                            }
                            propertyList.add(variantList[j].values[i])/*.also { if(j==0)it.isSelected = true }*/
                        }
                        propertyCollection.add(mVariant.also { it.values = propertyList.distinct() })
                    }

                    val variantCollection = mutableListOf<HashMap<String,Any?>>()
                    for(k in 0 until variantList.count()){
                        val map = hashMapOf<String,Any?>()
                        map["id"] = variantList[k].id
                        map["parent"] = variantList[k]
                        val collections = mutableListOf<String>()
                        val collectionValues = variantList[k].values
                        for(m in 0 until collectionValues.count()){
                            collections.add(collectionValues[m].variantValue)
                            if(k==0){
                                propertyCollection[m].values.find { it.variantValue == collectionValues[m].variantValue }?.isSelected= true
                            }
                        }
                        map["collections"] = collections
                        variantCollection.add(map)
                    }
                    callback(propertyCollection,variantCollection)
                }
            }
        }
    }
}