package tradly.social.event.eventbooking

import android.content.Context
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import tradly.social.common.base.*
import tradly.social.common.network.CustomError
import tradly.social.common.network.RequestID
import tradly.social.common.network.feature.common.datasource.OrderDataSourceImpl
import tradly.social.common.network.feature.common.datasource.PaymentDataSourceImpl
import tradly.social.common.network.feature.eventbooking.datasource.EventBookingDataSourceImpl
import tradly.social.common.network.feature.exploreEvent.datasource.EventDataSourceImpl
import tradly.social.data.model.AppDataSyncHelper
import tradly.social.data.model.CoroutinesManager
import tradly.social.data.model.payments.stripe.StripeApiVersionPrefetch
import tradly.social.data.model.payments.stripe.StripeHelper
import tradly.social.domain.entities.AppError
import tradly.social.domain.entities.Event
import tradly.social.domain.entities.EventBooking
import tradly.social.domain.entities.Payment
import tradly.social.domain.entities.Result
import tradly.social.domain.repository.EventBookingRepository
import tradly.social.domain.repository.EventRepository
import tradly.social.domain.repository.OrderRepository
import tradly.social.domain.repository.PaymentRepository
import tradly.social.domain.usecases.*
import tradly.social.ui.base.BaseView
import tradly.social.ui.base.StripeView

class EventBookingViewModel:BaseViewModel(),StripeHelper.SecretKeyListener,StripeView {

    private val getEventUc by lazy { GetEventUc(EventRepository(EventDataSourceImpl())) }
    private val getPaymentTypesUC = GetPaymentTypesUc(PaymentRepository(PaymentDataSourceImpl()))
    private val confirmEventBookingUc = ConfirmEventBookingUc(EventBookingRepository(EventBookingDataSourceImpl()))
    private val getBookingEventUC by lazy { GetBookingEventsUc(EventBookingRepository(EventBookingDataSourceImpl())) }
    private val getBookingDetailUC by lazy { GetBookingDetailUc(EventBookingRepository(EventBookingDataSourceImpl())) }
    private val _orderCheckoutLiveData by lazy { MutableLiveData<SingleEvent<String>>() }
    val orderCheckoutLiveData:LiveData<SingleEvent<String>> = _orderCheckoutLiveData
    private val _paymentTypesLiveData by lazy { MutableLiveData<List<Payment>>() }
    val paymentTypesLiveData:LiveData<List<Payment>> = _paymentTypesLiveData
    private val _eventBookingListLiveData by lazy { MutableLiveData<Triple<Boolean,Int,List<EventBooking>>>() }
    val eventBookingListLiveData:LiveData<Triple<Boolean,Int,List<EventBooking>>> = _eventBookingListLiveData
    private val _eventBookingDetail by lazy { MutableLiveData<EventBooking>() }
    val eventBookingDetail:LiveData<EventBooking> = _eventBookingDetail

    private val _onStripeCheckoutLiveData by lazy { MutableLiveData<SingleEvent<Boolean>>() }
    val onStripeCheckoutLiveData:LiveData<SingleEvent<Boolean>> = _onStripeCheckoutLiveData
    private lateinit var stripeHelper: StripeHelper
    private val parsePaymentDataSource = PaymentDataSourceImpl()
    private val paymentRepository = PaymentRepository(parsePaymentDataSource)
    private val getEphemeralKey = GetEphemeralKey(paymentRepository)
    private val getPaymentIntentSecret = GetPaymentIntentSecret(paymentRepository)
    var isLoading:Boolean = false

    fun initStripeSDK(ctx: Context, type:String, orderReference: String){
        AppConfigHelper.getConfigKey<String>(AppConfigHelper.Keys.STRIPE_API_PUB_KEY).apply {
            if(this.isNotEmpty()){
                stripeHelper = StripeHelper(ctx,this@EventBookingViewModel)
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
            getApiResult(viewModelScope,{getEphemeralKey.getEphemeralKey(apiVersion)},{ result->
                stripeHelper.initCustomerSession(result)
                stripeHelper.initPaymentSession(orderReference,this)
            },true,RequestID.GET_EPHEMERAL_KEY)
        }
    }

    override fun getSecretKey(paymentId: String, orderReference: String) {
        getApiResult(viewModelScope,{getPaymentIntentSecret.getPaymentIntentSecret(orderReference)},{
            stripeHelper.confirmPayment(ActivityHelper.getCurrentActivityInstance(),paymentId,it)
        },true,RequestID.GET_INTENT_SECRET)
    }

    fun onStripeActivityResult(requestCode:Int,resultCode:Int,data: Intent){
        if(::stripeHelper.isInitialized){
            if(requestCode==50000){
                showAPIProgress(true,RequestID.STRIPE_PAYMENT_RESULT)
            }
            stripeHelper.onActivityResult(requestCode,resultCode,data)
        }
    }

    override fun onCheckOutStatus(
        orderReference: String,
        paymentTypeId: Int,
        paymentType: String,
        channel: String,
        isSuccess: Boolean
    ) {
        _onStripeCheckoutLiveData.value = SingleEvent(isSuccess)
    }

    override fun showProgressDialog(msg: Int) {
        showAPIProgress(true,RequestID.STRIPE_PAYMENT_RESULT)
    }

    override fun hideProgressDialog() {
        showAPIProgress(false,RequestID.STRIPE_PAYMENT_RESULT)
    }

    fun getPaymentTypes(){
        getApiResult(viewModelScope,{getPaymentTypesUC.getPaymentTypes()},{
            _paymentTypesLiveData.value = it
        },true,RequestID.GET_PAYMENT_TYPES)
    }

    fun checkout(listingId:String,quantity:Int,variantId:Int = 0,type:String,paymentMethodId:Int){
        getApiResult(viewModelScope,{confirmEventBookingUc.confirmBooking(listingId,variantId,paymentMethodId,quantity,type)},{ orderReference ->
            _orderCheckoutLiveData.value = SingleEvent(orderReference)
        },true,RequestID.DIRECT_CHECKOUT)
    }

    fun getBookingEvents(pagination:Int,accountId:String,type:String){
        getApiResult(viewModelScope,{getBookingEventUC.getEventBookingListUc(pagination.toString(),accountId,type)},{ list ->
            _eventBookingListLiveData.value = Triple(pagination!=1,if (list.isNotEmpty()) pagination+1 else pagination,list)
        },true,RequestID.GET_BOOKING_EVENTS)
    }

    fun getBookingDetail(orderId:String,accountId: String?){
        getApiResult(viewModelScope,{getBookingDetailUC.getBookingDetail(orderId, accountId)},{ eventBooking->
            _eventBookingDetail.value =  eventBooking
        },true,RequestID.GET_BOOKING_DETAIL)
    }



    override fun showAPIProgress(show: Boolean, apiId: Int) {
        _uiState.value = SingleEvent(UIState.Loading(show,apiId))
    }

    override fun onFailure(apiError: AppError) {
        showAPIProgress(false,apiError.apiId)
        _uiState.value = SingleEvent(UIState.Failure(apiError))
    }

}