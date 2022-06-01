package tradly.social.data.model.dataSource

import tradly.social.common.base.AppConstant
import tradly.social.common.network.converters.TransactionConverter
import tradly.social.data.model.network.retrofit.RetrofitManager
import tradly.social.common.base.ErrorHandler
import tradly.social.common.network.CustomError
import tradly.social.domain.dataSource.TransactionDataSource
import tradly.social.domain.entities.AppError
import tradly.social.domain.entities.Earning
import tradly.social.domain.entities.Result
import tradly.social.domain.entities.Transaction

class ParseTransactionDataSource:TransactionDataSource{

    override fun getTransactions(
        page: Int,
        accountId: String,
        superType: String,
        types: List<String>
    ):Result<Pair<Earning?,List<Transaction>>> {
       return when(val result = RetrofitManager.getInstance().getTransactions(getQueryParams(page,superType,types,accountId))){
            is Result.Success->{
                val transactions = TransactionConverter.mapFrom(result.data.data.transactions)
                if(page==1){
                    return when(val earningResult = getEarnings(accountId)){
                        is Result.Success->Result.Success(Pair(earningResult.data,transactions))
                        is Result.Error->earningResult
                    }
                }
                Result.Success(Pair(null,transactions))
            }
            is Result.Error->result
        }
    }

    override fun getEarnings(accountId: String): Result<Earning> {
       return when(val result = RetrofitManager.getInstance().getEarnings(accountId)){
            is Result.Success->{
                if(result.data.status){
                    Result.Success(TransactionConverter.mapFrom(result.data.earningData))
                }
                else{
                    Result.Error(AppError(code = CustomError.EARNING_FETCH_FAILED))
                }
            }
            is Result.Error->result
        }
    }

    private fun getQueryParams(page:Int,superType:String,types:List<String>,accountId: String):HashMap<String,Any?>{
        val map = hashMapOf<String,Any?>()
        map["page"] = page
        map["per_page"] = AppConstant.ListPerPage.TRANSACTION_LIST
        map["super_type"] = superType
        map["type"] = types.joinToString(separator = ",")
        map["account_id"] = accountId
        return map
    }
}