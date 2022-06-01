package tradly.social.ui.transaction

import kotlinx.coroutines.*
import tradly.social.common.base.AppConstant
import tradly.social.data.model.dataSource.ParseTransactionDataSource
import tradly.social.domain.entities.AppError
import tradly.social.domain.entities.Earning
import tradly.social.domain.entities.Result
import tradly.social.domain.entities.Transaction
import tradly.social.domain.repository.TransactionRepository
import tradly.social.domain.usecases.GetTransactions

class TransactionPresenter(var view:View?){

    interface View{
        fun onTransactionList(list: List<Transaction>,isLoadMore:Boolean)
        fun showBalance(earning: Earning?)
        fun showProgressLoader()
        fun hideProgressLoader()
        fun onError(appError: AppError)
    }

    var job:Job?=null
    var getTransaction: GetTransactions
    init {
        val transactionRepository = TransactionRepository(ParseTransactionDataSource())
        getTransaction = GetTransactions((transactionRepository))
        job = Job()
    }

    fun getTransactions(page:Int,accountId:String,isForPayout:Boolean,isLoadMore:Boolean){
       job =  CoroutineScope(Dispatchers.Main).launch {
           view?.showProgressLoader()
           val call = async(Dispatchers.IO){ getTransaction.invoke(page,
               AppConstant.TransactionTypes.SUPER_TYPE,getTypes(isForPayout), accountId) }
           when(val result = call.await()){
               is Result.Success->{
                   if(page==1 && !isForPayout){
                       view?.showBalance(result.data.first)
                   }
                   view?.onTransactionList(result.data.second,isLoadMore)
               }
               is Result.Error->view?.onError(result.exception)
           }
           view?.hideProgressLoader()
       }
    }

    private fun getTypes(isForPayouts:Boolean):List<String>{
        return if(isForPayouts){
             listOf(AppConstant.TransactionTypes.SELLER_PAYOUTS)
        }
        else{
            listOf(
                AppConstant.TransactionTypes.SALES,
                AppConstant.TransactionTypes.SALES_NO_PAYOUTS,
                AppConstant.TransactionTypes.COMMISSION,
                AppConstant.TransactionTypes.COMMISSION_REVERSAL,
                AppConstant.TransactionTypes.SALES_REVERSAL,
                AppConstant.TransactionTypes.SALES_REVERSAL_NO_PAYOUTS,
                AppConstant.TransactionTypes.SELLER_TRANSFER,
                AppConstant.TransactionTypes.SELLER_TRANSFER_REVERSAL,
                AppConstant.TransactionTypes.SELLER_SUBSCRIPTION,
                AppConstant.TransactionTypes.SELLER_PAYMENT_PROCESSING_FEE,
                AppConstant.TransactionTypes.SELLER_PAYMENT_PROCESSING_FEE_REVERSAL)
        }
    }

    fun onDestroy(){
        view = null
        job?.cancel()
    }
}