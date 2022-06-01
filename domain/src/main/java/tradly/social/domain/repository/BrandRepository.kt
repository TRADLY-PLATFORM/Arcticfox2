package tradly.social.domain.repository

import tradly.social.domain.dataSource.BrandDataSource

class BrandRepository(private val brandDataSource: BrandDataSource) {
    suspend fun getBrands(limit:Int=0,pagination:Int=0) = brandDataSource.getBrands(limit, pagination)
}