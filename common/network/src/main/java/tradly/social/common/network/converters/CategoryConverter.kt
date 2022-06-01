package tradly.social.common.network.converters

import tradly.social.domain.entities.Category
import org.json.JSONArray
import tradly.social.common.network.entity.CategoryEntity
import tradly.social.common.network.entity.CategoryHierarchyResponse
import tradly.social.common.network.parseHelper.ParseConstant
import tradly.social.domain.entities.CategoryHierarchy
import java.util.*

object CategoryConverter:BaseConverter() {

    fun mapFrom(categoryEntity: CategoryEntity):Category =
        Category(
            id = categoryEntity.id,
            name = categoryEntity.name,
            imagePath = categoryEntity.imagePath,
            hasSubCategory = categoryEntity.hasSubCategory,
            parent = categoryEntity.parent,
            subCategory = mapFromList(categoryEntity.subCategory),
            hierarchy = mapFrom(categoryEntity.hierarchy)
        )

    fun mapFromStringList(fromList: List<String>) = fromList.map { Category(name = it) }

    fun mapFrom(list: List<CategoryHierarchyResponse>) = list.map { CategoryHierarchy(it.id,getStringOrEmpty(it.name),it.level) }


    fun mapFromList(from: List<CategoryEntity>?): List<Category> {
        val categoryList = mutableListOf<Category>()
        from?.let {
            for (item in from) {
                categoryList.add(mapFrom(item))
            }
        }
        return categoryList
    }

    fun mapFromList(from: String): List<Category> {
        val list = mutableListOf<Category>()
        val jsArray = JSONArray(from)
        for (i in 0 until (jsArray.length())) {
            val item = jsArray.getJSONObject(i)
            list.add(
                Category(
                    id = item.getString("id"),
                    imagePath = item.optString("imagePath"),
                    hasSubCategory = item.optBoolean("hasSubCategory"),
                    categotyTranslation = CategoryTranslatorConverter().getCurrentTranslation(
                        Locale.getDefault().language,
                        item.getJSONArray(ParseConstant.TRANSLATION).toString()
                    )
                )
            )
        }
        return list
    }

}