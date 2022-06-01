package tradly.social.common.network.converters

import tradly.social.common.network.entity.EarningData
import tradly.social.common.network.entity.TransactionEntity
import tradly.social.domain.entities.Earning
import tradly.social.domain.entities.Transaction

class TransactionConverter {
    companion object{
        fun mapFrom(earningData: EarningData)= Earning(
            totalSales = ProductConverter.mapFrom(earningData.totalSales),
            totalPayouts = ProductConverter.mapFrom(earningData.totalPayouts),
            pendingBalance = ProductConverter.mapFrom(earningData.pendingBalance)
        )

        fun mapFrom(list: List<TransactionEntity>):List<Transaction>{
            val transactionList = mutableListOf<Transaction>()
            list.forEach {
                val transaction = Transaction(
                    it.transactionNumber,
                    it.referenceType,
                    it.referenceId,
                    ProductConverter.mapFrom(it.price),
                    it.superType,
                    it.type,
                    it.order.id,
                    it.order.referenceNumber,
                    it.createdAt*1000L
                )
                transactionList.add(transaction)
            }
            return transactionList
        }
    }
}