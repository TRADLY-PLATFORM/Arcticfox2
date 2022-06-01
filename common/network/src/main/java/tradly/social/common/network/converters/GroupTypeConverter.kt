package tradly.social.common.network.converters

import tradly.social.common.network.entity.GroupTypeEntity
import tradly.social.domain.entities.GroupType

class GroupTypeConverter {
    fun mapFrom(from: GroupTypeEntity): GroupType {
        from?.let {
            return GroupType(
                id = from.id.toString(),
                name = from.name,
                groupTypeImage = from.imagePath
            )
        }
        return GroupType()
    }

    fun mapFromList(list:List<GroupTypeEntity>):List<GroupType>{
        val mlist = mutableListOf<GroupType>()
        for(item in list){
            mlist.add(mapFrom(item))
        }
        return mlist
    }
}