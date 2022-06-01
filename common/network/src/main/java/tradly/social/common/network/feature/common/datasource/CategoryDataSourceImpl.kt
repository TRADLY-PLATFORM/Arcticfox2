package tradly.social.common.network.feature.common.datasource

import tradly.social.common.network.converters.CategoryConverter
import tradly.social.common.network.CustomError
import tradly.social.common.network.base.BaseService
import tradly.social.common.network.retrofit.CategoryAPI
import tradly.social.domain.dataSource.CategoryDataSource
import tradly.social.domain.entities.AppError
import tradly.social.domain.entities.Category
import tradly.social.domain.entities.Result

class CategoryDataSourceImpl : CategoryDataSource,BaseService() {

    val apiService = getRetrofitService(CategoryAPI::class.java)

    override fun addCategory() {
    }

    override fun getCategories(categoryId: String, pagination: Int,type:String): Result<List<Category>> {

        return when (val result = apiCall(apiService.getCategories(if(categoryId.isEmpty()) "0" else categoryId,type))) {
            is Result.Success -> {
                if(result.data.status){
                    Result.Success(data = CategoryConverter.mapFromList(result.data.categoryData.categoryList))
                }
                else{
                    Result.Error(exception = AppError(code = CustomError.GET_CATEGORY_LIST_FAILED))
                }
            }
            is Result.Error -> result

        }
    }

    override fun getCategory() {
    }
}