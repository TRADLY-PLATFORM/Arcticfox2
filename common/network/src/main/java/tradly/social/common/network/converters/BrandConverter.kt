package tradly.social.common.network.converters

import tradly.social.domain.entities.Brand
import com.parse.ParseObject
import tradly.social.common.network.parseHelper.ParseConstant

class BrandConverter {
    fun mapFrom(from:ParseObject?):Brand =
        from?.let {
            Brand(
                from.getString(ParseConstant.NAME),
                from.getBoolean(ParseConstant.ACTIVE),
                from.getString(ParseConstant.IMAGE_PATH),
                from.getString(ParseConstant.STORE_TYPE),
                from.objectId,
                from.createdAt.time,
                from.updatedAt.time
            )
        }?:run{Brand()}

    fun mapFromList(from:List<ParseObject>?):List<Brand>{
        val brandList = mutableListOf<Brand>()
        from?.let {
            for (obj in from){
                brandList.add(mapFrom(obj))
            }
        }
        return brandList
    }
}