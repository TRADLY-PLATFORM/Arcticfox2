package tradly.social.domain.dataSource

import tradly.social.domain.entities.Result
import tradly.social.domain.entities.Tag

interface TagDataSource {
    fun searchTags(keyword:String):Result<List<Tag>>
}