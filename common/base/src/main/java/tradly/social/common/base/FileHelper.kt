package tradly.social.common.base

import android.annotation.TargetApi
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.text.TextUtils
import android.webkit.MimeTypeMap
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import tradly.social.domain.entities.Constant
import com.yalantis.ucrop.UCrop
import id.zelory.compressor.Compressor
import kotlinx.coroutines.*
import tradly.social.domain.entities.ImageFeed
import tradly.social.domain.entities.FileInfo
import java.io.*
import java.text.DecimalFormat
import java.util.*
import kotlin.coroutines.CoroutineContext


class FileHelper : CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    companion object {
        private var job: Job
        lateinit var tempFile: File
        const val RESULT_OPEN_GALLERY = 300
        const val RESULT_OPEN_CAMERA = 301
        const val RESULT_OPEN_DOC = 302
        const val RESULT_OPEN_ALL_DOC = 303
        const val RESULT_OPEN_CAMERA_FILE_UPLOAD = 304
        const val RESULT_OPEN_U_CROP_FILE_UPLOAD = 400
        private val decimalFormat = DecimalFormat("#.##")
        private val MB = 1024*1024
        private val KB = 1024
        init {
            job = Job()
        }

        private fun getDirectory(): String {
            if (isExternalStorageWritable()) {
                return getExternalFileDirectory()
            }
            return getInternalFileDirectory()
        }

        private fun getExternalFileDirectory(): String {
            return AppController.appContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString()
                .plus("/tradlyImages")
        }

        private fun getInternalFileDirectory(): String {
            return AppController.appContext.filesDir.absolutePath.plus("/tradlyImages")
        }

        private fun getExternalDirectory(): String {
            if (isExternalStorageWritable()) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                        .absolutePath.plus("/tradlyImages")
                }
                return getExternalFileDirectory()
            }
            return getInternalFileDirectory()
        }

        private fun isExternalStorageWritable(): Boolean {
            val state = Environment.getExternalStorageState()
            return (Environment.MEDIA_MOUNTED == state)
        }

        fun isExternalStorageReadable(): Boolean {
            return Environment.getExternalStorageState() in
                    setOf(Environment.MEDIA_MOUNTED, Environment.MEDIA_MOUNTED_READ_ONLY)
        }

        fun createTempFile(extension: String = Constant.EMPTY): File {
            val dir = getDirectory()
            val file = File(dir)
            if (!file.exists()) {
                file.mkdir()
            }
            return File(dir, getFileUniqueName(extension)).also { tempFile = it }
        }

        fun createCameraFile(): File {
            val dir = getExternalDirectory()
            val file = File(dir)
            if (!file.exists()) {
                file.mkdir()
            }
            return File(dir, getFileUniqueName()).also { tempFile = it }
        }

        private fun getFileUniqueName(extension: String = Constant.EMPTY): String {
            val random = Random()
            if (!extension.isNullOrEmpty()) {
                return "file_" + Calendar.getInstance().timeInMillis + (random.nextInt(999999) + 100000) + "." + extension
            }
            return "img_" + Calendar.getInstance().timeInMillis + (random.nextInt(999999) + 100000) + ".jpg"
        }

        fun cropImage(selectedImage: Uri?, activity: Activity, isCustomRatio: Boolean, compress: Boolean,showAllAvailableRatios:Boolean = false , requestCode: Int = 0) {
            selectedImage?.let {
                val destinationFile = createTempFile()
                val uCrop = UCrop.of(selectedImage, Uri.fromFile(destinationFile))
                val options = getUCropOption(activity, isCustomRatio, compress,showAllAvailableRatios)
                uCrop.withOptions(options).apply {
                    if(requestCode!=0){
                        start(activity,requestCode)
                    }
                    else{
                        start(activity)
                    }
                }
            }
        }

        fun cropImage(selectedImage: Uri?, fragment: Fragment, isCustomRatio: Boolean, compress: Boolean,showAllAvailableRatios:Boolean = false) {
            selectedImage?.let {
                val destinationFile = createTempFile()
                val uCrop = UCrop.of(selectedImage, Uri.fromFile(destinationFile))
                val options = getUCropOption(fragment.requireContext(), isCustomRatio, compress,showAllAvailableRatios)
                uCrop.withOptions(options).start(fragment.requireContext(),fragment)
            }
        }

        private fun getUCropOption(context: Context, isCustomRatio: Boolean, compress: Boolean ,showAllAvailableRatios:Boolean): UCrop.Options {
            val options = UCrop.Options()
            if(!showAllAvailableRatios){
                if (isCustomRatio) {
                    options.withAspectRatio(16f, 9f)
                } else {
                    options.withAspectRatio(1f, 1f)
                }
            }
            if (compress) {
                options.withMaxResultSize(400, 400)
            }
            val colorPrimary = ThemeUtil.getResourceValue(context,R.attr.colorPrimary)
            val colorPrimaryDark = ThemeUtil.getResourceValue(context,R.attr.colorPrimaryDark)
            options.setToolbarTitle(context.getString(R.string.edit_photo))
            options.setToolbarColor(ContextCompat.getColor(context,colorPrimary))
            options.setStatusBarColor(ContextCompat.getColor(context,colorPrimaryDark))
            options.setActiveWidgetColor(ContextCompat.getColor(context,colorPrimaryDark))
            return options

        }

        fun compressImageFile(context: Context, finalPath: String?): String {
            val compressedImage = Compressor.Builder(context)
                .setQuality(100)
                .setDestinationDirectoryPath(getDirectory())
                .setMaxHeight(1080f)
                .setMaxWidth(1350f)
                .setCompressFormat(Bitmap.CompressFormat.JPEG)
                .build()
                .compressToFile(File(finalPath))
            return compressedImage.absolutePath

        }


        private fun closeSilently(closeable: Closeable?) {
            if (closeable != null) {
                try {
                    closeable.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
        }


        fun openPhotoVideo(activity: Activity) {
            val i = Intent(Intent.ACTION_OPEN_DOCUMENT)
            i.setType("*/*")
            //val mimetypes = arrayOf("image/*", "video/*")
            val mimetypes = arrayOf("image/*")
            i.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes)
            activity.startActivityForResult(i, RESULT_OPEN_GALLERY)
        }

        fun openAttachment(activity: Activity) {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.setType("*/*")
            val mimetypes = arrayOf("application/*", "text/*")
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes)
            activity.startActivityForResult(intent, RESULT_OPEN_DOC)
        }

        fun openAllAttachment(activity: Activity) {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "*/*"
            activity.startActivityForResult(intent, RESULT_OPEN_ALL_DOC)
        }


        fun openGallery(activity: Activity) = getPhotoIntent { intent -> activity.startActivityForResult(intent, RESULT_OPEN_GALLERY) }

        fun openGallery(fragment: Fragment) = getPhotoIntent { intent -> fragment.startActivityForResult(intent, RESULT_OPEN_GALLERY) }

        fun openCamera(activity: Activity , requestCode:Int = RESULT_OPEN_CAMERA) = getCameraIntent { intent->  activity.startActivityForResult(intent, requestCode) }

        fun openCamera(fragment: Fragment) = getCameraIntent { intent-> fragment.startActivityForResult(intent, RESULT_OPEN_CAMERA) }

        private fun getPhotoIntent(callback: (result: Intent) -> Unit){
           Intent(Intent.ACTION_PICK).apply { type = "image/*" }.also { callback(it) }
        }

        private fun getCameraIntent(callback: (result: Intent) -> Unit){
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val file = createCameraFile()
            try{
                val contentUri = FileProvider.getUriForFile(AppController.appContext, AppController.appContext.getPackageName() + ".fileProvider", file)
                intent.putExtra(MediaStore.EXTRA_OUTPUT, contentUri)
                callback(intent)
            }catch (ex:ActivityNotFoundException){
                ActivityHelper.getCurrentActivityInstance()?.showToast(R.string.camera_app_not_found)
            }
        }

        fun getMimeType(filePath: String?): String {
            val extension = MimeTypeMap.getFileExtensionFromUrl(filePath)
            extension?.let {
                return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: Constant.EMPTY
            }
            return Constant.EMPTY
        }

        fun getFileSize(file:File):String{
            val length = file.length()
            return if(length> MB){
                decimalFormat.format(length/ MB)+"mb"
            }
            else if(length> KB){
                decimalFormat.format(length/ KB)+"kb"
            }
            else{
                decimalFormat.format(length)+"b"
            }
        }

        fun convertListToFileInfoList(list:List<ImageFeed>):List<FileInfo>{
            val mList = mutableListOf<FileInfo>()
            for(item in list){
                val filePath = item.filePath
                if(!filePath.startsWith("http")){
                    mList.add(FileInfo(fileUri = filePath, name = File(filePath).name,type = getMimeType(filePath)))
                }
            }
            return mList
        }
    }

    fun getFileImage(mimeType: String?): Int {
        var resource = R.drawable.unknown
        val fileType = FileHelper().getFileType(mimeType)
        when (fileType) {
            "video" -> resource = R.drawable.icon_video
            "image" -> resource = R.drawable.icon_img
            "audio" -> resource = R.drawable.icon_audio
            "application" -> resource = R.drawable.icon_document
            "text" -> resource = R.drawable.icon_txt
            else -> resource = R.drawable.icon_unknown
        }

        return resource
    }


    fun getFileType(mimeType: String?): String {
        if (!TextUtils.isEmpty(mimeType)) {
            var splitMime = mimeType?.split("/")
            splitMime?.let {
                if (it.size > 0) {
                    return it[0]
                }
            }
        }
        return Constant.EMPTY
    }

    fun getExtnFromMimeType(mimeType: String?): String {
        if (!TextUtils.isEmpty(mimeType)) {
           return MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)?:Constant.EMPTY
        }
        return Constant.EMPTY
    }

    fun copy(fileUri: Uri?, toPath: String, callback: (result: Boolean) -> Unit) {
        launch(Dispatchers.Main) {
            val call = async(Dispatchers.IO) { copyFile(fileUri, toPath) }
            callback(call.await())
        }
    }

    fun getByte(path: String?): ByteArray {
        var getBytes = byteArrayOf()
        try {
            val file = File(path)
            getBytes = ByteArray(file.length() as Int)
            val fis = FileInputStream(file)
            fis.read(getBytes)
            fis.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return getBytes
    }

    private suspend fun copyFile(fileUri: Uri?, toPath: String): Boolean {
        var inStream: FileInputStream? = null
        var outStream: FileOutputStream? = null
        val parcelFileDescriptor: ParcelFileDescriptor?
        try {
            fileUri?.let {
                parcelFileDescriptor = AppController.appContext.contentResolver.openFileDescriptor(fileUri, "r")
                if (parcelFileDescriptor != null) {
                    val fileDescriptor = parcelFileDescriptor.fileDescriptor
                    inStream = FileInputStream(fileDescriptor)
                    outStream = FileOutputStream(File(toPath))
                    val inChannel = inStream?.channel
                    val outChannel = outStream?.channel
                    inChannel?.transferTo(0, inChannel.size(), outChannel)
                    parcelFileDescriptor.close()
                }
            } ?: run { return false }
        } catch (e: Exception) {
            return false
        } finally {
            closeSilently(inStream)
            closeSilently(outStream)
        }
        return true
    }


    // It will clear all temp image files
    fun deleteFiles() {
        GlobalScope.launch(Dispatchers.IO) {
            val dir = getDirectory()
            val file = File(dir)
            if (file.isDirectory) {
                try {
                    file.listFiles()?.let {
                        it.forEach {
                            if (it != null && it.exists()) {
                                it.delete()
                            }
                        }
                    }
                } catch (ex: Exception) {
                    ErrorHandler.printLog(ex.message)
                }
            }
        }
    }

    fun addImageToGallery(file: File) {
        if (isExternalStorageWritable()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                insertImageToMediaStore(file)
            }
        }
    }

    @TargetApi(29)
    private fun insertImageToMediaStore(file: File) {
        try {
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, file.name)
                put(MediaStore.Images.Media.MIME_TYPE, getMimeType(file.absolutePath))
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
            val resolver = AppController.appContext.contentResolver
            val collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            val uri = resolver.insert(collection, values)
            uri?.let {
                resolver.openFileDescriptor(uri, "w", null).use {
                    val fileDescriptor = it?.fileDescriptor
                    val outStream = FileOutputStream(fileDescriptor)
                    val inStream = FileInputStream(file.absolutePath)
                    val inChannel = inStream.channel
                    val outChannel = outStream.channel
                    inChannel?.transferTo(0, inChannel.size(), outChannel)
                    it?.close()
                }
                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
            }

        } catch (ex: java.lang.Exception) {
            ErrorHandler.printLog(ex.message)
        }
    }

    fun getMimeType(filePath: String): String? {
        val extension = MimeTypeMap.getFileExtensionFromUrl(filePath)
        extension?.let {
            return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: Constant.EMPTY
        }
        return Constant.EMPTY
    }
}