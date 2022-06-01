package tradly.social.ui.map

import android.app.Activity
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.view.View
import android.widget.Toast
import tradly.social.R
import tradly.social.common.base.LocationHelper
import tradly.social.common.base.ErrorHandler
import tradly.social.domain.entities.Address
import tradly.social.domain.entities.AppError
import tradly.social.domain.entities.GeoPoint
import tradly.social.common.base.BaseActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.activity_map.*
import tradly.social.common.util.parser.extension.toJson

class MapActivity : BaseActivity(), MapPresenter.View, OnMapReadyCallback {

    companion object { const val RESULT_CODE = 100 }
    private var address: Address? = null
    private var isInProgress: Boolean = false
    private var mGoogleMap: GoogleMap? = null
    private var selectedLatLng : LatLng?=null
    private lateinit var mapPresenter: MapPresenter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        mapView?.onCreate(savedInstanceState)
        mapView?.onResume()
        mapView?.getMapAsync(this)
        mapPresenter = MapPresenter(this)
        btnCurrentLocation.setOnClickListener { showMyLocation() }
        btnSelectLocation?.setOnClickListener {
            address?.let {
                finishActivityWithResult(it)
            }?:run{
                selectedLatLng?.let {
                    showSearchBottomView()
                    mapPresenter.getAddress(GeoPoint(it.latitude,it.longitude))
                }
            }
        }

        actionChange?.setOnClickListener {
            isInProgress = false
            bottomView?.visibility = View.GONE
            btnText?.setText(R.string.map_select_location)
            btnSelectLocation?.visibility = View.VISIBLE
            address = null
        }

        imgBack?.setOnClickListener{
            finish()
        }
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        mGoogleMap = googleMap
        googleMap?.uiSettings?.isMyLocationButtonEnabled = false
        googleMap?.uiSettings?.isScrollGesturesEnabled = true
        googleMap?.uiSettings?.isZoomGesturesEnabled = true
        googleMap?.uiSettings?.isZoomControlsEnabled = false
        googleMap?.uiSettings?.isMapToolbarEnabled = false
        googleMap?.isIndoorEnabled = false
        googleMap?.uiSettings?.isCompassEnabled = false
        googleMap?.isMyLocationEnabled = false
        showMyLocation()
        googleMap?.setOnCameraMoveStartedListener {
            if (it == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
                // get maps center position coordinates
                selectedLatLng = googleMap.cameraPosition.target
            }
        }

        /*try {
            googleMap?.let {
                val success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                        this,
                        R.raw.map_style
                    )
                )
                if (!success) {
                    ErrorHandler.printLog("MAP_STYLE_NOT_APPLIED")
                }
            }
        } catch (ex: Resources.NotFoundException) {
            ErrorHandler.printLog(ex.message)
        }*/
    }

    private fun showMyLocation() {
        LocationHelper.getInstance().getMyLocation { location: Location ->
            moveCamera(LatLng(location.latitude, location.longitude))
        }
    }

    private fun moveCamera(latLng: LatLng?) {
        mGoogleMap?.let {
            latLng?.let {
                selectedLatLng = it
                val cameraUpdate =
                    CameraUpdateFactory.newLatLngZoom(
                        latLng,
                        LocationHelper.LocationConstant.DEFAULT_ZOOM
                    )
                mGoogleMap?.moveCamera(cameraUpdate)
                mGoogleMap?.animateCamera(cameraUpdate)
                mGoogleMap?.moveCamera(
                    CameraUpdateFactory.newCameraPosition(
                        CameraPosition.Builder()
                            .target(mGoogleMap?.cameraPosition?.target)
                            .zoom(LocationHelper.LocationConstant.DEFAULT_ZOOM)
                            //.bearing(30)
                            .build()
                    )
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView?.onDestroy()
    }

    override fun showAddress(address: Address) {
        this.address = address
        actionChange?.visibility = View.GONE
        showResultBottomView(address.formattedAddress)
    }

    override fun showProgressLoader() {
        isInProgress = true
        progress?.visibility = View.VISIBLE
        txtTitle?.setText(R.string.please_wait)
        actionChange?.visibility = View.GONE
    }

    override fun hideProgressLoader() {
        progress?.visibility = View.GONE
        isInProgress = false
    }

    override fun networkError(msg: Int) {
        Toast.makeText(this, getString(msg), Toast.LENGTH_SHORT).show()
    }

    override fun onFailure(appError: AppError) {
        ErrorHandler.handleError(this, appError)
    }

    override fun showAddresses(addressList: List<Address>) {

    }
    private fun showSearchBottomView(){
        btnSelectLocation?.visibility = View.GONE
        bottomView?.visibility = View.VISIBLE
        txtTitle?.text = getString(R.string.please_wait)
        progress.visibility = View.VISIBLE
        txtAddress?.visibility = View.GONE
        actionChange?.visibility = View.GONE
    }

    private fun showResultBottomView(result:String){
        btnSelectLocation?.visibility = View.VISIBLE
        btnText?.text = getString(R.string.done)
        bottomView?.visibility = View.VISIBLE
        txtTitle?.text = getString(R.string.map_location)
        progress.visibility = View.GONE
        txtAddress?.visibility = View.VISIBLE
        txtAddress?.text = result
        actionChange?.visibility = View.VISIBLE
    }

    private fun finishActivityWithResult(address: Address){
        val intent = Intent()
        intent.putExtra("address",address.toJson<Address>())
        setResult(Activity.RESULT_OK,intent)
        finish()
    }
}
