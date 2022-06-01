package tradly.social.domain.dataSource

import tradly.social.domain.entities.Earning
import tradly.social.domain.entities.Result
import tradly.social.domain.entities.Transaction

interface TransactionDataSource {

    fun getTransactions(page:Int,accountId:String,superType:String,types:List<String>):Result<Pair<Earning?,List<Transaction>>>

    fun getEarnings(accountId:String):Result<Earning>
}