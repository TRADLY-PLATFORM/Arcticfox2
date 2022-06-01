package tradly.social.common.network.converters

import tradly.social.common.network.entity.PromoEntity
import tradly.social.common.util.parser.extension.getValue
import tradly.social.domain.entities.PromoBanner

object PromoBannerConverter {

    fun mapFromList(list:List<PromoEntity>?):List<PromoBanner>{
        val promoList = mutableListOf<PromoBanner>()
        list?.let {
            for(item in it){
                promoList.add(mapFrom(item))
            }
        }
        return promoList
    }

    fun mapFrom(promoEntity: PromoEntity): PromoBanner =
        PromoBanner(
            reference = promoEntity.value.getValue(),
            type = promoEntity.type,
            imagePath = promoEntity.imagePath
        )
}