package tradly.social.data.model.dataSource

import tradly.social.common.network.converters.CountryModelConverter
import tradly.social.data.model.network.retrofit.RetrofitManager
import tradly.social.common.network.parseHelper.ParseConstant
import tradly.social.common.base.ErrorHandler
import tradly.social.common.network.parseHelper.ParseManager
import tradly.social.domain.dataSource.CountryDataSource
import tradly.social.domain.entities.AppError
import tradly.social.domain.entities.Country
import tradly.social.domain.entities.Result
import com.parse.ParseUser
import tradly.social.common.network.CustomError

class ParseCountryDataSource : CountryDataSource {
    override fun getCountries(): Result<List<Country>> {
        return when (val result = RetrofitManager.getInstance().getCountries()) {
            is Result.Success -> {
                if(result.data.status){
                    Result.Success(CountryModelConverter().mapFromList(result.data.data.countries))
                }
                else{
                    Result.Error(ErrorHandler.getErrorInfo(result.data.error))
                }
            }
            is Result.Error -> result
        }
    }

    override fun getCountry(id: String): Result<Country> {
        return when (val result = ParseManager.getInstance().getCountry(id)) {
            is Result.Success -> Result.Success(data = CountryModelConverter().mapFrom(result.data))
            is Result.Error -> result
        }
    }

    override fun getUserCountry(): Result<Country> {
        val user = ParseUser.getCurrentUser()
        val parseCountryObject = user.getParseObject(ParseConstant.COUNTRY)
        parseCountryObject?.let {
            if (it.isDataAvailable) {
               return Result.Success(data = CountryModelConverter().mapFrom(it))
            } else {
               return when (val result = ParseManager.getInstance().getCountry(parseCountryObject.objectId)) {
                    is Result.Success -> {
                        Result.Success(data = CountryModelConverter().mapFrom(result.data))
                    }
                    is Result.Error -> {
                        result
                    }
                }
            }
        }
        return Result.Error(exception = AppError(code = CustomError.COUNTRY_NOT_FOUND))
    }


}