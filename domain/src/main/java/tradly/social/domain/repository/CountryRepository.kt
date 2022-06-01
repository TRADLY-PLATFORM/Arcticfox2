package tradly.social.domain.repository

import tradly.social.domain.dataSource.CountryDataSource

class CountryRepository(private val countryDataSource: CountryDataSource) {
    suspend fun getCountries()= countryDataSource.getCountries()
    suspend fun getCountry(id:String) = countryDataSource.getCountry(id)
    suspend fun getUserCountry() = countryDataSource.getUserCountry()
}