package tradly.social.domain.usecases

import tradly.social.domain.entities.Group
import tradly.social.domain.repository.GroupRepository

class AddGroup(val groupRepository:GroupRepository) {
    suspend fun addGroup(group:Group) = groupRepository.addGroup(group)
}