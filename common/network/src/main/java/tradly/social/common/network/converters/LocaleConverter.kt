package tradly.social.common.network.converters

import tradly.social.common.network.entity.LocaleEntity
import tradly.social.domain.entities.Locale

object  LocaleConverter {
    fun mapFrom(from: LocaleEntity?): Locale =
        from?.let {
            Locale().apply {
                name = it.name
                locale = it.locale
                default = it.default

            }
        } ?: run {
            Locale()
        }

    fun mapFromList(fromList: List<LocaleEntity>?): List<Locale> {
        val list = mutableListOf<Locale>()
        fromList?.let {
            it.forEach {
                list.add(mapFrom(it))
            }
        }
        return list
    }
}