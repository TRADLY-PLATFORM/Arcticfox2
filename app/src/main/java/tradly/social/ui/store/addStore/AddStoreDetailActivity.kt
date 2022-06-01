package tradly.social.ui.store.addStore

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import tradly.social.R
import tradly.social.common.*
import tradly.social.domain.entities.*
import tradly.social.common.base.BaseActivity
import tradly.social.ui.map.MapActivity
import tradly.social.ui.product.addProduct.AddProductActivity
import com.yalantis.ucrop.UCrop
import kotlinx.android.synthetic.main.activity_add_store_detail.*
import kotlinx.android.synthetic.main.activity_add_store_detail.recyclerAttribute
import kotlinx.android.synthetic.main.toolbar.*
import tradly.social.adapter.AttributeListAdapter
import tradly.social.adapter.ListingAdapter
import tradly.social.common.base.*
import tradly.social.common.util.parser.extension.toObject
import tradly.social.ui.map.LocationSearchFragment
import tradly.social.ui.store.storeDetail.StoreDetailActivity
import tradly.social.domain.entities.ProductAttribute
import java.io.File

class AddStoreDetailActivity : BaseActivity(), AddStorePresenter.View {

    private lateinit var addStorePresenter: AddStorePresenter
    private var selectedImagePath: String = Constant.EMPTY
    private var storeLocation:GeoPoint?=null
    private var attributeListAdapter: AttributeListAdapter? = null
    private lateinit var shippingMethodAdapter:ListingAdapter
    private var shippingMethodList = mutableListOf<ShippingMethod>()
    private var shouldFetchAttribute:Boolean = false
    private var selectedCategoryId:String = AppConstant.EMPTY
    private var isEdit:Boolean = false
    lateinit var productAttribute: ProductAttribute
    private lateinit var store:Store
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_store_detail)
        addStorePresenter = AddStorePresenter(this)
        isEdit = intent.getBooleanExtra(AppConstant.BundleProperty.IS_EDIT,false)
        setToolbar(toolbar, if(isEdit)R.string.addstore_edit_store else R.string.addstore_header_title, R.drawable.ic_back)
        if(isEdit)
        {
            store = intent.getStringExtra(AppConstant.BundleProperty.STORE).toObject<Store>()!!
            btnTxt?.setText(R.string.update)
            addStorePresenter.getStoreData(store.id)
        }
        else
        {
            addStorePresenter.loadStoreData()
        }
        initViews()
        setListeners()
    }

    private fun initViews(){
        AppConfigHelper.getConfigKey<String>(AppConfigHelper.Keys.ACCOUNT_ADDRESS_LABEL).let {
            if(it.isNotEmpty()){
                addressHint.text = it
            }
        }

        if(AppConfigHelper.getConfigKey(AppConfigHelper.Keys.SHIPPING_METHOD_PREFERENCE)){
            shippingMethodAdapter = ListingAdapter(this,shippingMethodList,
                AppConstant.ListingType.SHIPPING_METHOD_LIST,null){ position, obj ->}
            shipping_methods_recycler_view?.layoutManager = LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)
            shipping_methods_recycler_view?.adapter = shippingMethodAdapter
            shipping_methods_recycler_view?.isNestedScrollingEnabled = false
        }

        iconLocation.visibility = if(AppConfigHelper.getConfigKey(AppConfigHelper.Keys.ACCOUNT_MAP_LOCATION_SELECTOR_ENABLED)) View.VISIBLE else View.INVISIBLE
        storeNameHint.text = Utils.getSpannableMandatoryTitle(getString(R.string.addstore_store_name))
        categoryTypeHint.text = Utils.getSpannableMandatoryTitle(
            AppConfigHelper.getConfigKey(
                AppConfigHelper.Keys.ACCOUNT_CATEGORY_LABEL))
        preferred_shipment_title.text = Utils.getSpannableMandatoryTitle(getString(R.string.addstore_alert_message_choose_shipment))
        addressHint.text = Utils.getSpannableMandatoryTitle(getString(R.string.addstore_address))
        storeLinkHint.text = Utils.getSpannableMandatoryTitle(getString(R.string.addstore_store_url))
        storeLinkInputLayout?.prefixText = AppConfigHelper.getTenantConfigKey<String>(
            AppConfigHelper.Keys.BRANCH_LINK_BASE_URL)
        if(AppConfigHelper.getConfigKey(AppConfigHelper.Keys.ENABLE_BRANCH_UNIQUE_LINK)){
            if(AppController.appController.getUserStore()?.uniqueName.isNullOrEmpty()){
                storeLinkInputLayout?.visibility = View.VISIBLE
                storeLinkHint?.visibility = View.VISIBLE
            }
        }
        else{
            storeLinkInputLayout?.visibility = View.GONE
            storeLinkHint?.visibility = View.GONE
        }

        addressLayout.setConfigVisibility(AppConfigHelper.Keys.ACCOUNT_ADDRESS_ENABLED)

    }

    private fun populateStaticFields(store: Store){
        edStoreName?.setText(store.storeName)
        edStoreDesc?.setText(store.storeDescription)
        storeLinkInputEditText?.setText(store.uniqueName)
        if(store.storePic.isNotEmpty()){
            setStoreCoverImage(store.storePic)
        }
    }

    private fun setListeners(){

        addressLayout?.safeClickListener {
            if(!AppConfigHelper.getConfigKey<Boolean>(AppConfigHelper.Keys.ACCOUNT_MAP_LOCATION_SELECTOR_ENABLED)){
                val dialog = LocationSearchFragment()
                dialog.setListener(object :DialogListener{
                    override fun onClick(any: Any) {
                        val  address = any as Address
                        storeLocation = address.geoPoint
                        populateStoreAddress(address.formattedAddress)
                    }
                })
                dialog.show(supportFragmentManager, AppConstant.FragmentTag.SEARCH_LOCATION_FRAGMENT)
            }
            else{
                if (PermissionHelper.checkPermission(this, PermissionHelper.Permission.LOCATION)) {
                    checkSettings()
                } else {
                    LocationHelper.getInstance().requestLocationPermission(this)
                }
            }
        }

        btnCreateShop.safeClickListener {createStore()}

        addFab?.setOnClickListener {
            showMediaChooser()
        }

        cancelImg?.setOnClickListener {
            selectedImagePath = Constant.EMPTY
            selectedImage?.visibility = View.GONE
            addFab?.visibility = View.VISIBLE
            txtAddPhoto?.visibility = View.VISIBLE
            cancelImg?.visibility = View.GONE
        }

        categoryTypeSpinnerLayout?.setOnClickListener {
            shouldFetchAttribute = true
            categoryTypeSpinner?.performClick()
        }

        storeLinkInputEditText.onFocusChangeListener = View.OnFocusChangeListener { p0, isFocused ->
            storeLinkInputLayout?.isHelperTextEnabled = false
            storeLinkInputLayout?.helperText = Constant.EMPTY
        }

        storeLinkInputEditText.setFilter("^[A-Za-z0-9_.]*$")
    }

    private fun createStore(){
        if (!selectedImagePath.isNullOrEmpty()) {
            addStorePresenter.addStoreDetails(
                isEdit,if(isEdit)store.id else AppConstant.EMPTY,
                selectedCategoryId,
                storeName = edStoreName.getString(),
                webAddress = edStoreWebAddress.getString(),
                geoPoint = storeLocation,
                storeDescription = edStoreDesc.getString(),
                storeLinkName = storeLinkInputEditText.getString(),
                filePath = selectedImagePath,
                attributeList = attributeListAdapter?.getValues(),
                selectedShipments = getSelectedShippingMethods()
            )
        } else {
            showToast(R.string.addstore_select_cover_photo)
        }
    }

    private fun getSelectedShippingMethods():List<Int>{
        return if(AppConfigHelper.getConfigKey(AppConfigHelper.Keys.SHIPPING_METHOD_PREFERENCE)){
             shippingMethodList.filter { it.default }.map { it.id }
        }
        else{
            listOf()
        }
    }

    private fun startMapActivity() {
        val intent = Intent(this, MapActivity::class.java)
        startActivityForResult(intent, MapActivity.RESULT_CODE)
    }


    private fun showMediaChooser() {
        val dialog = BottomChooserDialog()
        dialog.setListener(object : DialogListener {
            override fun onClick(any: Any) {
                when (any as? Int) {
                    BottomChooserDialog.Type.CAMERA -> {
                        if (PermissionHelper.checkPermission(
                                this@AddStoreDetailActivity,
                                PermissionHelper.Permission.CAMERA
                            )
                        ) {
                            FileHelper.openCamera(this@AddStoreDetailActivity)
                        } else {
                            ActivityCompat.requestPermissions(
                                this@AddStoreDetailActivity,
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) arrayOf(Manifest.permission.CAMERA) else arrayOf(
                                    Manifest.permission.CAMERA,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                                ),
                                PermissionHelper.RESULT_CODE_CAMERA
                            )
                        }
                    }
                    BottomChooserDialog.Type.GALLERY -> {
                        if (PermissionHelper.checkPermission(
                                this@AddStoreDetailActivity,
                                PermissionHelper.Permission.READ_PERMISSION
                            )
                        ) {
                            FileHelper.openGallery(this@AddStoreDetailActivity)
                        } else {
                            ActivityCompat.requestPermissions(
                                this@AddStoreDetailActivity,
                                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                                PermissionHelper.RESULT_CODE_READ_STORAGE
                            )
                        }
                    }
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
                    this@AddStoreDetailActivity,
                    LocationHelper.LocationConstant.REQUEST_CHECK_SETTINGS
                )
            }
        }
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
                showToast(R.string.app_need_location_permission)
            }
        } else if (requestCode == PermissionHelper.RESULT_CODE_READ_STORAGE) {
            if (grantResults.count() > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                FileHelper.openGallery(this)
            } else {
              showToast(R.string.app_permission_gallery)
            }
        } else if (requestCode == PermissionHelper.RESULT_CODE_CAMERA || requestCode== PermissionHelper.RESULT_CODE_CAMERA_FILE_UPLOAD) {
            if (grantResults.count() > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    if (grantResults[1] != PackageManager.PERMISSION_GRANTED) {
                        showToast(R.string.app_permission_camera)
                        return
                    }
                }
                if(requestCode == PermissionHelper.RESULT_CODE_CAMERA_FILE_UPLOAD){
                    FileHelper.openCamera(this, FileHelper.RESULT_OPEN_CAMERA_FILE_UPLOAD)
                }
                else{
                    FileHelper.openCamera(this)
                }
            } else {
                showToast(R.string.app_permission_camera)
            }
        }
        else if(requestCode == PermissionHelper.RESULT_CODE_READ_STORAGE_DOC){
            FileHelper.openAllAttachment(this)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == LocationHelper.LocationConstant.REQUEST_CHECK_SETTINGS && resultCode == Activity.RESULT_OK) {  // TODO:Need to check Activity.RESULT_OK
            startMapActivity()
        } else if (requestCode == MapActivity.RESULT_CODE && resultCode == Activity.RESULT_OK && data != null) {
            val address = data.getStringExtra("address").toObject<Address>()
            storeLocation = address?.geoPoint
            populateStoreAddress(address?.formattedAddress)
        } else if (requestCode == FileHelper.RESULT_OPEN_GALLERY && resultCode == Activity.RESULT_OK && data != null) {
            val file = FileHelper.createTempFile()
            FileHelper().copy(data.data, file.absolutePath) {
                if (it) {
                    val uri = FileProvider.getUriForFile(this, getPackageName() + ".fileProvider", file)
                    FileHelper.cropImage(uri, this, false, false)
                }
            }
        } else if ((requestCode == FileHelper.RESULT_OPEN_CAMERA || requestCode == FileHelper.RESULT_OPEN_CAMERA_FILE_UPLOAD) && resultCode == Activity.RESULT_OK) {
            FileHelper().addImageToGallery(FileHelper.tempFile)
            val uri = FileProvider.getUriForFile(this, getPackageName() + ".fileProvider", FileHelper.tempFile)
            if (requestCode == FileHelper.RESULT_OPEN_CAMERA_FILE_UPLOAD){
                FileHelper.cropImage(uri, this, false, false,true, FileHelper.RESULT_OPEN_U_CROP_FILE_UPLOAD)
            }
            else{
                FileHelper.cropImage(uri, this, false, false)
            }
        }  else if (requestCode == UCrop.REQUEST_CROP && data != null && resultCode == Activity.RESULT_OK) {
            setStoreCoverImage(FileHelper.compressImageFile(this, FileHelper.tempFile.absolutePath))
        }
        else if(requestCode == FileHelper.RESULT_OPEN_U_CROP_FILE_UPLOAD && data != null && resultCode == Activity.RESULT_OK){
            val tempFile = FileHelper.compressImageFile(this, FileHelper.tempFile.absolutePath)
            if(::productAttribute.isInitialized){
                productAttribute.selectedValues.add(Value(any = FileInfo(fileUri = File(tempFile).absolutePath)))
                attributeListAdapter?.notifyDataSetChanged()
            }
        }
        else if(requestCode == FileHelper.RESULT_OPEN_ALL_DOC && resultCode == Activity.RESULT_OK){
            data?.data?.let { uri->
                val mimeType = AppController.appContext.contentResolver.getType(uri)
                val tempFile = FileHelper.createTempFile(FileHelper().getExtnFromMimeType(mimeType))
                FileHelper().copy(uri,tempFile.absolutePath){
                    if(::productAttribute.isInitialized){
                        productAttribute.selectedValues.add(Value(any = FileInfo(fileUri = tempFile.absolutePath)))
                        attributeListAdapter?.notifyDataSetChanged()
                    }
                }
            }
        }
    }

    private fun populateStoreAddress(formattedAddress:String?) {
        txtAddress?.text = formattedAddress
    }

    private fun setStoreCoverImage(selectedImagePath:String){
        this.selectedImagePath = selectedImagePath
        selectedImage?.visibility = View.VISIBLE
        addFab?.visibility = View.GONE
        txtAddPhoto?.visibility = View.GONE
        cancelImg?.visibility = View.VISIBLE
        if(selectedImagePath.startsWith("http"))
        {
            ImageHelper.getInstance().showImage(this, selectedImagePath, selectedImage, R.drawable.placeholder_image, R.drawable.placeholder_image)
        }
        else
        {
            ImageHelper.getInstance().showImage(this, Uri.fromFile(File(selectedImagePath)), selectedImage, R.drawable.placeholder_image, R.drawable.placeholder_image)
        }

    }

    override fun onSuccess(store:Store) {
        Utils.showAlertDialog(this,Constant.EMPTY,getString(if(isEdit)R.string.addstore_store_updated_successfully else R.string.addstore_store_created_successfully),false,false,object :
            Utils.DialogInterface{
            override fun onAccept() {
                intent?.let {
                    when(it.getStringExtra(AppConstant.BundleProperty.IS_FROM)){
                        "settings","profile"->{
                            startActivity(StoreDetailActivity::class.java,Bundle().apply {
                                putString("id",store.id)
                                putString("storeName",store.storeName)
                                putBoolean("isFromStoreCreation",true)
                            })
                            finish()
                        }
                        AppConstant.BundleProperty.MY_STORE ->{
                            setResult(Activity.RESULT_OK, Intent())
                            finish()
                        }
                        "home"->{
                            startActivity(AddProductActivity::class.java,Bundle().apply { putBoolean("isFromHome",intent.getBooleanExtra("isFromHome",false)) })
                            finish()
                        }
                    }
                }
            }
            override fun onReject() {

            }
        })
        addStorePresenter.syncUserStore()
        EventHelper.logFbEvent("Account Created")
    }

    override fun onLoadCategories(list: List<Category>) {
        if(list.isNotEmpty()){
            categoryTypeSpinnerLayout?.visibility = View.VISIBLE
            selectedCategoryId = list[0].id
            val categoryList = list.map { it.name }
            val categoryAdapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categoryList)
            categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            categoryTypeSpinner?.adapter = categoryAdapter
            categoryTypeSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, selectedPosition: Int, p3: Long) {
                    list[selectedPosition].apply {
                        selectedCategoryId = this.id
                        txtCategoryType?.text = this.name
                    }
                   if(shouldFetchAttribute){
                       addStorePresenter.getAttributes(list[selectedPosition].id,false)
                   }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {

                }
            }
            if(isEdit){
                list.indexOfFirst { c->c.id == store.categoryId }.let { index->
                    if(index!=-1){
                        categoryTypeSpinner?.setSelection(index)
                    }
                }
            }
            else{
                categoryTypeSpinner?.setSelection(0)
            }
        }
        else{
            addStorePresenter.getAttributes(selectedCategoryId,false)
        }
    }

    override fun onLoadAttribute(attributeList: List<Attribute>) {
        if (attributeList.isNotEmpty()) {
            recyclerAttribute.visibility = View.VISIBLE
            recyclerAttribute.isNestedScrollingEnabled = false
            recyclerAttribute.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
            attributeListAdapter = AttributeListAdapter(this, AttributeHelper.mapFrom(attributeList, if(isEdit)store.attributes else listOf())){clickAction,obj->
                handleAttributeAction(clickAction,obj as ProductAttribute)
            }
            recyclerAttribute.adapter = attributeListAdapter
        } else {
            recyclerAttribute.visibility = View.GONE
        }
    }

    override fun onMediaUploadFailed(msg: Int) {
        Toast.makeText(this, getString(msg), Toast.LENGTH_SHORT).show()
    }

    override fun onFailure(appError: AppError) {
        ErrorHandler.handleError(this, appError)
    }

    override fun showProgressDialog(msg: Int) {
        showLoader(msg)
    }

    override fun hideProgressDialog() {
        hideLoader()
    }

    override fun noInternet() {
        Toast.makeText(this, getString(R.string.no_internet), Toast.LENGTH_SHORT).show()
    }

    override fun inputError(id: Int, msg: Int , any:String) {
        when (id) {
            R.id.edStoreName -> edStoreName.error = getString(msg)
            R.id.edStoreDesc -> edStoreDesc.error = getString(msg)
            R.id.storeLinkInputEditText-> storeLinkInputEditText.error = getString(msg)
            R.id.edStoreWebAddress -> edStoreWebAddress.error = getString(msg)
            R.id.txtAddress-> {
                Toast.makeText(this, getString(msg), Toast.LENGTH_SHORT).show()
            }
            R.id.categoryTypeSpinnerLayout->{
                showToast(getString(msg))
                addStorePresenter.getCategory()
            }
            R.id.addAddressLayout->showToast(getString(msg))
            R.id.recyclerAttribute-> showToast(any)
            else->{
                if(msg!=0){
                    showToast(getString(msg))
                }
            }
        }
    }


    override fun onLoadShippingMethod(list: List<ShippingMethod>) {
        shippingMethodList.clear()
        shippingMethodList.addAll(list)
        if(isEdit){
            if(store.shippingMethods.isNotEmpty()){
                list.forEach { it.default = false }
                store.shippingMethods.forEach { list.find { c->c.id== it.id }?.default = true }
            }
        }
        if(::shippingMethodAdapter.isInitialized){
            shipping_methods_layout?.visibility = if(list.isNotEmpty())View.VISIBLE else View.GONE
            shippingMethodAdapter.notifyDataSetChanged()
        }
    }

    private fun handleAttributeAction(action:Int,productAttribute: ProductAttribute){
        this.productAttribute = productAttribute
        if(action == AttributeListAdapter.ActionType.UPLOAD_FIELD){
            Utils.showMediaChooseDialog(this){
                if(it == R.id.clPhotoView){
                    if (PermissionHelper.checkPermission(this@AddStoreDetailActivity, PermissionHelper.Permission.CAMERA)) {
                        FileHelper.openCamera(this@AddStoreDetailActivity, FileHelper.RESULT_OPEN_CAMERA_FILE_UPLOAD)
                    } else {
                        ActivityCompat.requestPermissions(this@AddStoreDetailActivity, if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) arrayOf(Manifest.permission.CAMERA) else arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE), PermissionHelper.RESULT_CODE_CAMERA_FILE_UPLOAD)
                    }
                }
            }
        }
        else{
            openDocument()
        }
    }

    private fun openDocument(){
        if(PermissionHelper.checkPermission(this, PermissionHelper.Permission.READ_PERMISSION)){
            FileHelper.openAllAttachment(this)
        }
        else{
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PermissionHelper.RESULT_CODE_READ_STORAGE_DOC)
        }
    }

    override fun onLoadStore(store: Store) {
        this.store = store
        populateStaticFields(store)
        storeLocation = store.geoPoint
        populateStoreAddress(store.address.formattedAddress)
    }

    override fun onShowLinkStatus(success: Boolean) {
        storeLinkInputLayout.isHelperTextEnabled = !success
        storeLinkInputLayout?.helperText = if(success)Constant.EMPTY else getString(R.string.addstore_alert_message_store_link_alias)
    }

    override fun onDestroy() {
        super.onDestroy()
        addStorePresenter.onDestory()
    }
}
