package tradly.social.common.cache

import tradly.social.common.common.AppConstant
import tradly.social.common.persistance.shared.PreferenceSecurity
import tradly.social.common.util.parser.extension.toJson
import tradly.social.common.util.parser.extension.toList
import tradly.social.domain.entities.Currency

object CurrencyCache {

    private var currency:Currency?=null

    fun getDefaultCurrency():Currency?{
        if(currency==null){
            val currencies = PreferenceSecurity.getString(AppConstant.PREF_CURRENT_CURRENCIES)
            if(currencies.isNotEmpty()){
                return currencies.toList<Currency>()?.find { c->c.default }?.also { currency = it }
            }
            return null
        }
        return currency
    }

    fun cacheCurrencies(list: List<Currency>){
        PreferenceSecurity.putString(AppConstant.PREF_CURRENT_CURRENCIES,list.toJson<List<Currency>>())
        list.find { c->c.default }?.let {defaultCurrency->
            this.currency = defaultCurrency
        }
    }
}