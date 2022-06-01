package tradly.social.domain.repository

import tradly.social.domain.dataSource.CurrencyDataSource

class CurrencyRepository(private var currencyDataSource: CurrencyDataSource){
    fun getCurrencies() = currencyDataSource.getCurrencies()
}