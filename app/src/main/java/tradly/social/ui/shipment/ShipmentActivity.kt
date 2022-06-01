package tradly.social.ui.shipment

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_shipment.*
import kotlinx.android.synthetic.main.layout_price_details.*
import kotlinx.android.synthetic.main.toolbar.*
import tradly.social.R
import tradly.social.adapter.ListingAdapter
import tradly.social.common.base.*
import tradly.social.common.util.parser.extension.toJson
import tradly.social.common.util.parser.extension.toObject
import tradly.social.domain.entities.*
import tradly.social.ui.cart.CartPresenter
import tradly.social.ui.payment.PaymentActivity

class ShipmentActivity : BaseActivity(),CartPresenter.View {
    private lateinit var cartPresenter: CartPresenter
    private var shippingAddress:ShippingAddress? = null
    private lateinit var shipmentAdapter:ListingAdapter
    private var shipmentMethodEnabled:Boolean = false
    private var selectedShippingMethod:ShippingMethod?=null
    private lateinit var account: Store
    private var shipmentList = mutableListOf<ShippingMethod>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shipment)
        setToolbar(toolbar,title = R.string.shipment_header_title,backNavIcon = R.drawable.ic_back)
        account = intent.getStringExtra(AppConstant.BundleProperty.STORE).toObject<Store>()!!
        cartPresenter = CartPresenter(this)
        shipmentMethodEnabled = AppConfigHelper.getConfigKey(AppConfigHelper.Keys.SHIPPING_METHOD_PREFERENCE)
        shipmentAdapter = ListingAdapter(this,shipmentList,
            AppConstant.ListingType.SHIPMENT_METHOD_SELECT_LIST,null){ position, obj ->
            if(NetworkUtil.isConnectingToInternet(true)){
                selectedShippingMethod = obj as ShippingMethod
                cartPresenter.getCartByShipment(selectedShippingMethod!!,CartPresenter.CartAction.CART_BY_SELECT_SHIPMENT)
            }
        }
        btnCheckOut?.safeClickListener { showPayment() }
        shipment_recycler_view?.layoutManager = LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)
        shipment_recycler_view?.adapter = shipmentAdapter
        if(shipmentMethodEnabled){
            cartPresenter.getShippingMethods(account.id)
        }
        else{
            cartPresenter.getShippingMethods(AppConstant.EMPTY)
        }
    }

    private fun showPayment(){
        if(NetworkUtil.isConnectingToInternet(true)){
            if(selectedShippingMethod!=null){
                if(selectedShippingMethod!!.type== AppConstant.ShipmentType.PICK_UP || shippingAddress!=null){
                    val args = Bundle().apply{
                       if(selectedShippingMethod!!.type == AppConstant.ShipmentType.DELIVERY){
                           putString(AppConstant.BundleProperty.ADDRESS_ID,shippingAddress!!.id)
                       }
                        putString(AppConstant.BundleProperty.SHIPPING_METHOD,selectedShippingMethod.toJson<ShippingMethod>())
                    }
                    startActivity(PaymentActivity::class.java,args)
                }
                else{
                    showToast(R.string.payment_select_delivery_address)
                }
            }
            else{
                showToast(R.string.shipment_alert_message_choose_shipment)
            }
        }
    }

    override fun onSuccess(cartResult: Cart, isFor: Int) {
        cvPriceLayout?.visibility = View.VISIBLE
        blLayout?.visibility = View.VISIBLE
        if(selectedShippingMethod!=null){
            if(selectedShippingMethod!!.type== AppConstant.ShipmentType.PICK_UP){
                showPickupAddress()
            }
        }
        setPriceDetails(cartResult)
    }

    override fun onFailure(appError: AppError) {
        ErrorHandler.handleError(this,appError)
    }

    override fun showShipmentList(list: List<ShippingMethod>) {
        shipment_layout?.visibility = View.VISIBLE
        list.forEach { it.default = false }
        list[0].default = true
        shipmentList.addAll(list)
        shipmentAdapter.notifyDataSetChanged()
        selectedShippingMethod = list[0]
        if(AppConstant.ShipmentType.PICK_UP==list[0].type){
            showPickupAddress()
        }
    }

    override fun onCartItemRemoved() {

    }

    override fun showProgressLoader() {
        progressBar?.visibility = View.VISIBLE
    }

    override fun hideProgressLoader() {
        progressBar?.visibility = View.GONE
    }

    override fun showProgressDialog() {
        showLoader(R.string.please_wait)
    }

    override fun hideProgressDialog() {
        hideLoader()
    }

    override fun isItemInCart(isInItem: Boolean) {

    }

    private fun showPickupAddress(){
        currentAddressLayout?.visibility = View.GONE
        if(account.address.formattedAddress.isNotEmpty()){
            pickup_address_layout?.visibility = View.VISIBLE
            pickup_address_text?.text = account.address.formattedAddress
        }

    }

    override fun showCurrentAddress(shippingAddress: ShippingAddress?) {
        if(intent.hasExtra("address")){
            this.shippingAddress = intent.getStringExtra("address").toObject<ShippingAddress>()
        }
        else{
            this.shippingAddress = shippingAddress
        }
        currentAddressLayout?.visibility= View.VISIBLE
        pickup_address_layout?.visibility = View.GONE
        this.shippingAddress?.let {
            addAddressLayout?.visibility = View.GONE
            deliveryAddressLayout?.visibility = View.VISIBLE
            actionChange?.setOnClickListener {
                val intent = Intent(this,AddressListActivity::class.java)
                intent.putExtra(AppConstant.BundleProperty.SHIPMENT_TYPE,selectedShippingMethod?.type)
                startActivityForResult(intent,100)
            }
            txtOne?.text = String.format(getString(R.string.shipment_delivery_to),it.name,it.zipcode)
            txtTwo?.text = ShippingPresenter.getFormedAddress(it)
        }?:run{
            addAddressLayout?.visibility = View.VISIBLE
            addAddressLayout?.setOnClickListener {
                val intent = Intent(this,ManageShippingAddressActivity::class.java)
                intent.putExtra("isFromCart",true)
                intent.putExtra("isFor", AppConstant.ManageAction.ADD)
                intent.putExtra(AppConstant.BundleProperty.SHIPMENT_TYPE,selectedShippingMethod?.type)
                startActivityForResult(intent,100)
            }
        }
    }

    override fun onCartAdded() {

    }

    override fun noNetwork() {
        showToast(R.string.no_internet)
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
        txtDelivery?.visibility = View.GONE
        deliveryInfoLayout?.visibility = View.GONE
        if(selectedShippingMethod!=null){
            if(selectedShippingMethod!!.type == AppConstant.ShipmentType.DELIVERY && cartResult.cartDetail.shippingTotal.shippingEnabled){
                deliveryInfoLayout?.visibility = View.VISIBLE
                txtDelivery?.visibility = View.VISIBLE
                txtDeliveryValue?.text = cartResult.cartDetail.shippingTotal.formattedAmount
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode ==100 && data!=null){
            if(data.getBooleanExtra("isAdded",false)){
                selectedShippingMethod?.let {
                    cartPresenter.getCurrentAddress(it.type)
                }
                this.intent.putExtra("address",data.getStringExtra("address"))
            }
        }
    }
}