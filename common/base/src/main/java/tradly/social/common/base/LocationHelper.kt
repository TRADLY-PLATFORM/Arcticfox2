package tradly.social.common.base

import android.Manifest
import android.app.Activity
import android.content.IntentSender
import android.location.Location
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import java.lang.Exception

class LocationHelper {

    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private var locationSettingsRequest: LocationSettingsRequest? = null
    private var locationCallback: LocationCallback? = null
    private var settingsClient: SettingsClient? = null

    private val locationRequest: LocationRequest by lazy {
        LocationRequest().apply {
            interval = LocationConstant.UPDATE_INTERVAL_IN_MILLISECONDS
            fastestInterval = LocationConstant.FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }

    constructor() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(AppController.appContext)
        locationSettingsRequest = getLocationSettingsRequest()
        settingsClient = LocationServices.getSettingsClient(AppController.appContext)

    }

    object LocationConstant {
        const val UPDATE_INTERVAL_IN_MILLISECONDS: Long = 50
        const val FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS: Long = UPDATE_INTERVAL_IN_MILLISECONDS / 2
        const val REQUEST_CHECK_SETTINGS = 101
        const val DEFAULT_ZOOM:Float = 14f
        const val ZOOM_12F = 12f
        const val ZOOM_8F = 8f
        const val ZOOM_10F = 10f
    }

    companion object {
        @Volatile
        private var INSTANCE: LocationHelper? = null

        fun getInstance(): LocationHelper {
            return INSTANCE ?: synchronized(this) {
                LocationHelper().also { INSTANCE = it }
            }
        }
    }

    @SuppressWarnings("MissingPermission")
    fun getMyLocation(callback: (location: Location) -> Unit) {
        fusedLocationProviderClient?.lastLocation?.addOnSuccessListener { location: Location? ->
            location?.let {
                callback(location)
            } ?: run {
                fusedLocationProviderClient?.requestLocationUpdates(locationRequest, object : LocationCallback() {
                    override fun onLocationResult(p0: LocationResult?) {
                        super.onLocationResult(p0)
                        var lastLocation = p0?.lastLocation
                        lastLocation?.let { callback(it) }
                        fusedLocationProviderClient?.removeLocationUpdates(locationCallback)

                    }
                }.also { locationCallback = it }, Looper.getMainLooper())
            }

        }
    }

    fun checkSettings(callback: (success: Boolean, exception: ResolvableApiException?) -> Unit) {
        settingsClient?.checkLocationSettings(locationSettingsRequest)
            ?.addOnSuccessListener { locationSettingsResponse: LocationSettingsResponse? ->
                callback(true, null)
            }?.addOnFailureListener { exception: Exception ->
            if (exception is ResolvableApiException) {
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    callback(false, exception)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }

    fun requestLocationPermission(activity: Activity){
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            PermissionHelper.RESULT_CODE_LOCATION
        )
    }

    fun requestLocationPermission(fragment: Fragment){
        fragment.requestPermissions(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            PermissionHelper.RESULT_CODE_LOCATION
        )
    }

    private fun getLocationSettingsRequest(): LocationSettingsRequest {
        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(locationRequest)
        return builder.build()
    }
}