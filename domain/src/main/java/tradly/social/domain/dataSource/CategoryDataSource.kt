package tradly.social.domain.dataSource

import tradly.social.domain.entities.Category
import tradly.social.domain.entities.Result

interface CategoryDataSource {
    fun addCategory()
    fun getCategory()
    fun getCategories(categoryId:String,pagination:Int=0,type:String):Result<List<Category>>
}