package tradly.social.ui.shipment

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_manage_shipping_address.*
import kotlinx.android.synthetic.main.toolbar.*
import tradly.social.R
import tradly.social.common.base.*
import tradly.social.common.util.parser.extension.toJson
import tradly.social.domain.entities.Address
import tradly.social.domain.entities.AppError
import tradly.social.domain.entities.Constant
import tradly.social.domain.entities.ShippingAddress


class ManageShippingAddressActivity : BaseActivity(), ShippingPresenter.View {

    private var isAdded: Boolean = false
    private var isFromCart: Boolean = false
    private var presenter: ShippingPresenter? = null
    private var isFor: Int = 0
    lateinit var address: ShippingAddress
    private var isForPickupAddress:Boolean = false
    private var shipmentType:String = AppConstant.EMPTY
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(tradly.social.R.layout.activity_manage_shipping_address)
        setToolbar(toolbar, backNavIcon = tradly.social.R.drawable.ic_back)
        toolbar?.setNavigationOnClickListener {
            onBackPressed()
        }
        presenter = ShippingPresenter(this)
        intent?.let {
            shipmentType = getStringData(AppConstant.BundleProperty.SHIPMENT_TYPE)
            isForPickupAddress = it.getBooleanExtra(AppConstant.BundleProperty.IS_FOR_PICK_ADDRESS,false)
            isFromCart = it.getBooleanExtra("isFromCart", false)
            isFor = it.getIntExtra("isFor", 0)
            when (isFor) {
                AppConstant.ManageAction.EDIT -> {
                    supportActionBar?.title = if(isForPickupAddress){
                        getString(R.string.address_edit_pickup_address)
                    }
                    else{
                        getString(R.string.address_edit_address)
                    }
                    address = Gson().fromJson(it.getStringExtra("address"), ShippingAddress::class.java)
                    populateData(address)
                }
                AppConstant.ManageAction.ADD -> {
                    supportActionBar?.title = if(isForPickupAddress){
                        getString(R.string.address_add_pickup_address)
                    }
                    else{
                        getString(R.string.address_add_new_address)
                    }
                }
                else -> supportActionBar?.title = getString(R.string.address_add_new_address)
            }
        }

        btnCreateAddress?.setOnClickListener {
            when (isFor) {
                AppConstant.ManageAction.ADD -> {
                    presenter?.addAddress(
                        isFromCart,
                        edName.getString(),
                        edPhone.getString(),
                        edStAddressOne.getString(),
                        edStAddressTwo.getString(),
                        edLandMark.getString(),
                        edCity.getString(),
                        edState.getString(),
                        edCountry.getString(),
                        edZipCode.getString(),
                        shipmentType,
                        isForPickupAddress
                    )
                }
                AppConstant.ManageAction.EDIT -> {
                    presenter?.updateAddress(
                        address,
                        edName.getString(),
                        edPhone.getString(),
                        edStAddressOne.getString(),
                        edStAddressTwo.getString(),
                        edLandMark.getString(),
                        edCity.getString(),
                        edState.getString(),
                        edCountry.getString(),
                        edZipCode.getString(),
                        if (isForPickupAddress) AppConstant.AddressType.PICKUP_ADDRESS else if (shipmentType == AppConstant.ShipmentType.DELIVERY) AppConstant.AddressType.SHIPPING else AppConstant.AddressType.STORAGE_HUB
                    )
                }
            }
        }

        currentLocationLayout?.setOnClickListener {
            if (PermissionHelper.checkPermission(this, PermissionHelper.Permission.LOCATION)) {
                checkSettings()
            } else {
                requestPermission()
            }
        }

        initView()
    }

    fun initView(){
        AppConfigHelper.getConfigKey<Boolean>(AppConfigHelper.Keys.HIDE_LANDMARK).let {
            landMarkHint.visibility = if(it)View.GONE else View.VISIBLE
            edLandMark.visibility = if(it)View.GONE else View.VISIBLE
        }
    }

    private fun populateData(address: ShippingAddress) {
        edName.setText(address.name)
        edPhone.setText(address.phoneNumber)
        edStAddressOne.setText(address.lane1)
        edStAddressTwo.setText(address.lane2)
        edCity.setText(address.city)
        edState.setText(address.province)
        edLandMark.setText(address.landmark)
        edCountry.setText(address.country)
        edZipCode.setText(address.zipcode)
    }

    private fun checkSettings() {
        LocationHelper.getInstance().checkSettings { success, exception ->
            if (success) {
                getCurrentLocation()
            } else {
                exception?.startResolutionForResult(
                    this,
                    LocationHelper.LocationConstant.REQUEST_CHECK_SETTINGS
                )
            }
        }
    }

    private fun getCurrentLocation() {
        LocationHelper.getInstance().getMyLocation { location ->
            showLoader(R.string.please_wait)
            presenter?.getCurrentLocationAddress(this,location){ address->
                if(address != null){
                    hideLoader()
                    val stringBuilder: StringBuilder = StringBuilder()
                    for (i in 0 until address.maxAddressLineIndex + 1) {
                        stringBuilder.append(address.getAddressLine(i)).append("\n");
                    }
                    edStAddressOne.setText(stringBuilder.toString())
                    edLandMark.setText(address.thoroughfare?:Constant.EMPTY)
                    edCity.setText(address.locality)
                    edCountry.setText(address.countryName)
                    edState.setText(address.adminArea)
                    edZipCode.setText(address.postalCode)
                }
                else{
                    presenter?.getCurrentLocationAddress(location.latitude, location.longitude,false)
                }
            }
        }
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            PermissionHelper.RESULT_CODE_LOCATION
        )
    }

    override fun showProgressDialog() {
        showLoader(tradly.social.R.string.please_wait)
    }

    override fun hideProgressDialog() {
        hideLoader()
    }

    override fun hideProgressLoader() {

    }

    override fun showProgressLoader() {

    }

    override fun onLoadAddressList(list: List<ShippingAddress>) {

    }

    override fun onLoadCurrentLocationAddress(address: Address) {
        edStAddressOne.setText(address.locality)
        edCity.setText(address.city)
        edState.setText(address.state)
        edZipCode.setText(address.postalCode)
    }

    override fun showLocationFetchingError() {
        Toast.makeText(
            this,
            getString(tradly.social.R.string.address_cant_fetch_location),
            Toast.LENGTH_SHORT
        )
            .show()
    }

    override fun onSuccess(shippingAddress: ShippingAddress) {
        isAdded = true
        when (isFor) {
            AppConstant.ManageAction.EDIT -> {
                Toast.makeText(
                    this,
                    getString(tradly.social.R.string.address_address_updated_successfully),
                    Toast.LENGTH_SHORT
                ).show()
            }
            AppConstant.ManageAction.ADD -> {
                Toast.makeText(
                    this,
                    getString(tradly.social.R.string.address_address_added_successfully),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        finishActivity(shippingAddress)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PermissionHelper.RESULT_CODE_LOCATION) {
            if (grantResults.count() > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                checkSettings()
            } else {
                Utils.showSnackBarSettings(parentLayout, this)
            }
        }
    }

    override fun onFailure(appError: AppError) {

    }

    override fun noNetwork() {
        Toast.makeText(this, getString(tradly.social.R.string.no_internet), Toast.LENGTH_SHORT)
            .show()
    }

    override fun fieldError(id: Int, msg: Int) {
        when (id) {
            R.id.edName -> edName.error = getString(msg)
            R.id.edPhone -> edPhone.error = getString(msg)
            R.id.edStAddressOne -> edStAddressOne.error = getString(msg)
            R.id.edCity -> edCity.error = getString(msg)
            R.id.edState -> edState.error = getString(msg)
            R.id.edZipCode -> edZipCode.error = getString(msg)
        }
    }

    private fun finishActivity(shippingAddress: ShippingAddress? = null) {
        val intent = Intent()
        intent.putExtra("isAdded", isAdded)
        intent.putExtra("address",shippingAddress.toJson<ShippingAddress>())
        setResult(100, intent)
        finish()
    }

    override fun onBackPressed() {
        finishActivity()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == LocationHelper.LocationConstant.REQUEST_CHECK_SETTINGS && resultCode ==  Activity.RESULT_OK){
            getCurrentLocation()
        }
    }

}
