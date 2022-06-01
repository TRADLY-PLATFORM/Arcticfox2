package tradly.social.data.model.dataSource

import tradly.social.common.network.converters.BrandConverter
import tradly.social.common.network.parseHelper.ParseManager
import tradly.social.domain.dataSource.BrandDataSource
import tradly.social.domain.entities.Brand
import tradly.social.domain.entities.Result

class ParseBrandDataSource : BrandDataSource {
    override fun getBrands(limit: Int, pagination: Int): Result<List<Brand>> =
        when (val result = ParseManager.getInstance().getBrands(limit)) {
            is Result.Success -> Result.Success(data = BrandConverter().mapFromList(result.data))
            is Result.Error -> result
        }
}