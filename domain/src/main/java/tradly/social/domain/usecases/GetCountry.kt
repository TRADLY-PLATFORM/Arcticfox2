package tradly.social.domain.usecases

import tradly.social.domain.repository.CountryRepository

class GetCountry(private val countryRepository: CountryRepository) {
    suspend operator fun invoke() = countryRepository.getCountries()
    suspend fun getCountry(id:String) = countryRepository.getCountry(id)
    suspend fun getUserCountry() = countryRepository.getUserCountry()
}