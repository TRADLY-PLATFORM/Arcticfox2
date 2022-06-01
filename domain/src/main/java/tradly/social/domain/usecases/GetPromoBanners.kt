package tradly.social.domain.usecases

import tradly.social.domain.repository.PromoBannerRepository

class GetPromoBanners(private val promoBannerRepository: PromoBannerRepository) {
    suspend operator fun invoke(limit:Int=0) = promoBannerRepository.getBanners(limit)
}