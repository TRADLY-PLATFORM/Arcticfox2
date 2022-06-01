package tradly.social.domain.dataSource

import tradly.social.domain.entities.Country
import tradly.social.domain.entities.Result

interface CountryDataSource {
    fun getCountries(): Result<List<Country>>
    fun getCountry(id:String):Result<Country>
    fun getUserCountry():Result<Country>
}