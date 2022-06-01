package tradly.social.ui.shipment

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import tradly.social.R
import tradly.social.adapter.ListingAdapter
import tradly.social.common.base.AppConstant
import tradly.social.domain.entities.Address
import tradly.social.domain.entities.AppError
import tradly.social.domain.entities.ShippingAddress
import tradly.social.common.base.BaseActivity
import kotlinx.android.synthetic.main.activity_address_list.*
import kotlinx.android.synthetic.main.toolbar.*
import tradly.social.common.base.getStringData
import tradly.social.common.util.parser.extension.toJson

class AddressListActivity : BaseActivity(), ShippingPresenter.View {

    private var isChanged:Boolean=false
    private var list = mutableListOf<ShippingAddress>()
    private var listingAdapter: ListingAdapter? = null
    private var shippingPresenter: ShippingPresenter? = null
    private var currentSelectedAddress:ShippingAddress? = null
    private var isFromOrderDetail:Boolean = false
    private var shipmentType:String = AppConstant.EMPTY
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_address_list)
        setToolbar(toolbar, R.string.address_my_address, R.drawable.ic_back)
        toolbar?.setNavigationOnClickListener {
            onBackPressed()
        }
        shipmentType = getStringData(AppConstant.BundleProperty.SHIPMENT_TYPE)
        shippingPresenter = ShippingPresenter(this)
        isFromOrderDetail = intent.getBooleanExtra(AppConstant.BundleProperty.IS_FROM_ORDER_DETAIL,false)
        recycler_view?.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        listingAdapter = ListingAdapter(this, list, AppConstant.ListingType.ADDRESS_LIST, recycler_view) { position, obj ->
                isChanged = true
                val previousAddress = list.filter { t -> t.defaultAddress}.singleOrNull()
                currentSelectedAddress = list[position]
                previousAddress?.let {
                    list.forEach {
                        it.defaultAddress = false
                    }
                    list[position].defaultAddress = true
                    listingAdapter?.notifyDataSetChanged()
                }
            }
        listingAdapter?.setPickupAddress(isFromOrderDetail)
        recycler_view?.adapter = listingAdapter
        shippingPresenter?.getAddressList(shipmentType,isFromOrderDetail)
        addAddress?.setOnClickListener {
            val intent = Intent(this,ManageShippingAddressActivity::class.java)
            intent.putExtra("isFor", AppConstant.ManageAction.ADD)
            intent.putExtra(AppConstant.BundleProperty.IS_FOR_PICK_ADDRESS,isFromOrderDetail)
            intent.putExtra(AppConstant.BundleProperty.SHIPMENT_TYPE,shipmentType)
            startActivityForResult(intent,100)
        }
    }

    override fun onLoadAddressList(list: List<ShippingAddress>) {
        this.list.clear()
        this.list.addAll(list)
        listingAdapter?.notifyDataSetChanged()
        txtEmptyStateMsg?.visibility = if(list.isEmpty())View.VISIBLE else View.GONE
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 100 && data != null && data.getBooleanExtra("isAdded",false)){
            list.clear()
            listingAdapter?.notifyDataSetChanged()
            shippingPresenter?.getAddressList(shipmentType,isFromOrderDetail)
        }

    }
    override fun noNetwork() {
        Toast.makeText(this, getString(R.string.no_internet), Toast.LENGTH_SHORT).show()
    }

    override fun showProgressLoader() {
        progressBar?.visibility = View.VISIBLE
    }

    override fun hideProgressLoader() {
        progressBar?.visibility = View.GONE
    }

    override fun showProgressDialog() {

    }

    override fun hideProgressDialog() {

    }

    override fun onFailure(appError: AppError) {

    }

    override fun onSuccess(shippingAddress: ShippingAddress) {

    }

    override fun fieldError(id: Int, msg: Int) {
    }

    override fun showLocationFetchingError() {

    }

    override fun onLoadCurrentLocationAddress(address: Address) {

    }
    private fun finishActivity(){
        val intent = Intent()
        intent.putExtra("isAdded",isChanged)
        intent.putExtra("address",currentSelectedAddress.toJson<ShippingAddress>())
        setResult(100,intent)
        finish()
    }

    override fun onBackPressed() {
       finishActivity()
    }
}
