package tradly.social.domain.usecases

import tradly.social.domain.entities.Constant
import tradly.social.domain.repository.ProductRepository

class GetProduct(val productRepository: ProductRepository) {
    suspend operator fun invoke(productId:String?,locale:String)=  productRepository.getProduct(productId, locale)
    suspend fun getAttributes(categoryId:String = Constant.EMPTY,locale:String) = productRepository.getAttributes(categoryId,locale)
}