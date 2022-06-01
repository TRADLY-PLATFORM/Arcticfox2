package tradly.social.common.base

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.fragment.app.Fragment

class PermissionHelper {
    enum class Permission {
        LOCATION,
        READ_PERMISSION,
        CAMERA
    }

    object RequestCode{
        const val LOCATION = 100
    }

    companion object {
        const val RESULT_CODE_LOCATION = 100
        const val RESULT_CODE_READ_STORAGE = 101
        const val RESULT_CODE_CAMERA = 103
        const val RESULT_CODE_READ_STORAGE_DOC = 104
        const val RESULT_CODE_CAMERA_FILE_UPLOAD = 105
        fun checkPermission(context: Context, permission: Permission): Boolean {
            if(Build.VERSION.SDK_INT<Build.VERSION_CODES.M){ return true}
            return when (permission) {
                Permission.LOCATION -> checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                                       checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

                Permission.READ_PERMISSION -> checkSelfPermission(context,Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

                Permission.CAMERA ->{
                    if(Build.VERSION.SDK_INT<Build.VERSION_CODES.Q) {
                        checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                    }
                    else{
                    checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                    }
                }
            }
        }

        fun requestPermission(fragment:Fragment, permission: Permission, requestCode:Int){
            when(permission){
                Permission.LOCATION -> fragment.requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION),requestCode)
            }
        }
    }

}