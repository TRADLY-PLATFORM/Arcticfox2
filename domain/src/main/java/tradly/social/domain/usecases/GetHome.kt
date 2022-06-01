package tradly.social.domain.usecases

import tradly.social.domain.repository.HomeRepository

class GetHome(val homeRepository: HomeRepository) {
    suspend operator fun invoke(latitude:Double = 0.0,longitude:Double = 0.0)= homeRepository.getHome(latitude, longitude)
}