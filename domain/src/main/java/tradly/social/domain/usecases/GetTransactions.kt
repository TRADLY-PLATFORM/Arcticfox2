package tradly.social.domain.usecases

import tradly.social.domain.dataSource.TransactionDataSource
import tradly.social.domain.repository.TransactionRepository

class GetTransactions(private val transactionRepository: TransactionRepository){
    suspend operator fun invoke(page:Int,superType:String,types:List<String>,accountId:String) = transactionRepository.getTransactions(page, accountId, superType, types)
}