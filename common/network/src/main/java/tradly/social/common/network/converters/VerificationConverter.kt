package tradly.social.common.network.converters

import tradly.social.common.network.entity.VerificationEntity
import tradly.social.domain.entities.Verification

object VerificationConverter {
    fun mapFrom(verificationEntity: VerificationEntity): Verification = Verification(verificationEntity.verifyId.toString())
}