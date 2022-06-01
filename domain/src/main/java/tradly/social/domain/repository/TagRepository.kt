package tradly.social.domain.repository

import tradly.social.domain.dataSource.TagDataSource

class TagRepository(private val tagDataSource: TagDataSource) {
     fun searchTag(keyword:String) = tagDataSource.searchTags(keyword)
}