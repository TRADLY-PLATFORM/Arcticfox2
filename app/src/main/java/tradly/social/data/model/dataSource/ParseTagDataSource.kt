package tradly.social.data.model.dataSource

import tradly.social.common.network.converters.TagConverter
import tradly.social.data.model.network.retrofit.RetrofitManager
import tradly.social.domain.dataSource.TagDataSource
import tradly.social.domain.entities.Result
import tradly.social.domain.entities.Tag

class ParseTagDataSource:TagDataSource {
    override fun searchTags(keyword: String): Result<List<Tag>>  =
        when(val result = RetrofitManager.getInstance().searchTag(getQueryMap(keyword))){
            is Result.Success->Result.Success(data = TagConverter.mapFromList(result.data.tagData.tagList))
            is Result.Error->result
        }

    private fun getQueryMap(keyword: String):Map<String,Any?>{
        val hashMap = hashMapOf<String,Any?>()
        hashMap["key"] = keyword
        hashMap["limit"] = 1 // for first release it will always 1.
        return hashMap
    }
}