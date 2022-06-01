package tradly.social.domain.dataSource

import tradly.social.domain.entities.Country
import tradly.social.domain.entities.Currency
import tradly.social.domain.entities.Result

interface CurrencyDataSource {
    fun getCurrencies():Result<List<Currency>>
}