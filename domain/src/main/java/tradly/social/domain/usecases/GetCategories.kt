package tradly.social.domain.usecases

import tradly.social.domain.entities.Constant
import tradly.social.domain.repository.CategoryRepository

class GetCategories(private val categoryRepository: CategoryRepository) {
    suspend fun getCategories(categoryId:String = Constant.EMPTY , type:String = Constant.EMPTY) = categoryRepository.getCategories(categoryId,type)
}