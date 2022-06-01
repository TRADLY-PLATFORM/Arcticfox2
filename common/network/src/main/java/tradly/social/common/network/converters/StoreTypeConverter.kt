package tradly.social.common.network.converters

import tradly.social.domain.entities.StoreType
import com.parse.ParseObject
import tradly.social.common.network.parseHelper.ParseConstant

class StoreTypeConverter {
    fun mapFrom(from:ParseObject?):StoreType {
        val storeType = StoreType()
        from?.let {
            return storeType.also {
                it.name = from.getString(ParseConstant.NAME)
                it.type = from.getInt(ParseConstant.TYPE)
                it.createdAt = from.createdAt.time
                it.updatedAt = from.updatedAt.time
                it.id = from.objectId
            }
        }
        return storeType
    }

    fun mapFrom(from:List<ParseObject>?):List<StoreType>{
        val list = mutableListOf<StoreType>()
        from?.let {
            for(obj in from){
                list.add(mapFrom(obj))
            }
        }
        return list
    }
}