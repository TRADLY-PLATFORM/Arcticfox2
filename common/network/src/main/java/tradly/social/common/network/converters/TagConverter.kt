package tradly.social.common.network.converters

import tradly.social.common.network.entity.TagEntity
import tradly.social.domain.entities.Tag

object TagConverter {
    fun mapFrom(from: TagEntity?):Tag{
        from?.let {
            val tag = Tag()
            tag.name = from.name
            return tag
        }
        return Tag()
    }

    fun mapFromList(from:List<TagEntity>):List<Tag>{
        val list = mutableListOf<Tag>()
        for(obj in from){
            list.add(mapFrom(obj))
        }
        return list
    }
}