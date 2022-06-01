package tradly.social.common.network.converters

import com.parse.ParseObject
import com.parse.ParseUser
import tradly.social.common.network.parseHelper.ParseConstant

class GroupMemberConverter {
    fun getGroupFromList(from: List<ParseObject>): List<ParseObject?> {
        val list = mutableListOf<ParseObject?>()
        for (obj in from) {
            list.add(obj.getParseObject(ParseConstant.GROUP))
        }
        return list
    }

    fun getMemberFromList(from: List<ParseObject>): List<ParseUser?> {
        val list = mutableListOf<ParseUser?>()
        for (obj in from) {
            list.add(obj.getParseUser(ParseConstant.USER))
        }
        return list
    }
}