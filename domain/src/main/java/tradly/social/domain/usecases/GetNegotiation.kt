package tradly.social.domain.usecases

import tradly.social.domain.repository.ChatRepository

class GetNegotiation(private val chatRepository: ChatRepository) {
    suspend fun makeNegotiation(productId:String,negotiateAmount:Int) = chatRepository.makeNegotiation(productId,negotiateAmount)
    suspend fun updateNegotiationId(productId: String,negotiationId:String,status:Int) = chatRepository.updateNegotiation(productId, negotiationId, status)
}