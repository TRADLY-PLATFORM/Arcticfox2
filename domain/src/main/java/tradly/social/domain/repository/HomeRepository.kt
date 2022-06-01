package tradly.social.domain.repository

import tradly.social.domain.dataSource.HomeDataSource

class HomeRepository(val homeDataSource: HomeDataSource) {
    suspend fun getHome(latitude:Double,longitude:Double)= homeDataSource.getHome(latitude, longitude)
}