package tradly.social.domain.repository

import tradly.social.domain.dataSource.TransactionDataSource

class TransactionRepository(private val transactionDataSource: TransactionDataSource){
    suspend fun getTransactions(page:Int,accountId:String,superType:String,types:List<String>) = transactionDataSource.getTransactions(page, accountId, superType, types)
    suspend fun getEarnings(accountId:String) = transactionDataSource.getEarnings(accountId)
}