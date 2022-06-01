package tradly.social.data.model.dataSource

import tradly.social.common.network.converters.PromoBannerConverter
import tradly.social.common.network.parseHelper.ParseManager
import tradly.social.domain.dataSource.PromoBannerDataSource
import tradly.social.domain.entities.PromoBanner
import tradly.social.domain.entities.Result

class ParsePromoBannerDataSource:PromoBannerDataSource {
    override fun getPromoBanner() {
    }

    override fun getPromoBanners(limit: Int): Result<List<PromoBanner>> =
        when(val result = ParseManager.getInstance().getPromoBanner(limit)){
            is Result.Success -> Result.Success(data = PromoBannerConverter.mapFromList(null))
            is Result.Error->result
        }
}