package tradly.social.ui.group.createGroup

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.GridLayoutManager
import tradly.social.R
import tradly.social.adapter.GroupTypeGridAdapter
import tradly.social.common.*
import tradly.social.domain.entities.Address
import tradly.social.domain.entities.AppError
import tradly.social.domain.entities.Constant
import tradly.social.domain.entities.GroupType
import tradly.social.common.base.BaseActivity
import tradly.social.ui.map.MapActivity
import tradly.social.ui.product.addProduct.AddProductActivity
import tradly.social.ui.tags.TagActivity
import com.google.android.material.chip.Chip
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.yalantis.ucrop.UCrop
import kotlinx.android.synthetic.main.activity_create_group.*
import kotlinx.android.synthetic.main.activity_create_group.addFab
import kotlinx.android.synthetic.main.activity_create_group.cancelImg
import kotlinx.android.synthetic.main.activity_create_group.selectedImage
import kotlinx.android.synthetic.main.activity_create_group.txtAddPhoto
import kotlinx.android.synthetic.main.toolbar.*
import tradly.social.common.base.*
import tradly.social.common.util.parser.extension.toObject
import java.io.File

class CreateGroupActivity : BaseActivity(),CreateGroupDetailPresenter.View{

    private var groupTypeGridAdapter:GroupTypeGridAdapter?=null
    private var createGroupDetailPresenter: CreateGroupDetailPresenter? = null
    private var tagList = mutableListOf<String>()
    private var groupTypeList = mutableListOf<GroupType>()
    private var selectedImagePath:String?=null
    private var address:Address?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_group)
        setToolbar(toolbar, R.string.group_header_title, R.drawable.ic_back)
        createGroupDetailPresenter = CreateGroupDetailPresenter(this)
        recycler_view?.layoutManager = GridLayoutManager(this,3)
        recycler_view?.addItemDecoration(SpaceGrid(3, 25, true))
        groupTypeGridAdapter = GroupTypeGridAdapter(this,groupTypeList)
        recycler_view?.adapter = groupTypeGridAdapter
        addTagIcon.setOnClickListener {
            val intent = Intent(this, TagActivity::class.java)
            intent.putExtra("list", Gson().toJson(tagList))
            startActivityForResult(intent, AddProductActivity.REQUEST_CODE.TAG_RESULT)
        }

        addFab?.setOnClickListener {
            showMediaChooser()
        }

        btnCreateGroup?.setOnClickListener {
            createGroupDetailPresenter?.addGroup(
                edGroupName.text.toString().trim(),
                edGroupDesc.text.toString().trim(),
                address,
                groupTypeGridAdapter?.getSelectedType(),
                selectedImagePath)
        }

        cancelImg?.setOnClickListener {
            selectedImagePath = Constant.EMPTY
            selectedImage?.visibility = View.GONE
            addFab?.visibility = View.VISIBLE
            txtAddPhoto?.visibility = View.VISIBLE
            cancelImg?.visibility = View.GONE
        }

        iconLocation.setOnClickListener {
            if (PermissionHelper.checkPermission(this, PermissionHelper.Permission.LOCATION)) {
                checkSettings()
            } else {
                LocationHelper.getInstance().requestLocationPermission(this)
            }
        }

        createGroupDetailPresenter?.getGroupTypes()

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AddProductActivity.REQUEST_CODE.TAG_RESULT && resultCode == Activity.RESULT_OK && data != null) {
            addTags(data.getStringExtra("list"))
        }
        if (requestCode == FileHelper.RESULT_OPEN_GALLERY && resultCode == Activity.RESULT_OK && data != null) {
            val file = FileHelper.createTempFile()
            FileHelper().copy(data.data, file.absolutePath) {
                if (it) {
                    val uri = FileProvider.getUriForFile(this, getPackageName() + ".fileProvider", file)
                    FileHelper.cropImage(uri, this, true, false)
                }
            }
        } else if (requestCode == FileHelper.RESULT_OPEN_CAMERA && resultCode == Activity.RESULT_OK) {
            FileHelper().addImageToGallery(FileHelper.tempFile)
            val uri = FileProvider.getUriForFile(this, getPackageName() + ".fileProvider", FileHelper.tempFile)
            FileHelper.cropImage(uri, this, true, false)
        } else if (requestCode == UCrop.REQUEST_CROP && data != null && resultCode == Activity.RESULT_OK) {
            selectedImage?.visibility = View.VISIBLE
            addFab?.visibility = View.GONE
            txtAddPhoto?.visibility = View.GONE
            cancelImg?.visibility = View.VISIBLE
            selectedImagePath = FileHelper.compressImageFile(this, FileHelper.tempFile.absolutePath)
            ImageHelper.getInstance().showImage(this, Uri.fromFile(File(selectedImagePath)),selectedImage,R.drawable.placeholder_image,R.drawable.placeholder_image)
        }
        else if(requestCode == LocationHelper.LocationConstant.REQUEST_CHECK_SETTINGS && resultCode == Activity.RESULT_OK){
            startMapActivity()
        }
        else if (requestCode == MapActivity.RESULT_CODE && resultCode == Activity.RESULT_OK && data != null) {
            val address = data.getStringExtra("address").toObject<Address>()
            onLoadAddress(address)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PermissionHelper.RESULT_CODE_READ_STORAGE) {
            if (grantResults.count() > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                FileHelper.openGallery(this)
            }
            else{
               showToast(R.string.app_permission_gallery)
            }
        }
        else if (requestCode == PermissionHelper.RESULT_CODE_CAMERA) {
            if (grantResults.count() > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if(Build.VERSION.SDK_INT<Build.VERSION_CODES.Q){
                    if(grantResults[1] != PackageManager.PERMISSION_GRANTED){
                       showToast(R.string.app_permission_camera)
                        return
                    }
                }
                FileHelper.openCamera(this)
            }
            else{
                showToast(R.string.app_permission_camera)
            }
        }
        else if (requestCode == PermissionHelper.RESULT_CODE_LOCATION) {
            if (grantResults.count() > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                checkSettings()
            } else { showToast(R.string.app_need_location_permission)}
        }
    }
    override fun onSuccess() {
       showToast(R.string.group_added_successfully)
    }

    override fun onFailure(appError: AppError) {
        ErrorHandler.handleError(this,appError)
    }

    override fun groupTypeLoadFailed() {
        showToast(R.string.group_could_not_load_group)
    }
    override fun onGroupTypeLoaded(list: List<GroupType>) {
        groupTypeList.clear()
        groupTypeList.addAll(list)
        if(groupTypeList.count()>0){
            groupTypeHint?.visibility = View.VISIBLE
            groupTypeList[0].isSelected = true
        }
        groupTypeGridAdapter?.notifyDataSetChanged()
    }

    override fun showProgressDialog() {
        showLoader(R.string.please_wait)
    }

    override fun hideProgressDialog() {
        hideLoader()
    }

    private fun addTags(listString: String?) {
        val baseType = object : TypeToken<List<String>>() {
        }.type
        tagList.clear()
        tagList.addAll(Gson().fromJson<List<String>>(listString, baseType))
        chipGroup.removeAllViews()
        for (item in tagList) {
            val chip = Chip(this)
            chip.isCheckable = false
            chip.isCloseIconVisible = false
            chip.layoutDirection = View.LAYOUT_DIRECTION_LOCALE
            chip.isCloseIconEnabled = false
            chip.chipBackgroundColor = resources.getColorStateList(R.color.colorWhiteLight)
            chip.setTextColor(resources.getColorStateList(R.color.colorBlueLight))
            chip.text = item
            chipGroup.addView(chip)
        }
        if (tagList.count() > 0) {
            hashDivider?.visibility = View.VISIBLE
        } else {
            hashDivider?.visibility = View.GONE
        }
    }

    fun onLoadAddress(address: Address?){
        txtAddress.text = address?.formattedAddress
        this.address = address
    }

    private fun showMediaChooser(){
        val dialog = BottomChooserDialog()
        dialog.setListener(object : DialogListener {
            override fun onClick(any: Any){
                when(any as? Int){
                    BottomChooserDialog.Type.CAMERA->{
                        if(PermissionHelper.checkPermission(this@CreateGroupActivity, PermissionHelper.Permission.CAMERA)){
                            FileHelper.openCamera(this@CreateGroupActivity)
                        }
                        else{
                            ActivityCompat.requestPermissions(
                                this@CreateGroupActivity,
                                if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.Q)arrayOf(Manifest.permission.CAMERA) else arrayOf(
                                    Manifest.permission.CAMERA,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE),
                                PermissionHelper.RESULT_CODE_CAMERA
                            )
                        }
                    }
                    BottomChooserDialog.Type.GALLERY->{ if(PermissionHelper.checkPermission(this@CreateGroupActivity,
                            PermissionHelper.Permission.READ_PERMISSION)){
                        FileHelper.openGallery(this@CreateGroupActivity)
                    }
                    else{
                        ActivityCompat.requestPermissions(
                            this@CreateGroupActivity,
                            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                            PermissionHelper.RESULT_CODE_READ_STORAGE
                        )
                    }}
                }
            }
        })
        dialog.show(supportFragmentManager, AppConstant.FragmentTag.BOTTOM_CHOOSER_FRAGMENT)
    }

    private fun checkSettings() {
        LocationHelper.getInstance().checkSettings { success, exception ->
            if (success) {
                startMapActivity()
            } else {
                exception?.startResolutionForResult(
                    this,
                    LocationHelper.LocationConstant.REQUEST_CHECK_SETTINGS
                )
            }
        }
    }

    private fun startMapActivity() {
        val intent = Intent(this, MapActivity::class.java)
        startActivityForResult(intent, MapActivity.RESULT_CODE)
    }

    override fun fieldError(id: Int, msg: Int) {
        when(id){
            R.id.edGroupName-> edGroupName.error = getString(msg)
            R.id.edGroupDesc-> edGroupDesc.error = getString(msg)
            R.id.recycler_view->Toast.makeText(this,getString(R.string.group_select_type),Toast.LENGTH_SHORT).show()
            R.id.selectedImage->Toast.makeText(this,getString(R.string.group_select_cover_photo),Toast.LENGTH_SHORT).show()
        }
    }
}

