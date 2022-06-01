package tradly.social.common.network.converters

import tradly.social.domain.entities.CategoryTranslation
import org.json.JSONArray
import tradly.social.common.util.parser.extension.toObject

class CategoryTranslatorConverter {

    private fun getTranslation(
        locale: String,
        convertedList: List<CategoryTranslation>
    ): CategoryTranslation? {
        var categoryTranslation = convertedList.filter { c -> c.locale == locale }.singleOrNull()
        categoryTranslation?.let {
            return it
        } ?: run {
            return convertedList.find { c -> c.text == "en" }
        }
    }

    fun getCurrentTranslation(locale: String, jsonArray: String?): CategoryTranslation? {
        val jsonArray = JSONArray(jsonArray)
        val list = mutableListOf<CategoryTranslation>()
        for (i in 0 until (jsonArray.length())) {
            val categoryTranslation = jsonArray.getJSONObject(i).toString().toObject<CategoryTranslation>()
            categoryTranslation?.let {
                list.add(it)
            }
        }
        return getTranslation(locale, list)
    }
}