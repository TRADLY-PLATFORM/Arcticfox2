package tradly.social.domain.repository

import tradly.social.domain.dataSource.GroupDataSource
import tradly.social.domain.entities.Constant
import tradly.social.domain.entities.Group

class GroupRepository(private var groupDataSource: GroupDataSource) {
    suspend fun addGroup(group: Group) = groupDataSource.addGroup(group)
    suspend fun getGroups(scope: String, pagination: Int = 0,key:String) = groupDataSource.getGroups(scope, pagination, key)
    suspend fun addUserToGroup(groupId:String?) = groupDataSource.addUserToGroup(groupId)
    suspend fun getMyGroups(limit:Int=0,pagination:Int=0) = groupDataSource.getMyGroups(limit, pagination)
    suspend fun getGroup(groupId: String? = Constant.EMPTY, sort: String? = Constant.EMPTY, pagination: Int = 0, isMyProduct: Boolean = false) = groupDataSource.getGroup(groupId, sort, pagination, isMyProduct)
    suspend fun removeUserFromGroup(groupId: String?) = groupDataSource.removeUserFromGroup(groupId)
    suspend fun getGroupRelatedProduct(groupId: String? ,pagination: Int) = groupDataSource.getGroupRelatedProduct(groupId, pagination)
    suspend fun getGroupMembers(groupId: String?) = groupDataSource.getGroupMembers(groupId)
    suspend fun getGroupTypes() = groupDataSource?.getGroupType()
}