package tradly.social.ui.payment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_payment.*
import kotlinx.android.synthetic.main.layout_price_details.*
import kotlinx.android.synthetic.main.toolbar.*
import tradly.social.R
import tradly.social.adapter.ListingAdapter
import tradly.social.common.*
import tradly.social.common.base.*
import tradly.social.common.navigation.Activities
import tradly.social.common.util.parser.extension.toObject
import tradly.social.domain.entities.*
import tradly.social.ui.main.MainActivity

class PaymentActivity : BaseActivity(), PaymentPresenter.View, CustomOnClickListener.OnCustomClickListener {

    private var paymentPresenter: PaymentPresenter? = null
    private lateinit var listingAdapter: ListingAdapter
    private var isOrderSuccess:Boolean = false
    private lateinit var shipmentMethod:ShippingMethod
    private var paymentList = mutableListOf<Payment>()
    private lateinit var cart:Cart
    private lateinit var orderReference: String
    private lateinit var selectedPaymentId:String
    private lateinit var dialog: AlertDialog
    private var isFromEvent:Boolean = false
    private lateinit var event: Event

    companion object{
        const val EXTRAS_IS_FROM_EVENT = "isFromEvent"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)
        setToolbar(toolbar, R.string.payment_header_title, R.drawable.ic_back)
        paymentPresenter = PaymentPresenter(this)
        with(CustomOnClickListener(this)){
            btnCheckOut.setOnClickListener(this)
            txtContinue.setOnClickListener(this)
            btnMyOrder.setOnClickListener(this)
        }
        if(intent.getBooleanExtra(AppConstant.BundleProperty.IS_FROM_WEB_PAYMENT,false)){
            setAndShowPaymentStatusDialog()
        }
        else if (getIntentExtra<Boolean>(EXTRAS_IS_FROM_EVENT)){
            isFromEvent = true
            event = getIntentExtra<String>(Activities.BookingHostActivity.EXTRAS_EVENT).toObject<Event>()!!

        }
        else{
            setAdapter()
            shipmentMethod = intent.getStringExtra(AppConstant.BundleProperty.SHIPPING_METHOD).toObject<ShippingMethod>()!!
            paymentPresenter?.getPriceDetails(shipmentMethod.id.toString())
        }
    }

    fun setAndShowPaymentStatusDialog(){
        orderReference = getStringData(AppConstant.BundleProperty.ORDER_REFERENCE)
        selectedPaymentId = getStringData(AppConstant.BundleProperty.PAYMENT_METHOD_ID)
        switchToSuccessUI(intent.getBooleanExtra(AppConstant.BundleProperty.STATUS,false))
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        this.intent = intent
        intent?.let {
            if(intent.getBooleanExtra(AppConstant.BundleProperty.IS_FROM_WEB_PAYMENT,false)) {
               setAndShowPaymentStatusDialog()
            }
        }
    }

    override fun onCustomClick(view: View) {
        when(view.id){
            R.id.btnCheckOut-> checkOut()
            R.id.btnMyOrder->launchHome()
            R.id.txtContinue->{}
        }
    }

    private fun checkOut(){
        paymentList.find { it.default }?.let { payment ->
            if (::cart.isInitialized) {
                paymentPresenter?.checkOutCart(cart.cartDetail.grandTotal,getStringData(AppConstant.BundleProperty.ADDRESS_ID).toInteger(),
                    payment,
                    shipmentMethod.id
                )
            }
        } ?: run {
            showToast(R.string.payment_select_payment)
        }
    }

    override fun showPriceDetails(cartResult: Cart) {
        this.cart = cartResult
        cvPriceLayout?.visibility = View.VISIBLE
        blLayout?.visibility = View.VISIBLE
        setPriceDetails(cartResult)
    }

    override fun onFailure(appError: AppError) {
        ErrorHandler.handleError(this ,appError)
        hideProgressLoader()
        showToast(getString(R.string.something_went_wrong))
    }

    override fun showProgressLoader() {
        progressBar?.visibility = View.VISIBLE
    }

    override fun hideProgressLoader() {
        progressBar?.visibility = View.GONE
    }


    override fun onSuccessOrderCancel() {
        showToast(R.string.alert_message_order_cancelled_successfully)
        launchHome()
    }


    override fun onCheckOutStatus(orderReference: String ,paymentTypeId:Int,paymentType:String,channel:String,isSuccess:Boolean) {
        when{
            paymentType == AppConstant.PaymentTypes.COD || isSuccess -> switchToSuccessUI(true)
            paymentType == AppConstant.PaymentTypes.STRIPE && channel == AppConstant.PaymentChannel.SDK -> initPaymentSDK(paymentType,orderReference)
            paymentType == AppConstant.PaymentTypes.PAYULATAM && channel == AppConstant.PaymentChannel.WEB ->{
                paymentPresenter?.makeWebPayment(orderReference,paymentTypeId.toString())
            }
        }
        this.selectedPaymentId = paymentTypeId.toString()
        this.orderReference = orderReference
    }

    override fun showProgressDialog(msg: Int) {
        showLoader(msg)
    }

    override fun hideProgressDialog() {
        hideLoader()
    }

    private fun switchToSuccessUI(isSuccess: Boolean){
        isOrderSuccess = true
        if(!isFinishing){
            dialog = Utils.showCheckOutSuccessDialog(this,isSuccess){
                if(it==R.id.btnOne){
                   if(isSuccess){
                       launchHome()
                       dialog.dismiss()
                   }
                   else{
                       paymentPresenter?.makeWebPayment(this.orderReference,this.selectedPaymentId)
                    }
                }
                else if(it==R.id.txtTwo){
                    launchHome(true)
                    dialog.dismiss()
                }
            }
            dialog.show()
        }
    }

    private fun initPaymentSDK(type:String,orderReference: String){
        when(type){
            AppConstant.PaymentTypes.STRIPE-> paymentPresenter?.initStripeSDK(this,type,orderReference)
        }
    }

    private fun setAdapter(){
        paymentTypeList.layoutManager = LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)
        listingAdapter = ListingAdapter(this,paymentList,
            AppConstant.ListingType.PAYMENT_TYPES,paymentTypeList){ position, obj ->

        }
        paymentTypeList.adapter = listingAdapter
    }

    private fun setPriceDetails(cartResult: Cart) {
        txtOfferTotal?.text = cartResult.cartDetail.offerTotal.displayCurrency
        txtGrandTotal?.text = cartResult.cartDetail.grandTotal.displayCurrency
        txtTotalAmount?.text = cartResult.cartDetail.grandTotal.displayCurrency
        if(cartResult.cartList.count()>1){
            txtPriceCart?.text = String.format(getString(R.string.payment_prices), cartResult.cartList.count())
        }
        else{
            txtPriceCart?.text = String.format(getString(R.string.payment_price), cartResult.cartList.count())
        }
        if(shipmentMethod.type== AppConstant.ShipmentType.DELIVERY){
            val shipping = cartResult.cartDetail.shippingTotal
            if (shipping.shippingEnabled) {
                txtDeliveryValue.text = shipping.formattedAmount
                infoIcon?.visibility = View.GONE
                deliveryInfoLayout?.setOnClickListener(null)
                txtDelivery?.visibility = View.VISIBLE
                deliveryInfoLayout?.visibility = View.VISIBLE
            }
            else{
                txtDelivery?.visibility = View.GONE
                deliveryInfoLayout?.visibility = View.GONE
            }
        }
    }

    override fun showPaymentTypes(list: List<Payment>) {
        if(!list.isNullOrEmpty()){
            paymentModeLayout?.visibility = View.VISIBLE
            list.forEach { it.default = false }
            list[0].default = true
            paymentList.clear()
            paymentList.addAll(list)
            listingAdapter.notifyDataSetChanged()
        }
    }

    private fun launchHome(isForMyOrder:Boolean = false){
        val intent = Intent(this,MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        if(isForMyOrder){
            intent.putExtra(AppConstant.BundleProperty.IS_FOR_MY_ORDER,true)
        }
        startActivity(intent)
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        data?.let {
           if(Activity.RESULT_OK==resultCode){
               paymentPresenter?.onActivityResult(requestCode,resultCode,it)
           }
        }
    }

    override fun showError(messageRes: Int) {
        if (messageRes == R.string.cart_alert_message_min_balance_checkout) {
            cart.cartDetail.grandTotal.apply {
                Utils.showAlertDialog(this@PaymentActivity,Constant.EMPTY,getString(messageRes,this.currency,this.amount.toString()),false,false,object:
                    Utils.DialogInterface{
                    override fun onAccept() {

                    }

                    override fun onReject() {

                    }
                })
            }
        }
    }

    override fun onBackPressed() {
       if(isOrderSuccess){
           launchHome()
       }
        else{
           super.onBackPressed()
       }
    }

}
