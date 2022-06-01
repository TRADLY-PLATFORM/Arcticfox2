package tradly.social.domain.dataSource

import tradly.social.domain.entities.Brand
import tradly.social.domain.entities.Result

interface BrandDataSource {
    fun getBrands(limit:Int=0,pagination:Int=0):Result<List<Brand>>
}