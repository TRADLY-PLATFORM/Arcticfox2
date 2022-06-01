package tradly.social.domain.usecases

import tradly.social.domain.repository.CurrencyRepository

class GetCurrency(private val currencyRepository: CurrencyRepository){
    suspend fun getCurrencies() = currencyRepository.getCurrencies()
}