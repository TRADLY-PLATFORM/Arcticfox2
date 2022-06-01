package tradly.social.data.model.dataSource

import tradly.social.common.cache.CurrencyCache
import tradly.social.common.network.converters.CurrencyConverter
import tradly.social.data.model.network.retrofit.RetrofitManager
import tradly.social.domain.dataSource.CurrencyDataSource
import tradly.social.domain.entities.Currency
import tradly.social.domain.entities.Result

class ParseCurrencyDataSource:CurrencyDataSource{
    override fun getCurrencies(): Result<List<Currency>> =
        when(val result = RetrofitManager.getInstance().getCurrencies()){
            is Result.Success-> {
                val list = CurrencyConverter.mapFromList(result.data.currencyData.currencyList)
                CurrencyCache.cacheCurrencies(list)
                Result.Success(list)
            }
            is Result.Error->result
        }
}