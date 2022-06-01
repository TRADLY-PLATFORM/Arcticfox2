package tradly.social.domain.repository

import tradly.social.domain.dataSource.CategoryDataSource
import tradly.social.domain.entities.Constant

class CategoryRepository(private val categoryDataSource: CategoryDataSource) {
    suspend fun getCategories(categoryId:String,type:String) = categoryDataSource.getCategories(categoryId,type = type)
}