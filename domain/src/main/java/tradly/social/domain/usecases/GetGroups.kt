package tradly.social.domain.usecases

import tradly.social.domain.entities.Constant
import tradly.social.domain.repository.GroupRepository

class GetGroups(private val groupRepository: GroupRepository) {
    suspend operator fun invoke(scope: String, pagination: Int = 0,key:String = Constant.EMPTY) = groupRepository.getGroups(scope, pagination, key)
    suspend fun addUserToGroup(groupId:String?) = groupRepository.addUserToGroup(groupId)
    suspend fun getMyGroups(limit:Int=0,pagination:Int=0) = groupRepository.getMyGroups(limit, pagination)
    suspend fun getGroup(groupId: String? = Constant.EMPTY, sort: String? = Constant.EMPTY, pagination: Int = 0, isMyProduct: Boolean = false)= groupRepository.getGroup(groupId, sort, pagination, isMyProduct)
    suspend fun removeUserFromGroup(groupId: String?) = groupRepository.removeUserFromGroup(groupId)
    suspend fun getGroupRelatedProduct(groupId: String? ,pagination: Int) = groupRepository.getGroupRelatedProduct(groupId, pagination)
    suspend fun getGroupMembers(groupId: String?) = groupRepository.getGroupMembers(groupId)
    suspend fun getGroupTypes() = groupRepository.getGroupTypes()
}