package tradly.social.event.eventbooking.confirmbooking.ui

import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import tradly.social.common.adapter.PaymentListAdapter
import tradly.social.common.base.*
import tradly.social.common.base.getOrDefault
import tradly.social.common.network.APIEndPoints
import tradly.social.common.network.RequestID
import tradly.social.common.resources.CommonResourceAttr
import tradly.social.common.resources.CommonResourceColor
import tradly.social.common.resources.CommonResourceString
import tradly.social.common.base.setVisible
import tradly.social.domain.entities.Event
import tradly.social.domain.entities.Payment
import tradly.social.event.eventbooking.BR
import tradly.social.event.eventbooking.EventBookingViewModel
import tradly.social.event.eventbooking.SharedEventBookingViewModel
import tradly.social.event.eventbooking.R
import tradly.social.event.eventbooking.databinding.EventConfirmBookingBinding
import tradly.social.event.explore.common.CommonBaseResourceId
import tradly.social.common.base.BaseFragment
import tradly.social.ui.main.MainActivity


class EventBookingConfirmFragment : BaseFragment(),CustomOnClickListener.OnCustomClickListener,GenericAdapter.OnClickItemListener<Payment> {

    private lateinit var eventConfirmBookingBinding: EventConfirmBookingBinding
    private lateinit var sharedEventBookingViewModel: SharedEventBookingViewModel
    private lateinit var eventBookingViewModel: EventBookingViewModel
    private lateinit var paymentListAdapter: PaymentListAdapter
    private val colorPrimary by lazy { ThemeUtil.getResourceValue(requireContext(),CommonResourceAttr.colorPrimary) }
    private var chosenQuantity:Int = 1
    private var orderReference: String = AppConstant.EMPTY
    private var selectedPaymentId:String = AppConstant.EMPTY
    private var price:Double = 0.0

    companion object{
        const val TAG = "EventBookingConfirmFragment"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedEventBookingViewModel = requireActivity().getViewModel { SharedEventBookingViewModel() }
        eventBookingViewModel = getViewModel { EventBookingViewModel() }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        eventConfirmBookingBinding = DataBindingUtil.inflate(layoutInflater,R.layout.fragment_event_booking_confirm,container,false)
        eventConfirmBookingBinding.lifecycleOwner = viewLifecycleOwner
        return eventConfirmBookingBinding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeLiveData()
        initAdapter()
        eventBookingViewModel.getPaymentTypes()
    }

    private fun initAdapter(){
        paymentListAdapter = PaymentListAdapter()
        paymentListAdapter.onClickItemListener = this
        eventConfirmBookingBinding.apply {
            rvPaymentList.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)
            rvPaymentList.adapter = paymentListAdapter
        }
    }

    private fun observeLiveData(){
        observeEvent(eventBookingViewModel.uiState,this::handleUIState)
        sharedEventBookingViewModel.eventLiveData.observe(viewLifecycleOwner,{
            it?.let {
                onPopulateEvent(it)
            }
        })
        eventBookingViewModel.paymentTypesLiveData.observe(viewLifecycleOwner,this::onPopulatePaymentTypes)
        observeEvent(eventBookingViewModel.orderCheckoutLiveData,this::onCheckoutSuccess)
        observeEvent(sharedEventBookingViewModel.stripeActivityResult,this::onStripeActivityResult)
        observeEvent(eventBookingViewModel.onStripeCheckoutLiveData,{onStripeCheckoutSuccess()})
        observeEvent(sharedEventBookingViewModel.webPaymentResultLiveData,{
            this.orderReference = it.first
            this.selectedPaymentId = it.second
            showSuccessDialog(this.orderReference,this.selectedPaymentId,it.third)
        })
    }

    private fun onPopulateEvent(event:Event){
        eventConfirmBookingBinding.apply {
            val variantProperties = event.variants?.find { it.isSelected }
            if (variantProperties!=null){
                setVariable(BR.title,variantProperties.variantName)
                setVariable(BR.price,variantProperties.offerPrice.displayCurrency)
                setVariable(BR.quantity,chosenQuantity.toString())
                if (variantProperties.quantity == chosenQuantity){
                    enableIncreaseBtn(false)
                    enableDecreaseBtn(false)
                }
                this@EventBookingConfirmFragment.price = variantProperties.offerPrice.amount
            }
            else{
                eventConfirmBookingBinding.title = event.title
                eventConfirmBookingBinding.price = event.displayOffer
                //setVariable(BR.title,event.title)
                //setVariable(BR.price,event.displayOffer)
                setVariable(BR.quantity,chosenQuantity.toString())
                if (event.stock == chosenQuantity){
                    enableIncreaseBtn(false)
                    enableDecreaseBtn(false)
                }
                this@EventBookingConfirmFragment.price = event.offerPrice!!.amount
            }
            setVariable(BR.onClickListener,CustomOnClickListener(this@EventBookingConfirmFragment))
            setVariable(BR.startMillis,event.startAt)
            setVariable(BR.endMillis,event.endAt)
            setVariable(BR.timeFormat,DateTimeHelper.FORMAT_TIME_AM_PM)
            setVariable(BR.dateFormat,DateTimeHelper.FORMAT_DATE_EEE_D_MMM_YYYY)
            setVariable(BR.address,event.address)
            executePendingBindings()
        }
    }

    private fun onCheckoutSuccess(orderReference:String){
        val payment = paymentListAdapter.items.find { it.isSelected }!!
        this.orderReference = orderReference
        this.selectedPaymentId = payment.id.toString()
        when{
            payment.type == AppConstant.PaymentTypes.COD -> {
                showSuccessDialog(orderReference,payment.id.toString(),true)
            }
            payment.type == AppConstant.PaymentTypes.STRIPE && payment.channel == AppConstant.PaymentChannel.SDK -> {
                initPaymentSDK(payment.type,orderReference)
            }
            payment.channel == AppConstant.PaymentChannel.WEB ->{
                makeWebPayment(orderReference,payment.id.toString())
            }
        }
    }

    private fun onStripeCheckoutSuccess(){
        showSuccessDialog(orderReference,selectedPaymentId,true)
    }

    private fun initPaymentSDK(type:String,orderReference: String){
        when(type){
            AppConstant.PaymentTypes.STRIPE-> eventBookingViewModel.initStripeSDK(requireContext(),type,orderReference)
        }
    }

    fun showSuccessDialog(orderReference: String, paymentId:String,success:Boolean){
        val dialog = Utils.showDirectCheckOutSuccessDialog(requireContext(),success){
            if(it== tradly.social.R.id.btnOne){
                if(success){
                   requireActivity().finish()
                }
                else{
                    makeWebPayment(orderReference,paymentId)
                }
            }
            else if(it== tradly.social.R.id.txtTwo){
                val intent = Intent(requireContext(), MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
                requireActivity().finish()
            }
        }
        dialog.show()
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

    private fun onStripeActivityResult(data:Triple<Int,Int,Intent>){
        val (requestCode:Int,resultCode:Int,intent:Intent) = data
        eventBookingViewModel.onStripeActivityResult(requestCode, resultCode, intent)
    }

    private fun onPopulatePaymentTypes(list: List<Payment>){
        eventConfirmBookingBinding.parentLayout.setVisible()
        paymentListAdapter.items = list.toMutableList()
        if (paymentListAdapter.items.isNotEmpty()){
            paymentListAdapter.items[0].isSelected = true
            paymentListAdapter.notifyItemChanged(0)
        }
    }

    override fun onCustomClick(view: View) {
        when(view.id){
            R.id.button1-> onIncreaseQuantity()
            R.id.button3-> onDecreaseQuantity()
            R.id.btnConfirmBooking ->{
                val variantId = sharedEventBookingViewModel.eventLiveData.value?.variants?.find { it.isSelected }?.id?.toInt()?:0
                val paymentId = paymentListAdapter.items.find { it.isSelected }?.id?.getOrDefault<Int>()
                val event = sharedEventBookingViewModel.getEvent()!!
                eventBookingViewModel.checkout(event.id,chosenQuantity,variantId,AppConstant.ModuleType.EVENTS,paymentId!!)
            }
        }
    }

    private fun onIncreaseQuantity(){
        val event = sharedEventBookingViewModel.getEvent()!!
        if (event.stock == (chosenQuantity+1)){
            enableIncreaseBtn(false)
            return
        }
        chosenQuantity++
        eventConfirmBookingBinding.apply {
            setVariable(BR.quantity,chosenQuantity.toString())
            executePendingBindings()
        }
        updatePrice()
    }

    private fun enableIncreaseBtn(enable:Boolean){
        eventConfirmBookingBinding.button1.isEnabled = enable
        eventConfirmBookingBinding.button1.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), if (enable)colorPrimary else CommonResourceColor.colorMediumGrey))
    }

    private fun enableDecreaseBtn(enable:Boolean){
        eventConfirmBookingBinding.button3.isEnabled = enable
        eventConfirmBookingBinding.button3.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), if (enable)colorPrimary else CommonResourceColor.colorMediumGrey))
    }

    private fun onDecreaseQuantity(){
        if (chosenQuantity!=1){
            chosenQuantity--
            eventConfirmBookingBinding.apply {
                setVariable(BR.quantity,chosenQuantity.toString())
                executePendingBindings()
            }
            enableIncreaseBtn(true)
            updatePrice()
        }
    }

    private fun updatePrice(){
        val finalValue = chosenQuantity*price
        eventConfirmBookingBinding.tvTotalValue.text = finalValue.toInt().toString()
    }

    private fun handleUIState(uiState: UIState<Unit>){
        when(uiState){
            is UIState.Loading->{
                if (RequestID.GET_PAYMENT_TYPES == uiState.apiId){
                    eventConfirmBookingBinding.progressBar.visibility = if (uiState.isLoading) View.VISIBLE else View.GONE
                }
                else{
                    when(uiState.apiId){
                        RequestID.DIRECT_CHECKOUT,
                        RequestID.GET_EPHEMERAL_KEY,
                        RequestID.GET_INTENT_SECRET,
                        RequestID.STRIPE_PAYMENT_RESULT->{
                            if (uiState.isLoading){
                                showLoader(CommonResourceString.please_wait)
                            }
                            else{
                                hideLoader()
                            }
                        }
                    }
                }
            }
            is UIState.Failure-> ErrorHandler.handleError(uiState.errorData)
        }
    }

    override fun onClick(value: Payment, view: View, itemPosition: Int) {
        when(view.id){
            CommonBaseResourceId.listItemParent->{
                val index = paymentListAdapter.items.indexOfFirst { it.isSelected }
                if (index!=-1){
                    paymentListAdapter.items[index].isSelected = false
                    paymentListAdapter.notifyItemChanged(index)
                }
                value.isSelected = true
                paymentListAdapter.notifyItemChanged(itemPosition)
            }
        }
    }

}