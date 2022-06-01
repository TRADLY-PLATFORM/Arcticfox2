package tradly.social.domain.repository

import tradly.social.domain.dataSource.ChatDataSource
import tradly.social.domain.entities.Result

class ChatRepository(val chatDataSource: ChatDataSource) {
    fun makeNegotiation(productInt: String , negotiateAmount:Int):Result<Int> = chatDataSource.makeNegotiation(productInt, negotiateAmount)
    fun updateNegotiation(productId: String,negotiationId:String,status:Int) = chatDataSource.updateNegotiation(productId, negotiationId, status)
}