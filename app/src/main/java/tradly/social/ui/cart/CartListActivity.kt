package tradly.social.ui.cart

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_cart_list.*
import kotlinx.android.synthetic.main.layout_price_details.*
import kotlinx.android.synthetic.main.toolbar.*
import tradly.social.R
import tradly.social.adapter.ListingAdapter
import tradly.social.common.base.*
import tradly.social.common.util.parser.extension.toJson
import tradly.social.domain.entities.*
import tradly.social.ui.main.MainActivity
import tradly.social.ui.shipment.ShipmentActivity

class CartListActivity : BaseActivity(), CartPresenter.View {

    private var listingAdapter: ListingAdapter? = null
    private var cartPresenter: CartPresenter? = null
    private var cartList = mutableListOf<CartItem>()
    private var shipmentMethodEnabled:Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart_list)
        setToolbar(toolbar, R.string.cart_header_title, R.drawable.ic_back)
        cartPresenter = CartPresenter(this)
        shipmentMethodEnabled = AppConfigHelper.getConfigKey(AppConfigHelper.Keys.SHIPPING_METHOD_PREFERENCE)
        initView()
        setListeners()
        cartPresenter?.getCartItems()
    }


    private fun initView(){
        recycler_view?.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        listingAdapter = ListingAdapter(this, cartList, AppConstant.ListingType.CART_LIST, recycler_view) { position, obj ->
            if (obj is CartItem) {
                val cart = obj as? CartItem
                cart?.let { cartPresenter?.removeCartItem(it.listing.id) }
            } else if (obj is String) {
                addCartQty(cartList[position],obj as String)
            }
        }
        recycler_view?.adapter = listingAdapter
    }

    private fun setListeners(){
        btnShopNow?.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        }

        imgCancel?.setOnClickListener {
            clWarning?.visibility = View.GONE
        }
        btnCheckOut?.safeClickListener {
            if(NetworkUtil.isConnectingToInternet(true)){
                val store = cartList[0].listing.store
                startActivity(ShipmentActivity::class.java,Bundle().apply { putString(AppConstant.BundleProperty.STORE,store.toJson<Store>()) })
            }
        }
    }

    override fun onSuccess(cartResult: Cart, isFor: Int) {
        if (cartResult.cartList.isNotEmpty()) {
            cartList.clear()
            cartList.addAll(cartResult.cartList)
            //clWarning?.visibility = if (cartResult.itemRemoved) View.VISIBLE else View.GONE
            listingAdapter?.notifyDataSetChanged()
            txtEmptyStateMsg?.visibility = View.GONE
            cvPriceLayout?.visibility = View.VISIBLE
            blLayout?.visibility = View.VISIBLE
            setPriceDetails(cartResult)
        } else {
            cvPriceLayout?.visibility = View.GONE
            blLayout?.visibility = View.GONE
            txtEmptyStateMsg?.visibility = View.VISIBLE
            btnShopNow?.visibility = View.VISIBLE
            cartList.clear()
            listingAdapter?.notifyDataSetChanged()
        }

        when(isFor){
            CartPresenter.CartAction.CART_REMOVE->{
                Toast.makeText(this, getString(R.string.cart_item_removed_cart), Toast.LENGTH_SHORT).show()
            }
            CartPresenter.CartAction.CART_QTY->{}
        }
    }

    private fun setPriceDetails(cartResult: Cart) {
        cvPriceLayout?.visibility = View.GONE
        if(cartResult.cartList.count()>1){
            txtPriceCart?.text = String.format(getString(R.string.payment_prices), cartResult.cartList.count())
        }
        else{
            txtPriceCart?.text = String.format(getString(R.string.payment_price), cartResult.cartList.count())
        }
        txtOfferTotal?.text = cartResult.cartDetail.offerTotal.displayCurrency
        txtGrandTotal?.text = cartResult.cartDetail.grandTotal.displayCurrency
        txtTotalAmount?.text = cartResult.cartDetail.grandTotal.displayCurrency
    }

    override fun showCurrentAddress(shippingAddress: ShippingAddress?) {
    }

    private fun addCartQty(cart: CartItem,qty:String){
        cartPresenter?.addCartItem(cart.listing.id, AppConstant.CartType.CART_PRODUCT,qty.toInt())
    }
    override fun onCartItemRemoved() {
        cartPresenter?.getCartItems(CartPresenter.CartAction.CART_REMOVE)
        showToast(getString(R.string.cart_item_removed_cart))
    }

    override fun onCartAdded() {
        cartPresenter?.getCartItems(CartPresenter.CartAction.CART_QTY)
    }

    override fun onFailure(appError: AppError) {
        ErrorHandler.handleError(this, appError)
        hideProgressDialog()
    }

    override fun noNetwork() {

    }

    override fun isItemInCart(isInItem: Boolean) {

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


    override fun showShipmentList(list: List<ShippingMethod>) {

    }
}
