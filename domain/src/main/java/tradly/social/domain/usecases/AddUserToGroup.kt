package tradly.social.domain.usecases

import tradly.social.domain.repository.GroupRepository

class AddUserToGroup(private val groupRepository: GroupRepository) {
    suspend operator fun invoke(groupId:String) = groupRepository.addUserToGroup(groupId)
    suspend fun removeUserFromGroup(groupId: String?) = groupRepository.removeUserFromGroup(groupId)
}