package tradly.social.domain.usecases

import tradly.social.domain.repository.TagRepository

class SearchTags(private val tagRepository: TagRepository){
     fun searchTag(keyword:String) = tagRepository.searchTag(keyword)
}