package tradly.social.ui.payment

import android.content.Context
import android.content.Intent
import android.net.Uri
import tradly.social.domain.repository.CartRepository
import kotlinx.coroutines.*
import tradly.social.BuildConfig
import tradly.social.R
import tradly.social.common.base.AppConfigHelper
import tradly.social.common.base.Utils
import tradly.social.common.base.AppController
import tradly.social.common.network.APIEndPoints
import tradly.social.data.model.AppDataSyncHelper
import tradly.social.data.model.dataSource.*
import tradly.social.common.base.ErrorHandler
import tradly.social.common.network.CustomError
import tradly.social.common.network.feature.common.datasource.OrderDataSourceImpl
import tradly.social.common.network.feature.common.datasource.PaymentDataSourceImpl
import tradly.social.data.model.payments.stripe.StripeApiVersionPrefetch
import tradly.social.data.model.payments.stripe.StripeHelper
import tradly.social.domain.entities.*
import tradly.social.domain.repository.OrderRepository
import tradly.social.domain.repository.PaymentRepository
import tradly.social.domain.usecases.*
import tradly.social.ui.base.BaseView
import tradly.social.ui.base.StripeView
import kotlin.coroutines.CoroutineContext

class PaymentPresenter(var view:View?): CoroutineScope,StripeHelper.SecretKeyListener {
    interface View:StripeView{
        fun showPriceDetails(cartResult: Cart)
        fun showPaymentTypes(list: List<Payment>)
        fun showProgressLoader()
        fun hideProgressLoader()
        fun onSuccessOrderCancel()
        fun onFailure(appError: AppError)
        fun showError(messageRes:Int)
    }

    private var job: Job
    private var getCart: GetCart? = null
    private var getPaymentTypes:GetPaymentTypesUc
    private var checkOutCartUc:CheckOutCartUc
    private var getOrderUc:GetOrderUc
    private var getPaymentIntentSecret:GetPaymentIntentSecret
    private var getEphemeralKey:GetEphemeralKey
    private lateinit var stripeHelper:StripeHelper

    init {
        val parseCartDataSource = ParseCartDataSource()
        val parseOrderDataSource = OrderDataSourceImpl()
        val parsePaymentDataSource = PaymentDataSourceImpl()
        val paymentRepository = PaymentRepository(parsePaymentDataSource)
        val orderRepository = OrderRepository(parseOrderDataSource)
        val cartRepository = CartRepository(parseCartDataSource)
        getCart = GetCart(cartRepository)
        getPaymentTypes = GetPaymentTypesUc(paymentRepository)
        checkOutCartUc = CheckOutCartUc(orderRepository)
        getPaymentIntentSecret = GetPaymentIntentSecret(paymentRepository)
        getEphemeralKey = GetEphemeralKey(paymentRepository)
        getOrderUc = GetOrderUc(orderRepository)
        job = Job()

    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    fun getPriceDetails(shipmentMethodId:String){
        launch{
            view?.showProgressLoader()
            val call = async(Dispatchers.IO){ getCart?.getCartItems(shipmentMethodId) }
            when(val result = call.await()){
                is Result.Success->{
                    val paymentTypeCall = async(Dispatchers.IO){ getPaymentTypes.getPaymentTypes() }
                    when(val resultPaymentType = paymentTypeCall.await()){
                        is Result.Success-> {
                            view?.showPriceDetails(result.data)
                            view?.showPaymentTypes(resultPaymentType.data)
                        }
                        is Result.Error-> view?.onFailure(resultPaymentType.exception)
                    }
                }
                is Result.Error->view?.onFailure(result.exception)
            }
            view?.hideProgressLoader()
        }
    }

    fun checkOutCart(grandPrice: Price,shippingAddressId:Int,payment:Payment,shippingMethodId:Int){
        launch {
            if(isValidCheckOut(grandPrice,payment.minAmount)){
                view?.showProgressDialog(R.string.please_wait)
                val call = async(Dispatchers.IO){ checkOutCartUc.checkOutOrder(shippingAddressId, payment.id,shippingMethodId) }
                when(val result = call.await()){
                    is Result.Success-> view?.onCheckOutStatus(result.data,payment.id,payment.type,payment.channel)
                    is Result.Error->view?.onFailure(result.exception)
                }
                view?.hideProgressDialog()
            }
        }
    }

    fun makeWebPayment(orderReference: String,paymentMethodId:String){
        val authKey = AppController.appController.getUser()!!.authKeys.authKey
        val uri = Uri.parse(BuildConfig.BASE_URL+ APIEndPoints.WEB_PAYMENT).buildUpon().apply {
            this.appendQueryParameter("order_reference",orderReference)
            this.appendQueryParameter("auth_key",authKey)
            this.appendQueryParameter("payment_method_id",paymentMethodId)
        }.build()
        Utils.openUrlInBrowser(uri.toString())
    }

    fun initStripeSDK(ctx:Context,type:String,orderReference: String){
       AppConfigHelper.getConfigKey<String>(AppConfigHelper.Keys.STRIPE_API_PUB_KEY).apply {
           if(this.isNotEmpty()){
               stripeHelper = StripeHelper(ctx,view)
               stripeHelper.initStripe(this)
               initStripeCustomerSession(orderReference)
           }
           else{
               ErrorHandler.handleError(exception = AppError(type, CustomError.INIT_PAYMENT_FAILED))
               AppDataSyncHelper.syncAppConfig()
           }
       }
    }

    private fun initStripeCustomerSession(orderReference:String){
        StripeApiVersionPrefetch{ apiVersion->
            launch(Dispatchers.Main){
                view?.showProgressDialog(R.string.please_wait)
                val call = async(Dispatchers.IO){ getEphemeralKey.getEphemeralKey(apiVersion) }
                when(val result = call.await()){
                    is Result.Success->{
                        view?.hideProgressDialog()
                        stripeHelper.initCustomerSession(result.data)
                        stripeHelper.initPaymentSession(orderReference,this@PaymentPresenter)
                    }
                    is Result.Error->{
                        view?.hideProgressDialog()
                        ErrorHandler.handleError(exception = result.exception)
                    }
                }
            }
        }
    }

    override fun getSecretKey(paymentId: String, orderReference: String) {
        launch(Dispatchers.Main){
            view?.showProgressDialog(R.string.please_wait)
            val call = async(Dispatchers.IO){ getPaymentIntentSecret.getPaymentIntentSecret(orderReference)  }
            when(val result = call.await()){
                is Result.Success->stripeHelper.confirmPayment(paymentId,result.data)
                is Result.Error->view?.onFailure(result.exception)
            }
            view?.hideProgressDialog()
        }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent){
        if(::stripeHelper.isInitialized){
            if(requestCode==50000){
                view?.showProgressDialog(R.string.please_wait)
            }
            stripeHelper.onActivityResult(requestCode,resultCode,data)
        }
    }

    fun isValidCheckOut(grandPrice: Price, paymentMinAmount: Double):Boolean{
        if(grandPrice.amount < paymentMinAmount){
            view?.showError(R.string.cart_alert_message_min_balance_checkout)
            return false
        }
        return true
    }


    fun cancelOrder(orderReference: String,){
        launch(Dispatchers.Main) {
            view?.showProgressDialog(R.string.please_wait)
            val call = async(Dispatchers.IO) { getOrderUc.cancelOrder(orderReference,tradly.social.common.base.AppConstant.OrderStatus.ORDER_STATUS_CANCELED_BY_CUSTOMER) }
            when(val result = call.await()){
                is Result.Success-> {
                    view?.hideProgressDialog()
                    view?.onSuccessOrderCancel()
                }
                is Result.Error->{
                    view?.hideProgressDialog()
                    ErrorHandler.handleError(exception = result.exception)
                }
            }
        }
    }

    fun ondestroy(){
        job.cancel()
        view = null
    }
}