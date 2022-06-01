package tradly.social.domain.dataSource

import tradly.social.domain.entities.Result

interface ChatDataSource {
    fun makeNegotiation(productId:String, negotiateAmount:Int):Result<Int>
    fun updateNegotiation(productId: String,negotiationId:String,status:Int):Result<Boolean>
}