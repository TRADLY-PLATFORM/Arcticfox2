package tradly.social.common.network.converters

import tradly.social.common.network.entity.VariantEntity
import tradly.social.common.network.entity.VariantTypeEntity
import tradly.social.common.network.entity.VariantValueEntity
import tradly.social.domain.entities.Variant
import tradly.social.domain.entities.VariantProperties
import tradly.social.domain.entities.VariantType

class VariantConverter {
    companion object{
        fun mapFromVariant(variantList: List<VariantEntity>):List<Variant>{
            val list = mutableListOf<Variant>()
            for ( item in variantList){
                val variant = Variant(
                    id = item.id.toString(),
                    listDisplayPrice = item.listPrice.displayCurrency,
                    offerDisplayPrice = item.offerPrice.displayCurrency,
                    offerPercent = item.offerPercent,
                    listPrice = ProductConverter.mapFrom(item.listPrice),
                    offerPrice = ProductConverter.mapFrom(item.offerPrice),
                    images = item.images,
                    variantName = item.title,
                    variantDescription = item.description,
                    values = mapFromVariantValueEntity(item.valueList),
                    variantValues = mapFromVariantTypeValueEntity(item.variantValues),
                    quantity = item.stock
                )
                list.add(variant)
            }
            return list
        }

        fun mapFromVariantValueEntity(variantValueEntityList: List<VariantValueEntity>):List<VariantProperties>{
            val list = mutableListOf<VariantProperties>()
            for (item in variantValueEntityList){
                val variantProperties = VariantProperties(
                    id = item.id.toString(),
                    variantType = item.variantTypeEntity.name,
                    variantValue = item.value
                )
                list.add(variantProperties)
            }
            return list
        }

        fun mapFromVariantTypeValueEntity(variantValueEntityList: List<VariantValueEntity>):List<VariantProperties>{
            val list = mutableListOf<VariantProperties>()
            for (item in variantValueEntityList){
                val variantProperties = VariantProperties(
                    id = item.id.toString(),
                    variantTypeId = item.variantTypeEntity.id,
                    variantValueId = item.variantTypeValueEntity.id,
                    variantType = item.variantTypeEntity.name,
                    variantValue = item.variantTypeValueEntity.name
                )
                list.add(variantProperties)
            }
            return list
        }

        fun mapFrom(variantTypeEntityList: List<VariantTypeEntity>): List<VariantType> =
            variantTypeEntityList.map {
                VariantType(
                    it.id,
                    it.name,
                    mapFrom(it.variantTypeValues),
                    it.active
                )
            }
    }
}