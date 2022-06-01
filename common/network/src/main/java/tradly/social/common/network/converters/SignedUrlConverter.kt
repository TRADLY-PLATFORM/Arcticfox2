package tradly.social.common.network.converters

import tradly.social.common.network.entity.SignedURLEntity
import tradly.social.domain.entities.FileInfo

object SignedUrlConverter {
    fun mapFrom(from: SignedURLEntity):FileInfo =
        FileInfo().apply {

        }
}