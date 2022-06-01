package tradly.social.domain.dataSource

import tradly.social.domain.entities.*

interface GroupDataSource {
    fun addGroup(group:Group):Result<Boolean>
    fun getGroups(scope: String, pagination: Int = 0,key:String):Result<List<Group>>
    fun addUserToGroup(groupId:String?):Result<String>
    fun getMyGroups(limit:Int,pagination:Int):Result<List<Group>>
    fun getGroup(groupId: String? = Constant.EMPTY, sort: String? = Constant.EMPTY, pagination: Int = 0, isMyProduct: Boolean = false):Result<BaseResult>
    fun removeUserFromGroup(groupId: String?):Result<Boolean>
    fun getGroupRelatedProduct(groupId: String? ,pagination: Int):Result<List<Product>>
    fun getGroupMembers(groupId: String?):Result<List<User>>
    fun getGroupType():Result<List<GroupType>>
}