package tradly.social.domain.dataSource

import tradly.social.domain.entities.Home
import tradly.social.domain.entities.Result

interface HomeDataSource {
    fun getHome(latitude:Double = 0.0,longitude:Double = 0.0):Result<Home>
}