package tradly.social.ui.feedback

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.InputType
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.yalantis.ucrop.UCrop
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_feedback.*
import kotlinx.android.synthetic.main.activity_feedback.categoryTypeSpinner
import kotlinx.android.synthetic.main.toolbar.*
import tradly.social.R
import tradly.social.adapter.ListingAdapter
import tradly.social.common.base.*
import tradly.social.domain.entities.AppError
import tradly.social.domain.entities.Category
import tradly.social.domain.entities.Constant
import tradly.social.domain.entities.FileInfo
import tradly.social.common.base.BaseActivity

class FeedbackActivity : BaseActivity(),FeedbackPresenter.View {
    private var attachmentList = mutableListOf<FileInfo>()
    private var selectedCategoryName:String = Constant.EMPTY
    private lateinit var listingAdapter: ListingAdapter
    private lateinit var presenter: FeedbackPresenter
    private var selectedMood = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feedback)
        setToolbar(toolbar,title = R.string.feedback_header_title,backNavIcon = R.drawable.ic_back)
        presenter = FeedbackPresenter(this)
        initView()
        setAdapter()
    }

    fun initView(){
        setAttachmentVisibility()
        presenter.loadCategories()
        feedback_category_spinner_layout?.setOnClickListener {
            categoryTypeSpinner?.performClick()
        }

        when(AppConfigHelper.getConfigKey<Int>(
            AppConfigHelper.Keys.FEEDBACK_CONTACT_FIELD,
            AppConstant.FeedbackEmailContactField.EMAIL)){
            AppConstant.FeedbackEmailContactField.EMAIL->{
                ed_feedback_email.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                ed_feedback_email.hint = getString(R.string.feedback_email)
            }
            AppConstant.FeedbackEmailContactField.PHONE->{
                ed_feedback_email.inputType = InputType.TYPE_CLASS_PHONE
                ed_feedback_email.hint = getString(R.string.feedback_mobileno)
            }
            AppConstant.FeedbackEmailContactField.EMAIL_PHONE->{
                ed_feedback_email.inputType = InputType.TYPE_CLASS_TEXT
                ed_feedback_email.hint = getString(R.string.feedback_email_mobileNo)
            }
        }
    }

    private fun setAttachmentVisibility(){
        if (attachmentList.isEmpty()) {
            feedback_attachment_title?.visibility = View.GONE
            attachment_recycler_view?.visibility = View.GONE
        } else {
            feedback_attachment_title?.text = if (attachmentList.size > 1) String.format(
                getString(R.string.feedback_attachments),
                attachmentList.size.toString()
            ) else getString(R.string.feedback_attachment)
            feedback_attachment_title?.visibility = View.VISIBLE
            attachment_recycler_view?.visibility = View.VISIBLE
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.findItem(R.id.action_attachment)?.isVisible = AppController.appController.getUser()!=null
        menu?.findItem(R.id.action_send)?.isVisible = true
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_attachment) {
            if(attachmentList.size < 4){
                openAttachment()
            }
            else{
                showToast(R.string.feedback_max_attachment_message)
            }
        } else if (item.itemId == R.id.action_send) {
            sendFeedback()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun openAttachment() {
        if (PermissionHelper.checkPermission(this, PermissionHelper.Permission.READ_PERMISSION)) {
            FileHelper.openAllAttachment(this)
        }
        else{
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PermissionHelper.RESULT_CODE_READ_STORAGE)
        }
    }

    private fun sendFeedback(){
        presenter.sendFeedback(
            selectedCategoryName,
            selectedMood,
            ed_feedback_email.getString(),
            ed_feedback_title.getString(),
            ed_feedback_additional_info.getString(),
            attachmentList
        )
    }
    private fun setAdapter() {
        listingAdapter = ListingAdapter(this, attachmentList, AppConstant.ListingType.ATTACHMENT_LIST, attachment_recycler_view) { position, obj ->
            attachmentList.removeAt(position)
            listingAdapter.notifyDataSetChanged()
            setAttachmentVisibility()
        }
        attachment_recycler_view?.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        attachment_recycler_view?.adapter = listingAdapter
    }

    fun onClickEmoji(view: View) {
        val resetValue = Utils.getPixel(this, 0)
        mood1?.borderWidth =resetValue
        mood2?.borderWidth = resetValue
        mood3?.borderWidth = resetValue
        mood4?.borderWidth = resetValue
        mood5?.borderWidth = resetValue
        (view as? CircleImageView)?.borderWidth = Utils.getPixel(this, 3)
        when(view.id){
            R.id.mood1->selectedMood = 1
            R.id.mood2->selectedMood = 2
            R.id.mood3->selectedMood = 3
            R.id.mood4->selectedMood = 4
            R.id.mood5->selectedMood = 5
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(Activity.RESULT_OK==resultCode){
            if(requestCode == FileHelper.RESULT_OPEN_ALL_DOC){
                data?.data?.let {uri->
                    val mimeType = AppController.appContext.contentResolver.getType(uri)
                    val tempFile = FileHelper.createTempFile(FileHelper().getExtnFromMimeType(mimeType))
                    FileHelper().copy(uri,tempFile.absolutePath){
                        if(it){
                            if(FileHelper().getFileType(mimeType) == "image"){
                                val fileUri = FileProvider.getUriForFile(this, packageName + ".fileProvider", tempFile)
                                FileHelper.cropImage(fileUri, this, false, false,true)
                            }
                            else{
                                FileInfo(tempFile.absolutePath).also { attachmentList.add(it) }
                                listingAdapter.notifyDataSetChanged()
                                setAttachmentVisibility()
                            }
                        }
                    }
                }
            }
            else if(requestCode == UCrop.REQUEST_CROP && data != null && resultCode == Activity.RESULT_OK){
                val selectedImagePath = FileHelper.compressImageFile(this, FileHelper.tempFile.absolutePath)
                FileInfo(selectedImagePath).also { attachmentList.add(it) }
                listingAdapter.notifyDataSetChanged()
                setAttachmentVisibility()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode== PermissionHelper.RESULT_CODE_READ_STORAGE){
            if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
                FileHelper.openAllAttachment(this)
            }
            else{
                showToast(R.string.app_permission_gallery)
            }
        }
    }


    override fun onFailure(appError: AppError) {
        ErrorHandler.handleError(exception = appError)
    }

    override fun onSuccess() {
        showToast(R.string.feedback_sent_successfully)
        finish()
    }

    override fun showCategories(list: List<Category>) {
        if(list.isNotEmpty()){
            feedback_category_spinner_layout?.visibility = View.VISIBLE
            val categoryAdapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,list.map { it.name } )
            categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            categoryTypeSpinner?.adapter = categoryAdapter
            categoryTypeSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View,
                    position: Int,
                    id: Long
                ) {
                    txtCategoryType?.text = list[position].name.also { selectedCategoryName = it }
                }

                override fun onNothingSelected(parent: AdapterView<*>) {

                }
            }
            categoryTypeSpinner?.setSelection(0)
        }
    }

    override fun showProgressDialog(msg: Int, title: Int) {
       showLoader(msg)
    }

    override fun changeProgressMessage(msg: Int, title: Int) {
      changeLoaderMessage(msg,title)
    }

    override fun hideProgressDialog() {
       hideLoader()
    }

    override fun onMediaUploadFailed(msg: Int) {
        showToast(msg)
    }

    override fun showFieldError(id: Int, msg: Any) {
        if(id == R.id.feedback_title){
            feedback_title?.error = getString(msg as Int)
        }
    }
}