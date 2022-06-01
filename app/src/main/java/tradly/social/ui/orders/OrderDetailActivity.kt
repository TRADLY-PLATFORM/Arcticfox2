package tradly.social.ui.orders

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.moe.pushlibrary.utils.MoEHelperUtils
import kotlinx.android.synthetic.main.activity_order_detail.*
import tradly.social.R
import tradly.social.adapter.CountryAdapter
import tradly.social.adapter.ListingAdapter
import tradly.social.common.*
import tradly.social.domain.entities.ImageFeed
import tradly.social.common.base.*
import tradly.social.common.network.CustomError
import tradly.social.common.util.parser.extension.toJson
import tradly.social.common.util.parser.extension.toList
import tradly.social.common.util.parser.extension.toObject
import tradly.social.domain.entities.*
import tradly.social.ui.message.thread.ChatThreadActivity
import tradly.social.ui.shipment.AddressListActivity
import tradly.social.ui.store.storeDetail.StoreDetailActivity

class OrderDetailActivity : BaseActivity(), OrderDetailPresenter.View {

    private lateinit var orderDetailPresenter: OrderDetailPresenter
    private  var isMyStoreOrder:Boolean = false
    private  lateinit var orderDetail:OrderDetail
    private lateinit var orderId:String
    private lateinit var accountId:String
    private var shouldFetchOrderDetail:Boolean = false
    private var bottomChooserDialog:BottomChooserDialog?=null
    private lateinit var productListAdapter:ListingAdapter
    private var orderList = mutableListOf<OrderedProductEntity>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_detail)
        setToolbar(toolbar, R.string.orderdetail_order_detail, R.drawable.ic_back)
        isMyStoreOrder = intent.getBooleanExtra(AppConstant.BundleProperty.IS_MY_STORE_ORDER,false)
        accountId = getStringData(AppConstant.BundleProperty.ACCOUNT_ID)
        orderId = getStringData(AppConstant.BundleProperty.ORDER_ID)
        orderDetailPresenter = OrderDetailPresenter(this)
        orderDetailPresenter.getOrderDetails(orderId,accountId)
        setListener()
        setAdapters()
    }

    fun setListener(){
        btnStatusOne?.safeClickListener {
            if(!isMyStoreOrder && ::orderDetail.isInitialized){
                orderDetailPresenter.updateOrderStatus(orderId,accountId,orderDetail.nextStatus[0])
            }
        }

        btnStatusTwo?.safeClickListener {
            showSellerStatusDialog()
        }

        btnAddPickupAddress?.safeClickListener {
            val intent = Intent(this, AddressListActivity::class.java)
            intent.putExtra(AppConstant.BundleProperty.IS_FROM_ORDER_DETAIL,true)
            startActivityForResult(intent,100)
        }

        actionChange?.safeClickListener {
            val intent = Intent(this, AddressListActivity::class.java)
            intent.putExtra(AppConstant.BundleProperty.IS_FROM_ORDER_DETAIL,true)
            startActivityForResult(intent,100)
        }

        swipeRefresh?.setOnRefreshListener {
            shouldFetchOrderDetail = false
            orderDetailPresenter.getOrderDetails(orderId,accountId,false)
        }
    }

    private fun showSellerStatusDialog(){
        val list = this.orderDetailPresenter.getNextStatus(orderDetail.nextStatus)
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle(tradly.social.common.base.R.string.orderdetail_select_status)
        val adapter = CountryAdapter(list,this, AppConstant.ListingType.SELLER_STATUS_LIST)
        alertDialog.setNegativeButton(android.R.string.cancel) { p0, p1 ->}
        alertDialog.setPositiveButton(android.R.string.ok) { p0, p1 ->
            list.find { it.first }?.apply {
                orderDetailPresenter.updateOrderStatus(orderId,accountId,this.second.first)
            }
        }
        alertDialog.setAdapter(adapter) { p0, pos -> }
        alertDialog.create().show()
    }

    private fun setAdapters(){
        productListAdapter = ListingAdapter(
            this,
            orderList,
            AppConstant.ListingType.MY_ORDER_LIST,
            recyclerViewProducts
        ) { pos, any ->
               bottomChooserDialog = ViewUtil.showReviewSheet(AppConstant.ModuleType.LISTINGS,any){ resultBundle->
                val images = resultBundle.getString(AppConstant.BundleProperty.IMAGES).toList<ImageFeed>()
                orderDetailPresenter.submitReview(
                    resultBundle.getString(AppConstant.BundleProperty.ID)!!,
                    resultBundle.getInt(AppConstant.BundleProperty.REVIEW_TITLE),
                    resultBundle.getString(AppConstant.BundleProperty.REVIEW_CONTENT)!!,
                    resultBundle.getInt(AppConstant.BundleProperty.REVIEW_RATING),
                    images = images!!,
                    callback = {
                        if(it){
                            showToast(getString(R.string.addreview_review_success_message))
                            if(!isFinishing){
                                bottomChooserDialog?.let {  dialog->
                                    if(dialog.isAdded)dialog.dismiss()
                                }
                            }
                        }
                    }
                )
            }
            bottomChooserDialog?.show(supportFragmentManager, AppConstant.FragmentTag.BOTTOM_CHOOSER_FRAGMENT)
        }
        recyclerViewProducts.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false)
        recyclerViewProducts.addItemDecoration(VerticalSpaceItemDecoration(25))
        recyclerViewProducts.adapter = productListAdapter
    }

    override fun onResume() {
        super.onResume()
        if(shouldFetchOrderDetail){
            orderDetailPresenter.getOrderDetails(orderId,accountId,false)
        }
    }

    override fun getOrderDetails(orderDetail: OrderDetail) {
        this.orderDetail = orderDetail
        shouldFetchOrderDetail = true
        nestedScrollView?.visibility = View.VISIBLE
        swipeRefresh?.isRefreshing = false
        if (orderDetail.orderedProductEntities.size > 2) {
            btnReviewAllProducts.visibility = View.VISIBLE
        }
        if(isMyStoreOrder){
            orderDetail.orderedProductEntities.forEach { it.reviewStatus = true }
        }
        textAmount.text = orderDetail.grandTotal.displayCurrency
        textOrderId.text = orderDetail.orderId

        setDeliveryInfo(orderDetail)
        setPickupAddressInfo(orderDetail)
        this.orderList.clear()
        this.orderList.addAll(orderDetail.orderedProductEntities)
        productListAdapter.notifyDataSetChanged()
        val timelineListAdapter = ListingAdapter(
            this,
            orderDetail.orderStatusEntities,
            AppConstant.ListingType.ORDER_TIME_LINE_LIST,
            recyclerViewOrderTimeline
        ) { _, _ ->
        }
        recyclerViewOrderTimeline.layoutManager = LinearLayoutManager(this)
        recyclerViewOrderTimeline.adapter = timelineListAdapter

        if(orderDetail.nextStatus.isNotEmpty()){
            if(!isMyStoreOrder){
                if(orderDetail.nextStatus.size==1){
                    btnStatusOne?.visibility = View.VISIBLE
                    btnStatusOne?.setText(Utils.getOrderStatus(orderDetail.nextStatus[0]))
                }
                else{
                    btnStatusTwo?.visibility = View.VISIBLE
                }
            }
            else{
                btnStatusTwo?.visibility = View.VISIBLE
            }
        }
        else{
            btnStatusOne?.visibility = View.GONE
            btnStatusTwo?.visibility = View.GONE
        }

        setUserInfo(orderDetail)
    }

    private fun setDeliveryInfo(orderDetail: OrderDetail){
        if(orderDetail.shipmentMethod == AppConstant.ShipmentType.DELIVERY){
            CardViewDeliveryAddress?.visibility = View.VISIBLE
            textName.text = orderDetail.orderAddressEntity.name
            textAddress.text = orderDetail.orderAddressEntity.address
            textMobileNo.text = orderDetail.orderAddressEntity.phoneNumber
        }
        else{
            CardViewDeliveryAddress?.visibility = View.GONE
        }
    }

    private fun setPickupAddressInfo(orderDetail: OrderDetail){
        if(orderDetail.shipmentMethod == AppConstant.ShipmentType.PICK_UP){
            if(orderDetail.pickupAddressEntity!=null){
                cardViewPickupAddress?.visibility = View.VISIBLE
                btnAddPickupAddress?.visibility = View.GONE
                textPickupAddressName.text = orderDetail.pickupAddressEntity!!.name
                textPickupAddress.text = orderDetail.pickupAddressEntity!!.address
                textPickupAddressMobileNo.text = orderDetail.pickupAddressEntity!!.phoneNumber
                actionChange?.visibility = View.INVISIBLE
                if(isMyStoreOrder){
                    actionChange?.visibility = View.VISIBLE
                }
            }
            else{
                cardViewPickupAddress?.visibility = View.GONE
                if(isMyStoreOrder){
                    btnAddPickupAddress?.visibility = View.VISIBLE
                }
            }
         }
        else{
            cardViewPickupAddress?.visibility = View.GONE
        }
        }


    private fun setUserInfo(orderDetail: OrderDetail){
        val user = if(isMyStoreOrder)orderDetail.user else orderDetail.store.user
        if(user!=null){
            app_bar?.visibility = View.VISIBLE
            userProfile?.setImageByUrl(this,user.profilePic,R.drawable.ic_user_placeholder)
            userName?.text = user.name
            btnActionThree?.safeClickListener {
                showProgressDialogLoader()
                val currentUser = AppController.appController.getUser()!!
                val providerName = if(isMyStoreOrder)orderDetail.user.name else orderDetail.store.storeName
                MessageHelper.checkChatRoomAvailable(currentUser.name,currentUser.id,user.id,providerName,currentUser.profilePic,user.profilePic,object :MessageHelper.Companion.ChatListener{
                    override fun onSuccess(any: Any?) {
                        hideProgressDialogLoader()
                        (any as? ChatRoom)?.let {
                            startActivity(ChatThreadActivity::class.java, Bundle().apply { putString(
                                AppConstant.BundleProperty.CHAT_ROOM,it.toJson<ChatRoom>()) })
                        }
                    }

                    override fun onFailure(message: String?, code: Int) {
                        hideProgressDialogLoader()
                    }
                })
            }

            userName.safeClickListener {
                val store = orderDetail.store
                startActivity(StoreDetailActivity.newIntent(this, store.id, store.storeName))
            }
        }
    }

    override fun showProgressLoader() {
        progressBar?.visibility = View.VISIBLE
    }

    override fun hideProgressLoader() {
        progressBar?.visibility = View.GONE
    }

    override fun showProgressDialogLoader() {
        showLoader(R.string.please_wait)
    }

    override fun hideProgressDialogLoader() {
        hideLoader()
    }

    override fun onFailure(appError: AppError) {
        ErrorHandler.handleError(this, appError)
        if(appError.code == CustomError.CODE_PICKUP_ADDRES_NOT_SET){
            Utils.showAlertDialog(this,
                AppConstant.EMPTY,getString(R.string.address_alert_message_add_pickup_address),true,false,null)
        }
        else{
            MoEHelperUtils.showToast(getString(R.string.something_went_wrong), this)
        }
        shouldFetchOrderDetail = true
        swipeRefresh?.isRefreshing = false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==100 && resultCode==100&& data!= null){
            if(data.getBooleanExtra("isAdded",false)){
                val address = data.getStringExtra("address")
                if(!address.isNullOrEmpty()){
                    shouldFetchOrderDetail = false
                    val shippingAddress =address.toObject<ShippingAddress>()
                    orderDetailPresenter.updatePickupAddress(orderId,accountId,shippingAddress!!)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        orderDetailPresenter.onDestroy()
    }
}
