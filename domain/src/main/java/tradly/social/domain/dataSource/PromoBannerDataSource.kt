package tradly.social.domain.dataSource

import tradly.social.domain.entities.PromoBanner
import tradly.social.domain.entities.Result

interface PromoBannerDataSource {
    fun getPromoBanners(limit:Int = 0):Result<List<PromoBanner>>
    fun getPromoBanner()
}