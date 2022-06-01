package tradly.social.data.model.dataSource

import tradly.social.common.network.NetworkError
import tradly.social.data.model.network.retrofit.RetrofitManager
import tradly.social.domain.dataSource.ChatDataSource
import tradly.social.domain.entities.AppError
import tradly.social.domain.entities.Result

class ParseChatDataSource:ChatDataSource {
    override fun makeNegotiation(productId: String , negotiateAmount:Int): Result<Int> =
        when(val result = RetrofitManager.getInstance().makeNegotiation(productId,getRequestBody(negotiateAmount))){
            is Result.Success->{
                if (result.data.status){
                    Result.Success(result.data.data.negotiation.id)
                }
                else{
                    Result.Error(AppError(errorType = NetworkError.API_UNKNOWN_ERROR))
                }
            }
            is Result.Error->result
        }

    override fun updateNegotiation(
        productId: String,
        negotiationId: String,
        status: Int
    ): Result<Boolean> =
        when(val result = RetrofitManager.getInstance().updateNegotiation(productId,negotiationId,getRequestBody(status = status))){
            is Result.Success->{
                if (result.data.status){
                    Result.Success(true)
                }
                else{
                    Result.Error(AppError(errorType = NetworkError.API_UNKNOWN_ERROR))
                }
            }
            is Result.Error->result
        }

    private fun getRequestBody(price:Int = 0,status:Int = 0):HashMap<String,Any?>{
        val map = HashMap<String,Any?>()
        val body = HashMap<String,Any?>()
        if (price!=0){ body["price"] = price }
        if (status!=0){ body["status"] = status }
        map["negotiation"] = body
        return map
    }
}