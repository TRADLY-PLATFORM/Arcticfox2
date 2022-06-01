package tradly.social.common.network.converters

import tradly.social.domain.entities.Group
import com.parse.ParseObject
import com.parse.ktx.putOrIgnore
import tradly.social.common.network.parseHelper.ParseClasses
import tradly.social.common.network.parseHelper.ParseConstant

class GroupConverter {
    fun mapFrom(from: ParseObject?): Group {
        from?.let {
            return Group(
                from.getString(ParseConstant.NAME),
                UserModelConverter().mapFrom(from.getParseUser(ParseConstant.CREATED_BY)),
                from.getString(ParseConstant.IMAGE_PATH),
                GeoPointModelConverter.mapFrom(from.getParseGeoPoint(ParseConstant.LOCATION)),
                from.getString(ParseConstant.DESCRIPTION),
                from.getInt(ParseConstant.MEMBERS_COUNT),
                null,
                from.getBoolean(ParseConstant.ARCHEIVED),
                from.getBoolean(ParseConstant.BLOCKED),
                from.objectId,
                from.createdAt.time,
                from.updatedAt.time,
                from.getString(ParseConstant.GROUP_ADDRESS),
                groupType = from.getInt(ParseConstant.GROUP_TYPE)
            )
        }
        return Group()
    }

    fun mapFromList(fromList: List<ParseObject?>): List<Group> {
        val list = mutableListOf<Group>()
        fromList?.let {
            for (obj in fromList) {
                list.add(mapFrom(obj))
            }
        }
        return list
    }

    fun mapFrom(from: Group): ParseObject =
        ParseObject(ParseClasses.GROUPS).apply {
            putOrIgnore(ParseConstant.NAME,from.groupName)
            putOrIgnore(ParseConstant.MEMBERS_COUNT,from.membersCount)
            putOrIgnore(ParseConstant.GROUP_TYPE,from.groupType)
            putOrIgnore(ParseConstant.IMAGE_PATH,from.groupPic)
            putOrIgnore(ParseConstant.LOCATION,GeoPointModelConverter.mapFrom(from.location))
            putOrIgnore(ParseConstant.DESCRIPTION,from.groupDescription)
            putOrIgnore(ParseConstant.GROUP_ADDRESS,from.groupAddress)
        }
}