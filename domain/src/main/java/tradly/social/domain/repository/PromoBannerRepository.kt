package tradly.social.domain.repository

import tradly.social.domain.dataSource.PromoBannerDataSource

class PromoBannerRepository(private val promoBannerDataSource: PromoBannerDataSource) {
    suspend fun getBanners(limit:Int=0) = promoBannerDataSource.getPromoBanners(limit)
}