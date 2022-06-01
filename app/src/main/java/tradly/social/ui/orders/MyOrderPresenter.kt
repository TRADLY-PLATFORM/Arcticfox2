package tradly.social.ui.orders

import kotlinx.coroutines.*
import tradly.social.common.network.feature.common.datasource.OrderDataSourceImpl
import tradly.social.domain.entities.AppError
import tradly.social.domain.entities.Order
import tradly.social.domain.entities.Result
import tradly.social.domain.repository.OrderRepository
import tradly.social.domain.usecases.GetOrderUc
import kotlin.coroutines.CoroutineContext
typealias OrderStatus = tradly.social.common.base.AppConstant.OrderStatus

class MyOrderPresenter(var view: View?) : CoroutineScope {

    interface View {
        fun getOrders(orderItems: List<Order>, isLoadMore:Boolean = false)
        fun showProgressLoader()
        fun hideProgressLoader()
        fun showProgressDialogLoader()
        fun hideProgressDialogLoader()
        fun onFailure(appError: AppError)
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private var getOrderUc: GetOrderUc
    private var job: Job

    init {
        val orderDataSource = OrderDataSourceImpl()
        val orderRepository = OrderRepository(orderDataSource)
        getOrderUc = GetOrderUc(orderRepository)
        job = Job()
    }


    fun getOrders(pageNo: Int,accountId:String, startDate:String,endDate:String, isLoadMore:Boolean = false,orderStatus: Int=0) {
        launch {
            view?.showProgressLoader()
            val statusList = if(orderStatus== OrderStatus.ORDER_STATUS_CANCELED_BY_CUSTOMER|| orderStatus== OrderStatus.ORDER_STATUS_CANCELED_BY_ACCOUNT){
                listOf(OrderStatus.ORDER_STATUS_CANCELED_BY_CUSTOMER,OrderStatus.ORDER_STATUS_CANCELED_BY_ACCOUNT)
            }
            else{
                listOf(orderStatus)
            }
            val call = async(Dispatchers.IO) { getOrderUc.getOrders(pageNo.toString(),accountId,startDate,endDate,statusList) }
            when (val result = call.await()) {
                is Result.Success -> view?.getOrders(result.data,isLoadMore)
                is Result.Error -> view?.onFailure(result.exception)
            }
            view?.hideProgressLoader()
        }
    }

    fun onDestroy(){
        job.cancel()
        view = null
    }
}