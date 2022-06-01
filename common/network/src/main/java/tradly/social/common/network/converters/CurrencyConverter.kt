package tradly.social.common.network.converters

import tradly.social.common.network.entity.CurrencyEntity
import tradly.social.domain.entities.Currency

object CurrencyConverter{
    fun mapFrom(from: CurrencyEntity) =
        Currency().apply {
            id = from.id
            name = from.name
            code = from.code
            precision = from.precision
            format = from.format
            default = from.default
        }

    fun mapFromList(list:List<CurrencyEntity>): List<Currency> {
        val mList = mutableListOf<Currency>()
        list.forEach { it->
            mList.add(mapFrom(it))
        }
        return mList.also { it.sortByDescending { currency-> currency.default } }
    }
}