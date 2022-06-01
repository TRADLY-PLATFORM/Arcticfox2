package tradly.social.domain.usecases

import tradly.social.domain.repository.BrandRepository

class GetBrands (private val brandRepository: BrandRepository){
    suspend operator fun invoke(limit:Int=0,pagination:Int=0) = brandRepository.getBrands(limit, pagination)
}