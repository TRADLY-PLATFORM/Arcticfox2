package tradly.social.ui.message.thread

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Paint
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import tradly.social.R
import tradly.social.adapter.ChatThreadAdapter
import tradly.social.common.*
import tradly.social.domain.entities.*
import tradly.social.common.base.BaseActivity
import com.yalantis.ucrop.UCrop
import kotlinx.android.synthetic.main.activity_chat_thread.*
import kotlinx.android.synthetic.main.activity_chat_thread.productTitle
import kotlinx.android.synthetic.main.activity_chat_thread.progress_circular
import kotlinx.android.synthetic.main.activity_chat_thread.toolbar
import kotlinx.android.synthetic.main.activity_chat_thread.txtActualPrice
import kotlinx.android.synthetic.main.activity_chat_thread.txtOffer
import kotlinx.android.synthetic.main.activity_chat_thread.txtPrice
import tradly.social.adapter.ChatThreadListener
import tradly.social.common.base.*
import tradly.social.common.network.converters.ProductConverter
import tradly.social.common.util.parser.extension.toJson
import tradly.social.common.util.parser.extension.toObject
import tradly.social.ui.product.addProduct.productDetail.ProductDetailActivity
import java.io.File

class ChatThreadActivity : BaseActivity(),ChatThreadPresenter.View,ChatThreadListener {

    var chatRoom:ChatRoom? = null
    private var product:Product?=null
    lateinit var provider:User
    private var chatThreadPresenter:ChatThreadPresenter?= null
    private var chatThreadAdapter:ChatThreadAdapter?=null
    private var isFromNotification = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_thread)
        setSupportActionBar(toolbar)
        chatThreadPresenter = ChatThreadPresenter(this)
        ImageHelper.getInstance().showImage(this,null,chatProfile,R.drawable.ic_user_placeholder,R.drawable.ic_user_placeholder)
        isFromNotification = intent.getBooleanExtra(AppConstant.BundleProperty.IS_FROM_NOTIFICATION,false)
        if(!isFromNotification){
            chatRoom = intent.getStringExtra("chatRoom").toObject<ChatRoom>()
            initViews(chatRoom)
            fetchChatThreads()
            setListener()
        }
        else{
            progressBar?.visibility = View.VISIBLE
            updateStoreOwnerInfo(getStringData(NotificationHelper.PayloadKey.SENDER_NAME))
            AppController.appController.getUser()?.let { user->
                MessageHelper.getChatRoomById(user.id,getStringData(NotificationHelper.PayloadKey.CHAT_ROOM_ID),object :MessageHelper.Companion.ChatListener{
                    override fun onSuccess(any: Any?) {
                        progressBar?.visibility = View.GONE
                        (any as? ChatRoom)?.let {
                            initViews(it)
                            fetchChatThreads()
                            setListener()
                        }
                    }
                    override fun onFailure(message: String?,code:Int) {
                        progressBar?.visibility = View.GONE
                    }
                })
            }
        }
    }

    private fun initViews(chatRoom: ChatRoom?){
        this.chatRoom = chatRoom
        provider = getProvider(chatRoom)
        threadLayout?.visibility = View.VISIBLE
        chatBoxLayout?.visibility = View.VISIBLE
        updateStoreOwnerInfo(provider.name)
        intent?.let {
            ImageHelper.getInstance().showImage(this,provider.profilePic,chatProfile,R.drawable.ic_user_placeholder,R.drawable.ic_user_placeholder)
            if(intent.hasExtra("product") && !isFromNotification){
                product = intent.getStringExtra("product").toObject<Product>()
                product?.let {
                    topView.visibility = View.VISIBLE
                    productTitle.text = it.title
                    txtActualPrice.text = it.listPrice?.displayCurrency
                    txtActualPrice.visibility = if(AppConfigHelper.getConfigKey(AppConfigHelper.Keys.HIDE_OFFER_PERCENT)) View.GONE else View.VISIBLE
                    txtActualPrice.paintFlags = txtActualPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    txtOffer.text= String.format(getString(R.string.off_data),it.offerPercent.toString())
                    txtPrice.text = it.offerPrice?.displayCurrency
                    ImageHelper.getInstance().showImage(this,if(it.images.isNotEmpty())it.images[0] else null,productImg,R.drawable.placeholder_image,R.drawable.placeholder_image)
                }
            }
            else{
                topView.visibility = View.GONE
            }
        }
    }

    private fun fetchChatThreads(){
        recycler_view?.layoutManager = LinearLayoutManager(this,LinearLayoutManager.VERTICAL,true)
        val query = MessageHelper.getChatThreadQuery(chatRoom?.id)
        chatThreadAdapter = ChatThreadAdapter(this,chatRoom?.id,provider,query,null,null,recycler_view)
        chatThreadAdapter?.setChatListener(this)
        recycler_view.adapter = chatThreadAdapter
    }

    private fun setListener(){
        attachImg?.setOnClickListener {
            showMediaChooser()
        }
        backNav?.setOnClickListener {
            finish()
        }

        fabSend?.setOnClickListener {
            val msg = edChat.text?.toString()?.trim()
            if(!msg.isNullOrEmpty()){
                sendChat(msg,MessageHelper.Companion.Type.MSG)
                edChat.setText(Constant.EMPTY)
            }
        }

        actionBtn?.setOnClickListener {
            if(NetworkUtil.isConnectingToInternet()){
                product?.apply {
                    val map = hashMapOf<String,Any?>()
                    map["id"] = this.id.toInt()
                    map["title"] = this.title
                    map["images"] = this.images
                    map["offer_price"] = ProductConverter.mapFrom(this.offerPrice!!)
                    map["list_price"] = ProductConverter.mapFrom(this.listPrice!!)
                    map["offer_percent"] = this.offerPercent
                    sendChat(map.toJson<Map<String,Any?>>(),MessageHelper.Companion.Type.PRODUCT)
                    topView?.visibility = View.GONE
                }
            }
        }
    }

    private fun sendChat(txt:String,mimeType:String){
        chatRoom?.id?.let {
            chatThreadPresenter?.sendMsg(provider,it,txt,mimeType)
        }
    }
    override fun onSuccess(list: List<Chat>) {

    }

    override fun onFailure(appError: AppError) {

    }

    override fun showProgressLoader() {
        progress_circular?.visibility = View.VISIBLE
    }

    override fun hideProgressLoader() {
        progress_circular?.visibility = View.GONE
    }

    override fun noInternet() {

    }

    override fun showProgressDialog() {
        showLoader(R.string.please_wait)
    }

    override fun hideProgressDialog() {
        hideLoader()
    }

    private fun getProvider(chatRoom: ChatRoom?) = User(
        name = chatRoom?.receiver.getValue(),
        id = chatRoom?.receiverId?:Constant.EMPTY,
        profilePic = chatRoom?.profilePic.getValue()
    )

    private fun updateStoreOwnerInfo(storeName: String?) {
        chatUserNameTxt?.text = storeName
    }

    private fun showMediaChooser(){
        val dialog = BottomChooserDialog()
        val bundle = Bundle()
        bundle.putBoolean("hasDoc",false)
        dialog.arguments = bundle
        dialog.setListener(object : DialogListener {
            override fun onClick(any: Any){
                when(any as? Int){
                    BottomChooserDialog.Type.CAMERA->{
                        if(PermissionHelper.checkPermission(this@ChatThreadActivity, PermissionHelper.Permission.CAMERA)){
                            FileHelper.openCamera(this@ChatThreadActivity)
                        }
                        else{
                            ActivityCompat.requestPermissions(
                                this@ChatThreadActivity,
                                if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.Q)arrayOf(Manifest.permission.CAMERA) else arrayOf(
                                    Manifest.permission.CAMERA,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE),
                                PermissionHelper.RESULT_CODE_CAMERA
                            )
                        }
                    }
                    BottomChooserDialog.Type.GALLERY,
                    BottomChooserDialog.Type.DOCUMENT->{
                        val selectedItem = any as? Int
                        if(PermissionHelper.checkPermission(this@ChatThreadActivity,
                            PermissionHelper.Permission.READ_PERMISSION)){
                        if((any as? Int) == BottomChooserDialog.Type.DOCUMENT){
                            FileHelper.openAttachment(this@ChatThreadActivity)
                            return
                        }
                            FileHelper.openPhotoVideo(this@ChatThreadActivity)
                    }
                    else{
                        ActivityCompat.requestPermissions(
                            this@ChatThreadActivity,
                            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                            if(selectedItem == BottomChooserDialog.Type.GALLERY) PermissionHelper.RESULT_CODE_READ_STORAGE else PermissionHelper.RESULT_CODE_READ_STORAGE_DOC
                        )
                    }}
                }
            }
        })
        dialog.show(supportFragmentManager, AppConstant.FragmentTag.BOTTOM_CHOOSER_FRAGMENT)
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PermissionHelper.RESULT_CODE_READ_STORAGE) {
            if (grantResults.count() > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                FileHelper.openPhotoVideo(this)
            }
            else{
                Toast.makeText(this, getString(R.string.app_permission_gallery), Toast.LENGTH_SHORT).show()
            }
        }
        else if (requestCode == PermissionHelper.RESULT_CODE_CAMERA) {
            if (grantResults.count() > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if(Build.VERSION.SDK_INT<Build.VERSION_CODES.Q){
                    if(grantResults[1] != PackageManager.PERMISSION_GRANTED){
                        Toast.makeText(this, getString(R.string.app_permission_camera), Toast.LENGTH_SHORT).show()
                        return
                    }
                }
                FileHelper.openCamera(this)
            }
            else{
                Toast.makeText(this, getString(R.string.app_permission_camera), Toast.LENGTH_SHORT).show()
            }
        }
        if (requestCode == PermissionHelper.RESULT_CODE_READ_STORAGE_DOC) {
            if (grantResults.count() > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                FileHelper.openAttachment(this)
            }
            else{
                Toast.makeText(this, getString(R.string.app_permission_gallery), Toast.LENGTH_SHORT).show()
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == FileHelper.RESULT_OPEN_GALLERY && resultCode == Activity.RESULT_OK && data != null){
            data.data?.let {
                val mimeType = AppController.appContext.contentResolver.getType(it)
                if(FileHelper().getFileType(mimeType) == "video"){
                    showProgressDialog()
                    val file = FileHelper.createTempFile(FileHelper().getExtnFromMimeType(mimeType))
                    FileHelper().copy(it,file.absolutePath){
                        if(it){
                            chatRoom?.id?.let {
                                chatThreadPresenter?.uploadMedia(FileInfo(fileUri = file.absolutePath,type = FileHelper.getMimeType(file.absolutePath),name = file.name),provider,it)

                            }
                        }
                    }
                }
                else{
                    val file = FileHelper.createTempFile()
                    FileHelper().copy(data.data,file.absolutePath){
                        if(it){
                            val uri = FileProvider.getUriForFile(this,getPackageName() + ".fileProvider",file)
                            FileHelper.cropImage(uri,this,false,false)
                        }
                    }
                }
            }
        }
        else if(requestCode == FileHelper.RESULT_OPEN_CAMERA && resultCode == Activity.RESULT_OK){
            FileHelper().addImageToGallery(FileHelper.tempFile)
            val uri = FileProvider.getUriForFile(this,getPackageName() + ".fileProvider",
                FileHelper.tempFile)
            FileHelper.cropImage(uri,this,false,false)
        }
        else if(requestCode == UCrop.REQUEST_CROP && data != null && resultCode == Activity.RESULT_OK){
            showProgressDialog()
            val filePath = FileHelper.compressImageFile(this, FileHelper.tempFile.absolutePath)
            chatRoom?.id?.let {
                chatThreadPresenter?.uploadMedia(FileInfo(fileUri = filePath,type = FileHelper.getMimeType(filePath),name = File(filePath).name),provider,it)

            }
            }
        if(requestCode == FileHelper.RESULT_OPEN_DOC && resultCode == Activity.RESULT_OK && data != null){
            showProgressDialog()
            data.data?.let {
                val mimeType = AppController.appContext.contentResolver.getType(it)
                val file = FileHelper.createTempFile(FileHelper().getExtnFromMimeType(mimeType))
                FileHelper().copy(data.data,file.absolutePath){
                    if(it){
                        chatRoom?.id?.let {
                            chatThreadPresenter?.uploadMedia(FileInfo(fileUri = file.absolutePath,type = FileHelper.getMimeType(file.absolutePath),name = file.name),provider,it)

                        }
                    }
                    else{
                        hideProgressDialog()
                        Toast.makeText(this,getString(R.string.media_failed_msg),Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        FileHelper().deleteFiles()
        val user = AppController.appController.getUser()
        if(chatRoom!=null && user!=null){
            chatThreadAdapter?.detachListener(MessageHelper.getChatThreadRefererence(chatRoom!!.id))
            if(chatRoom!!.count!=0){
                MessageHelper.updateChatRoom(chatRoom!!.updatedBy,chatRoom!!.receiverId,user.id,chatRoom!!.id)
            }
        }
    }

    override fun onClickAccept(messageId:String,productId: String,makeOffer: MakeOffer) {
        chatThreadPresenter?.updateNegotiation(true,messageId,productId,makeOffer, AppConstant.NegotiationStatus.ACCEPTED)
    }

    override fun onClickDecline(messageId:String,productId: String,makeOffer: MakeOffer) {
        chatThreadPresenter?.updateNegotiation(false,messageId,productId,makeOffer, AppConstant.NegotiationStatus.REJECTED)
    }

    override fun onUpdateNegotiation(isAccepted:Boolean,messageId: String, makeOffer: MakeOffer) {
        MessageHelper.updateNegotiation(isAccepted,AppController.appController.getUser()!!,provider,provider.name,chatRoom?.id!!,messageId,makeOffer)
    }

    override fun onclickBuy(productId:String) {
        val intent = Intent(this, ProductDetailActivity::class.java)
        intent.putExtra("productId",productId)
        startActivity(intent)
    }
}
