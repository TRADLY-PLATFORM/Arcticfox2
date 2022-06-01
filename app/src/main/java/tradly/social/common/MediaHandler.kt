package tradly.social.common

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.yalantis.ucrop.UCrop
import tradly.social.R
import tradly.social.common.base.FileHelper
import tradly.social.common.base.PermissionHelper

class MediaHandler(var view:View) {

    interface View{
        fun onMediaResult(filePath:String)
    }

    fun openCamera(fragment: Fragment){
        if (PermissionHelper.checkPermission(fragment.requireContext(), PermissionHelper.Permission.CAMERA)){
            FileHelper.openCamera(fragment)
        }
        else{
            fragment.requestPermissions(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) arrayOf(Manifest.permission.CAMERA) else arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE), PermissionHelper.RESULT_CODE_CAMERA)
        }
    }

    fun openGallery(fragment: Fragment){
        if (PermissionHelper.checkPermission(fragment.requireContext(), PermissionHelper.Permission.READ_PERMISSION)){
            FileHelper.openGallery(fragment)
        }
        else{
            fragment.requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PermissionHelper.RESULT_CODE_READ_STORAGE)
        }
    }

    fun onActivityResult(fragment:Fragment,requestCode: Int, resultCode: Int, data: Intent?) {
        val context = fragment.requireContext()
        if (requestCode == FileHelper.RESULT_OPEN_CAMERA && resultCode == Activity.RESULT_OK) {
            FileHelper().addImageToGallery(FileHelper.tempFile)
            val uri = FileProvider.getUriForFile(
                context,
                context.packageName + ".fileProvider",
                FileHelper.tempFile
            )
            FileHelper.cropImage(uri, fragment, false, false)
        }
        else if (requestCode == FileHelper.RESULT_OPEN_GALLERY && data != null && resultCode == Activity.RESULT_OK) {
            val file = FileHelper.createTempFile()
            FileHelper().copy(data.data, file.absolutePath) {
                if (it) {
                    val uri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileProvider", file)
                    FileHelper.cropImage(uri, fragment, false, false)
                }
            }
        }
        else if (requestCode == UCrop.REQUEST_CROP && data != null && resultCode == Activity.RESULT_OK){
            var selectedImagePath = FileHelper.compressImageFile(context, FileHelper.tempFile.absolutePath)
            view.onMediaResult(selectedImagePath)
        }
    }

    fun onRequestPermissionsResult(fragment: Fragment,requestCode: Int, permissions: Array<out String>, grantResults: IntArray){
        val context = fragment.requireContext()
        if (requestCode == PermissionHelper.RESULT_CODE_READ_STORAGE) {
            if (grantResults.count() > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                FileHelper.openGallery(fragment)
            } else {
                Toast.makeText(context, context.getString(R.string.app_permission_gallery), Toast.LENGTH_SHORT).show()
            }
        } else if (requestCode == PermissionHelper.RESULT_CODE_CAMERA) {
            if (grantResults.count() > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    if (grantResults[1] != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(context, context.getString(R.string.app_permission_camera), Toast.LENGTH_SHORT).show()
                        return
                    }
                }
                FileHelper.openCamera(fragment)
            } else {
                Toast.makeText(context, context.getString(R.string.app_permission_camera), Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }
}