package tradly.social.data.model.dataSource

import tradly.social.common.network.converters.HomeConverter
import tradly.social.data.model.network.retrofit.RetrofitManager
import tradly.social.domain.dataSource.HomeDataSource
import tradly.social.domain.entities.Home
import tradly.social.domain.entities.Result

class ParseHomeDataSource():HomeDataSource {
    override fun getHome(latitude:Double,longitude:Double): Result<Home> =
        when (val result = RetrofitManager.getInstance().getHome(getQueryMap(latitude, longitude))){
            is Result.Success->{
                val home = HomeConverter.mapFrom(result.data)
                Result.Success(data = home)
            }
            is Result.Error->result
        }

    private fun getQueryMap(latitude:Double,longitude:Double):HashMap<String,Any?>{
        val map = hashMapOf<String,Any?>()
        if(latitude!=0.0 || longitude!=0.0){
            map["latitude"] = latitude
            map["longitude"] = longitude
        }
        return map
    }
}