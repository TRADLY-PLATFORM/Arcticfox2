package tradly.social.ui.product.addProduct

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import tradly.social.R
import tradly.social.adapter.AttributeListAdapter
import tradly.social.adapter.ProductViewPagerAdapter
import tradly.social.common.*
import tradly.social.domain.entities.ImageFeed
import tradly.social.domain.entities.*
import tradly.social.common.base.BaseActivity
import tradly.social.ui.tags.TagActivity
import com.google.android.material.chip.Chip
import com.yalantis.ucrop.UCrop

import kotlinx.android.synthetic.main.activity_add_product.*
import kotlinx.android.synthetic.main.activity_add_product.chipGroup
import kotlinx.android.synthetic.main.activity_add_product.iconLocation
import kotlinx.android.synthetic.main.activity_add_product.recyclerAttribute
import kotlinx.android.synthetic.main.activity_add_product.txtCategory
import kotlinx.android.synthetic.main.activity_add_product.viewPager
import kotlinx.android.synthetic.main.toolbar.toolbar
import tradly.social.common.base.*
import tradly.social.common.cache.AppCache
import tradly.social.common.event.addEvent.viewmodel.VariantViewModel
import tradly.social.common.navigation.Activities
import tradly.social.common.navigation.common.NavigationIntent
import tradly.social.common.network.CustomError
import tradly.social.common.network.RequestID
import tradly.social.common.util.parser.extension.toJson
import tradly.social.common.util.parser.extension.toList
import tradly.social.common.util.parser.extension.toObject
import tradly.social.data.model.CoroutinesManager
import tradly.social.common.network.feature.common.datasource.PaymentDataSourceImpl
import tradly.social.databinding.AddProductActivityBinding
import tradly.social.domain.entities.Currency
import tradly.social.domain.repository.PaymentRepository
import tradly.social.domain.usecases.SendSubscribeMail
import tradly.social.event.explore.common.VariantAdapter
import tradly.social.ui.map.LocationSearchFragment
import tradly.social.ui.map.MapActivity
import tradly.social.ui.store.storeDetail.StoreDetailActivity
import java.util.*
import kotlin.collections.ArrayList
import tradly.social.common.resources.CommonResourceString
import java.io.File


class AddProductActivity : BaseActivity(), AddProductPresenter.View,GenericAdapter.OnClickItemListener<Variant>,CustomOnClickListener.OnCustomClickListener {
    private lateinit var addProductPresenter: AddProductPresenter
    private lateinit var variantViewModel:VariantViewModel
    private lateinit var binding: AddProductActivityBinding
    lateinit var productAttribute: ProductAttribute
    private lateinit var variantAdapter:VariantAdapter
    private var tagList = mutableListOf<String>()
    private var variantList = mutableListOf<Variant>()
    private var categoryList = mutableListOf<Category>()
    private var subCategoryList = mutableListOf<Category>()
    private var imageList = ArrayList<ImageFeed>()
    private var imageViewPagerAdapter: ProductViewPagerAdapter? = null
    private var selectedCategoryId: String = Constant.EMPTY
    private var selectedCurrency: Currency? = null
    private var product:Product?=null
    private var isForEdit:Boolean = false
    private var isFromHome:Boolean = false
    private var maxQtyVisible:Boolean = false
    private var shippingChargeVisible:Boolean = false
    private var listingLocation:GeoPoint? = null
    private var listingLocationAddress:Address?=null
    private var attributeListAdapter: AttributeListAdapter? = null
    private val colorPrimary: Int by lazy { ThemeUtil.getResourceValue(this, R.attr.colorPrimary) }
    private val uploadPhotoCount:Int = AppConfigHelper.getConfigKey<Int>(AppConfigHelper.Keys.LISTING_PHOTO_UPLOAD_COUNT,1)
    private var eventFromMillis:Long = 0
    private var eventToMillis:Long = 0
    private var isVariantListChanged:Boolean = false
    private val isEventModule = AppCache.getCacheAppConfig()!!.module == AppConstant.ConfigModuleType.EVENT

    object REQUEST_CODE {
        const val TAG_RESULT = 200
        const val GROUP_RESULT = 201
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_add_product)
        binding.lifecycleOwner = this
        binding.onClickListener = CustomOnClickListener(this)
        val toolbarTitle = if (AppCache.getCacheAppConfig()!!.module == AppConstant.ConfigModuleType.EVENT) CommonResourceString.add_event_form_title else R.string.addproduct_header_title
        setToolbar(toolbar, toolbarTitle, R.drawable.ic_back)
        addProductPresenter = AddProductPresenter(this)
        variantViewModel = getViewModel { VariantViewModel() }
        setDataFromIntent()
        observeLiveData()
        initView()
        initAdapter()
        if(isForEdit){
         populateProduct(product!!)
        }
        setListener()
    }

    private fun setDataFromIntent(){
        isForEdit = intent.getBooleanExtra("isForEdit",false)
        isFromHome = intent.getBooleanExtra("isFromHome",false)
        maxQtyVisible = AppConfigHelper.getConfigKey<Boolean>(AppConfigHelper.Keys.SHOW_MAX_QUANTITY)
        shippingChargeVisible = AppConfigHelper.getConfigKey<Boolean>(AppConfigHelper.Keys.SHOW_SHIPPING_CHARGE)
        if(isForEdit){
            product = intent.getStringExtra("product").toObject<Product>()
            product?.images?.forEach {
                imageList.add(ImageFeed(it,false))
            }
            if(imageList.size<=3){
                imageList.add(ImageFeed(isAddItem = true))
            }
        }
    }

    private fun observeLiveData(){
        observeEvent(variantViewModel.uiState,this::handleUIState)
        observeEvent(variantViewModel.variantTypeResultLiveData,{onVariantTypeResponse()})

    }

    override fun onCustomClick(view: View) {
        when(view.id){
            R.id.ivDelete->{
                enableAddEventTimeView()
            }
            R.id.ivEdit-> showAddEventTime()
        }
    }

    private fun setListener(){
        addTagIcon.setOnClickListener { showTagActivity() }
        binding.layoutEventVariance.btnAddVariant.setOnClickListener { showAddVariant(null) }
        btnCreate?.setOnClickListener { onClickCreate() }
        binding.layoutAddEventTime.btnAddTime.setOnClickListener { showAddEventTime() }
        categoryLayout?.setOnClickListener {
            if (categoryList.count() > 0) {
                categoryTypeSpinner?.performClick()
            }
        }

        subCategoryLayout?.setOnClickListener{
            subCategoryTypeSpinner?.performClick()
        }
    }

    private fun onVariantTypeResponse(){
        showAddVariant(null)
    }

    private fun populateProduct(product: Product){
        edProductTitle.setText(product.title)
        edPrice.setText(product.listPrice?.amount?.toString())
        edPriceOffer.setText(product.offerPercent.toString())
        edDesc.setText(product.description)
        edMaxQty?.setText(product.maxQuantity.toString())
        edShipping?.setText(product.shipping.amount.toString())
        populateStoreAddress(product.address?.formattedAddress)
        edStock.setText(product.stock.toString())
        addTags(product.tags.map{it.name})
        listingLocation = product.location
        if (AppCache.getCacheAppConfig()?.module == AppConstant.ConfigModuleType.EVENT){
            product.variants.forEach { it.values = it.variantValues }  // For Temp fix.
            variantList.addAll(product.variants)
            variantAdapter.notifyDataSetChanged()
            eventToMillis = product.endAt
            eventFromMillis = product.startAt
            populateEventTime()
            binding.layoutEventTimeCard.apply {
                ivEdit.setGone()
                ivDelete.setGone()
            }
        }
    }

    private fun enableAddEventTimeView(){
        binding.layoutAddEventTime.root.setVisible()
        binding.layoutEventTimeCard.root.setGone()
        eventFromMillis = 0L
        eventToMillis = 0L
    }

    private fun populateEventTime(){
        binding.layoutAddEventTime.root.setGone()
        binding.layoutEventTimeCard.root.setVisible()
        binding.apply {
            setVariable(BR.startMillis,eventFromMillis)
            setVariable(BR.endMillis,eventToMillis)
            setVariable(BR.dateFormat,DateTimeHelper.FORMAT_DATE_EEE_D_MMM_YYYY)
            setVariable(BR.timeFormat,DateTimeHelper.FORMAT_TIME_AM_PM)
            executePendingBindings()
        }
    }

    private fun populateStoreAddress(formattedAddress: String?) {
        txtAddress?.text = formattedAddress
    }

    private fun initAdapter() {
        if(!isForEdit){
            imageList.clear()
            imageList.add(ImageFeed(isAddItem = true))
        }
        imageViewPagerAdapter = ProductViewPagerAdapter(this, imageList, {
            showMediaChooser()
        }, {
            if (imageList.filter { i -> i.isAddItem }.singleOrNull() == null) {
                imageList.add(ImageFeed(isAddItem = true))
            }
            imageList.removeAt(it)
            imageViewPagerAdapter?.notifyDataSetChanged()
            if (imageList.count() == 1) {
                viewPager.setPadding(50, 50, 50, 50)
            }
        })
        viewPager.clipToPadding = false
        viewPager.setPadding(50, 50, 50, 50)
        viewPager?.pageMargin = 25
        viewPager?.adapter = imageViewPagerAdapter
        if (AppCache.getCacheAppConfig()?.module == AppConstant.ConfigModuleType.EVENT){
            variantAdapter = VariantAdapter()
            variantAdapter.items = variantList
            variantAdapter.setViewType(1)
            variantAdapter.onClickListener = this
            binding.layoutEventVariance.rvVariantList.apply {
                layoutManager = LinearLayoutManager(this@AddProductActivity,LinearLayoutManager.VERTICAL,false)
                adapter = variantAdapter
            }
        }
    }


    private fun handleAttributeAction(action:Int,productAttribute: ProductAttribute){
        this.productAttribute = productAttribute
        if(action == AttributeListAdapter.ActionType.UPLOAD_FIELD){
            Utils.showMediaChooseDialog(this){
                if(it == R.id.clPhotoView){
                    if (PermissionHelper.checkPermission(this, PermissionHelper.Permission.CAMERA)) {
                        FileHelper.openCamera(this, FileHelper.RESULT_OPEN_CAMERA_FILE_UPLOAD)
                    } else {
                        ActivityCompat.requestPermissions(this, if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) arrayOf(Manifest.permission.CAMERA) else arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE), PermissionHelper.RESULT_CODE_CAMERA_FILE_UPLOAD)
                    }
                }
                else{
                    openDocument()
                }
            }
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

    private fun initView() {
        if(isForEdit){
            btnCreateTxt.text = getString(R.string.addproduct_save)
        }
        addProductPresenter.loadCurrency()
        addProductPresenter.loadCategories(true)
        edPrice?.setOnFocusChangeListener { view, b ->
            if (b) {
                dividerPrice?.setBackgroundColor(ContextCompat.getColor(this, colorPrimary))
            } else {
                dividerPrice?.setBackgroundColor(
                    ContextCompat.getColor(
                        this,
                        R.color.colorVeryLightBlack
                    )
                )
            }
        }

        addressLayout?.safeClickListener {
            if(!AppConfigHelper.getConfigKey<Boolean>(AppConfigHelper.Keys.LISTING_MAP_LOCATION_SELECTOR_ENABLED)){
                val dialog = LocationSearchFragment()
                dialog.setListener(object :DialogListener{
                    override fun onClick(any: Any) {
                        val address = any as Address
                        listingLocation = address.geoPoint
                        listingLocationAddress = address
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

        // Field visibility depends on configs
        AppConfigHelper.getConfigKey<String>(AppConfigHelper.Keys.LISTING_ADDRESS_LABEL).let {
            if(it.isNotEmpty()){
                addressHint.text = it
            }
        }

        val titleHintValue = getString(if (isEventModule) R.string.addproduct_product_title else R.string.add_event_title)
        productTitleHint.text = titleHintValue
        val stockEnabled = AppConfigHelper.getConfigKey<Boolean>(AppConfigHelper.Keys.STOCK_ENABLED)
        val stockHintValue = if (isEventModule) getString(R.string.addproduct_ticket) else getString(R.string.addproduct_stock)
        stockHint.text = stockHintValue
        stockLayout.visibility = if (stockEnabled)View.VISIBLE else View.GONE
        maxQtyLayout?.visibility = if(maxQtyVisible)View.VISIBLE else View.GONE
        shippingChargeLayout?.visibility = if(shippingChargeVisible) View.VISIBLE else View.GONE

        if(!isForEdit && maxQtyVisible){
            edMaxQty.setText("1")
        }

        if(!isForEdit){
            tradly.social.common.persistance.shared.PreferenceSecurity.getString(preferenceConstant.PREF_LAST_CAPTURED_LOCATION).toObject<Address>()?.apply {
                listingLocation = this.geoPoint
                listingLocationAddress = this
                populateStoreAddress(this.formattedAddress)
            }
        }
        priceHint.text = Utils.getSpannableMandatoryTitle(getString(R.string.addproduct_price))
        productTitleHint.text = Utils.getSpannableMandatoryTitle(productTitleHint.text.toString())
        categoryHint.text = Utils.getSpannableMandatoryTitle(getString(R.string.addproduct_category))
        maxQtyHint.text = Utils.getSpannableMandatoryTitle(getString(R.string.addproduct_max_qty))
        addressHint.text = Utils.getSpannableMandatoryTitle(getString(R.string.addproduct_address))
        iconLocation.visibility = if(AppConfigHelper.getConfigKey(AppConfigHelper.Keys.LISTING_MAP_LOCATION_SELECTOR_ENABLED)) View.VISIBLE else View.INVISIBLE
        //addressLayout.setConfigVisibility(AppConfigHelper.Keys.LISTING_ADDRESS_ENABLED)
        addressLayout.setVisible()
        tagLayout.visibility = if(AppConfigHelper.getConfigKey(AppConfigHelper.Keys.HIDE_LISTING_TAGS,false)) View.GONE else View.VISIBLE
        if (AppCache.getCacheAppConfig()?.module == AppConstant.ConfigModuleType.EVENT){
            binding.layoutEventVariance.root.setVisible()
            binding.layoutEventTime.setVisible()
        }
    }

    override fun onClick(value: Variant, view: View, itemPosition: Int){
        when(view.id){
            R.id.cvVariantItemParent-> showAddVariant(value)
        }
    }

    private fun showAddVariant(variant:Variant?){
        val variantTypeList = variantViewModel.variantTypeLiveData.value
        val bundle = Bundle().apply {
            putString(Activities.VariantHostActivity.EXTRAS_VARIANT_TYPE_LIST,variantTypeList.toJson<List<VariantType>>())
            putString(Activities.VariantHostActivity.EXTRAS_VARIANT_LIST,this@AddProductActivity.variantList.toJson<List<Variant>>())
            if(variant!=null){
                putString(Activities.VariantHostActivity.EXTRAS_VARIANT,variant.toJson<Variant>())
            }
            putInt(Activities.VariantHostActivity.EXTRAS_CRUD_ACTION,if (variant==null)Activities.VariantHostActivity.CRUD_ACTION.ADD else Activities.VariantHostActivity.CRUD_ACTION.EDIT)
            selectedCurrency?.let {
                putString(Activities.VariantHostActivity.EXTRAS_CURRENCY,it.toJson<Currency>())
            }
            if (isForEdit){
                putString(Activities.VariantHostActivity.EXTRAS_LISTING_ID,product!!.id)
            }
        }
        val intent = NavigationIntent.getIntent(Activities.VariantHostActivity,bundle)
        startActivityResult(intent,ActivityRequestCode.VariantHostActivity)
    }

    private fun showAddEventTime(){
        val bundle = Bundle().apply {
           if (eventFromMillis!=0L){
               putLong(Activities.EventTimeHostActivity.EXTRAS_FROM_MILLIS,eventFromMillis)
               putLong(Activities.EventTimeHostActivity.EXTRAS_TO_MILLIS,eventToMillis)
           }
        }
        val intent = NavigationIntent.getIntent(Activities.EventTimeHostActivity,bundle)
        startActivityForResult(intent,ActivityRequestCode.EventTimeHostActivity)
    }

    private fun checkSettings() {
        LocationHelper.getInstance().checkSettings { success, exception ->
            if (success) {
                startActivityForResult(MapActivity::class.java,MapActivity.RESULT_CODE)
            } else {
                exception?.startResolutionForResult(
                    this,
                    LocationHelper.LocationConstant.REQUEST_CHECK_SETTINGS
                )
            }
        }
    }

    private fun onclickAddVariant(){
        if (variantViewModel.variantTypeLiveData.value==null){
            variantViewModel.getVariantTypes()
        }
        else{
            showAddVariant(null)
        }
    }

    private fun onClickCreate(){
        val uploadImageList = ArrayList<ImageFeed>(imageList)
        uploadImageList.removeAll { i->i.isAddItem }
        addProductPresenter.addProduct(
            AppController.appController.getUserStore()?.id,
            product?.id,
            edProductTitle.getString(),
            selectedCurrency?.id?.toString(),
            edPrice.getString(),
            edPriceOffer.getString(),
            selectedCategoryId,
            edDesc.getString(),
            edMaxQty.getString(),
            edStock.getString(),
            edShipping.getString(),
            tagList,
            attributeListAdapter?.getValues()
            ,uploadImageList,
            listingLocation,
            isForEdit,
            this.variantList,
            eventFromMillis,
            eventToMillis
        )
    }

    override fun loadAttributes(attributeList: List<Attribute>) {
        if (attributeList.isNotEmpty()) {
            recyclerAttribute.visibility = View.VISIBLE
            recyclerAttribute.isNestedScrollingEnabled = false
            recyclerAttribute.layoutManager =
                LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
            attributeListAdapter =
                AttributeListAdapter(this, AttributeHelper.mapFrom(attributeList ,if(isForEdit) product?.attributes else listOf<Attribute>())){ isFor: Int, obj: Any ->
                    handleAttributeAction(isFor,obj as ProductAttribute)
                }
            recyclerAttribute.adapter = attributeListAdapter
        } else {
            recyclerAttribute.visibility = View.GONE
        }
    }

    override fun loadCurrency(currencies: List<Currency>) {
        val list = currencies.map { it.code.plus("-").plus(it.name) }
        val currencyAdapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, list)
        currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        currencySpinner?.adapter = currencyAdapter
        currencySpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                txtCurrency?.text = currencies[position].code
                selectedCurrency = currencies[position]
                if(shippingChargeVisible){
                    edShipping?.hint = selectedCurrency?.code
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }
        currencySpinner.setSelection(0)
        currencyListLayout?.setOnClickListener { currencySpinner.performClick() }
        if(isForEdit){
            currencies.find { c->c.id == product?.currencyId }?.let {
                currencySpinner.setSelection(currencies.indexOf(it))
            }
        }

    }

    override fun showTags(list: List<Tag>, constrait: String) {

    }

    override fun networkError() {
        showToast(R.string.no_internet)
    }

    override fun onMediaUploadFailed(msg: Int) {
        showToast(msg)
    }

    override fun onFailure(appError: AppError) {
        if(appError.code == CustomError.SUBSCRIPTION_NOT_ENABLED){
            showSubscriptionDialog()
        }
        else{
            showToast(R.string.something_went_wrong)
        }
        ErrorHandler.handleError(exception = appError)
    }

    private fun showSubscriptionDialog(){
        val store = AppController.appController.getUserStore()!!
        Utils.showAlertDialog(this,null,message = AppConfigHelper.getConfigKey(AppConfigHelper.Keys.SUBSCRIPTION_NOT_ACTIVE_MESSAGE)
            ,false,false,dialogInterface = object : Utils.DialogInterface{
                override fun onAccept() {
                    if(NetworkUtil.isConnectingToInternet()){
                        showLoader(R.string.please_wait)
                        val sendSubscribeMail = SendSubscribeMail(PaymentRepository(
                            PaymentDataSourceImpl()
                        ))
                        CoroutinesManager.ioThenMain({sendSubscribeMail.sendMail(store.id)},{
                            if(!isFinishing){
                                hideLoader()
                                when(it){
                                    is Result.Success->showToast("Mail sent successfully")
                                    is Result.Error-> ErrorHandler.handleError(this@AddProductActivity,it.exception)
                                }
                            }
                        })
                    }
                }

                override fun onReject() {

                }

            })
    }

    override fun onSuccess(id: String) {
        Utils.showProductAddedDialog(this,isForEdit){
            when(it){
                R.id.btnOne->{
                    if(isFromHome){
                        val bundle = Bundle()
                        bundle.putString("id",AppController.appController.getUserStore()?.id)
                        startActivity(StoreDetailActivity::class.java,bundle)
                    }
                    setResult(Activity.RESULT_OK, Intent())
                    finish()
                }
            }
        }
        if(!isForEdit){
            tradly.social.common.persistance.shared.PreferenceSecurity.putString(preferenceConstant.PREF_LAST_CAPTURED_LOCATION,listingLocationAddress.toJson<Address>())
        }
    }

    override fun showProgressDialog(msg: Int , title:Int) {
        showLoader(msg,title)
    }

    override fun hideProgressDialog() {
        hideLoader()
    }

    override fun changeProgressMessage(msg: Int, title: Int) {
        changeLoaderMessage(msg,title)
    }

    private fun handleUIState(uiState: UIState<Unit>){
        when(uiState){
            is UIState.Loading->{
                if(uiState.apiId == RequestID.GET_VARIANT_TYPES){
                    if(uiState.isLoading){
                        showLoader(R.string.please_wait)
                    }
                    else{
                        hideLoader()
                    }
                }
            }
            is UIState.Failure-> ErrorHandler.handleError(uiState.errorData)
        }
    }

    override fun showFieldError(id: Int, msg: Any) {
        when (id) {
            R.id.recycler_view ->showToast(msg as Int)
            R.id.edProductTitle ->edProductTitle?.error = getString(msg as Int)
            R.id.edPrice ->{
                val minPrice = AppConfigHelper.getConfigKey<Int>(AppConfigHelper.Keys.LISTING_MIN_PRICE)
                edPrice?.error = getString(R.string.addproduct_min_price_error,"$minPrice ${selectedCurrency?.symbol}")
            }
            R.id.categoryLayout->{
                showToast(msg as Int)
                addProductPresenter.loadCategories(false)
            }
            R.id.edStock->edStock?.error = getString(msg as Int)
            R.id.edDesc ->edDesc?.error = getString(msg as Int)
            R.id.edPriceOffer->edPriceOffer?.error = getString(msg as Int)
            R.id.recyclerAttribute ->showToast(msg as String)
            R.id.edMaxQty ->showToast(getString(msg as Int))
            R.id.addAddressLayout->showToast(getString(msg as Int))
        }
    }

    override fun loadCategories(list: List<Category>) {
        if (list.count() > 0) {
            categoryList.clear()
            categoryList.addAll(list)
            val mCategoryList = mutableListOf<String>()
            for (obj in categoryList) {
                mCategoryList.add(obj.name)
            }
            val adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, mCategoryList)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            categoryTypeSpinner.setOnItemSelectedListener(object :
                AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    txtCategory?.text = mCategoryList[position]
                    selectedCategoryId = categoryList[position].id
                    val subCategoryList = categoryList[position].subCategory
                    if(subCategoryList.isEmpty()){
                        addProductPresenter.getAttribute(list[position].id,false)
                    }
                    loadSubCategory(subCategoryList)
                }

                override fun onNothingSelected(parent: AdapterView<*>) {

                }
            })
            categoryTypeSpinner.adapter = adapter
            categoryTypeSpinner.setSelection(0)
            if(isForEdit){
                Handler().postDelayed({ loadChosenCategory(list) },3000)
            }
        }
        else{
            hideProgressDialog()
        }

    }


    private fun loadChosenCategory(list: List<Category>){
        product?.let {
            if(it.categoryIds.isNotEmpty()){
                list.find { c->c.id == it.categoryIds[0].toString() }?.let {
                    categoryTypeSpinner?.setSelection(list.indexOf(it),true)
                    return
                }

                list.forEach { category->
                    category.subCategory.find { c->c.id == it.categoryIds[0].toString() }?.let { subCategory->
                        categoryTypeSpinner?.setSelection(list.indexOf(category))
                        subCategoryTypeSpinner?.setSelection(category.subCategory.indexOf(subCategory))
                    }
                }
            }
        }
    }

    fun loadSubCategory(list: List<Category>){
        if(list.count()>0){
            subCategoryLayout?.visibility = View.VISIBLE
            subCategoryList.clear()
            subCategoryList.addAll(list)
            val mCategoryList = mutableListOf<String>()
            for (obj in subCategoryList) {
                mCategoryList.add(obj.name)
            }
            val adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, mCategoryList)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            subCategoryTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    txtSubCategory?.text = mCategoryList[position]
                    selectedCategoryId = subCategoryList[position].id
                    addProductPresenter.getAttribute(list[position].id)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }

            }
            subCategoryTypeSpinner.adapter = adapter
            subCategoryTypeSpinner.setSelection(0)
        }
        else{
            subCategoryLayout?.visibility = View.GONE
        }
    }

    private fun showMediaChooser() {
        val dialog = BottomChooserDialog()
        dialog.setListener(object : DialogListener {
            override fun onClick(any: Any) {
                when (any as? Int) {
                    BottomChooserDialog.Type.CAMERA -> {
                        if (PermissionHelper.checkPermission(
                                this@AddProductActivity,
                                PermissionHelper.Permission.CAMERA
                            )
                        ) {
                            FileHelper.openCamera(this@AddProductActivity)
                        } else {
                            ActivityCompat.requestPermissions(this@AddProductActivity,
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) arrayOf(Manifest.permission.CAMERA) else arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                                PermissionHelper.RESULT_CODE_CAMERA
                            )
                        }
                    }
                    BottomChooserDialog.Type.GALLERY -> {
                        if (PermissionHelper.checkPermission(
                                this@AddProductActivity,
                                PermissionHelper.Permission.READ_PERMISSION
                            )
                        ) {
                            FileHelper.openGallery(this@AddProductActivity)
                        } else {
                            ActivityCompat.requestPermissions(
                                this@AddProductActivity,
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE.TAG_RESULT && resultCode == Activity.RESULT_OK && data != null) {
            addTags(data.getStringExtra("list"))
        } else if (requestCode == REQUEST_CODE.GROUP_RESULT && resultCode == Activity.RESULT_OK && data != null) {
            addGroups(data.getStringExtra("list"))
        } else if (requestCode == FileHelper.RESULT_OPEN_GALLERY && data != null && resultCode == Activity.RESULT_OK) {
            val file = FileHelper.createTempFile()
            FileHelper().copy(data.data, file.absolutePath) {
                if (it) {
                    val uri =
                        FileProvider.getUriForFile(this, getPackageName() + ".fileProvider", file)
                    FileHelper.cropImage(uri, this, false, false)
                }
            }
        } else if (requestCode == FileHelper.RESULT_OPEN_CAMERA && resultCode == Activity.RESULT_OK) {
            FileHelper().addImageToGallery(FileHelper.tempFile)
            val uri = FileProvider.getUriForFile(this, "$packageName.fileProvider", FileHelper.tempFile)
            FileHelper.cropImage(uri, this, false, false)
        } else if (requestCode == UCrop.REQUEST_CROP && data != null && resultCode == Activity.RESULT_OK) {
            if (imageList.count() == 1) {
                viewPager.setPadding(50, 50, 150, 50)
            }
            var selectedImagePath = FileHelper.compressImageFile(this, FileHelper.tempFile.absolutePath)
            val imageFeed = ImageFeed(filePath = selectedImagePath, isAddItem = false)
            if (imageList.count() == uploadPhotoCount) {  //TODO +1 for Add view
                imageList.removeAt(imageList.count() - 1)
                imageList.add(imageFeed)
            } else {
                imageList.add(imageList.count() - 1, imageFeed)
            }

            imageViewPagerAdapter?.notifyDataSetChanged()

        }
        else if (requestCode == MapActivity.RESULT_CODE && resultCode == Activity.RESULT_OK && data != null) {
            val address = data.getStringExtra("address").toObject<Address>()
            listingLocation = address?.geoPoint
            listingLocationAddress = address
            populateStoreAddress(address?.formattedAddress)
        }
        else if (requestCode == ActivityRequestCode.VariantHostActivity && resultCode == Activity.RESULT_OK && data != null){
            val action = data.getIntExtra(Activities.VariantHostActivity.EXTRAS_CRUD_ACTION,0)
            val variant = data.getStringExtra(Activities.VariantHostActivity.EXTRAS_VARIANT).toObject<Variant>()
            isVariantListChanged = true
            when(action){
                Activities.VariantHostActivity.CRUD_ACTION.ADD->{
                    this.variantList.add(variant!!)
                    variantAdapter.notifyItemInserted(this.variantList.size)
                }
                Activities.VariantHostActivity.CRUD_ACTION.EDIT->{
                    this.variantList.indexOfFirst { it.id == variant!!.id }.takeIf { it!=-1 }?.let {  index->
                        this.variantList[index] = variant!!
                        this.variantAdapter.notifyItemChanged(index)
                    }
                }
                Activities.VariantHostActivity.CRUD_ACTION.DELETE->{
                    this.variantList.indexOfFirst { it.id == variant!!.id }.takeIf { it!=-1 }?.let {  index->
                        this.variantList.removeAt(index)
                        this.variantAdapter.notifyItemRemoved(index)
                    }
                }
            }
        }
        else if (requestCode == ActivityRequestCode.EventTimeHostActivity && resultCode == Activity.RESULT_OK && data != null){
            eventFromMillis = data.getLongExtra(Activities.EventTimeHostActivity.EXTRAS_FROM_MILLIS,0)
            eventToMillis = data.getLongExtra(Activities.EventTimeHostActivity.EXTRAS_TO_MILLIS,0)
            populateEventTime()
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PermissionHelper.RESULT_CODE_READ_STORAGE) {
            if (grantResults.count() > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                FileHelper.openGallery(this)
            } else {
                Toast.makeText(this, getString(R.string.app_permission_gallery), Toast.LENGTH_SHORT)
                    .show()
            }
        } else if (requestCode == PermissionHelper.RESULT_CODE_CAMERA) {
            if (grantResults.count() > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    if (grantResults[1] != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, getString(R.string.app_permission_camera), Toast.LENGTH_SHORT).show()
                        return
                    }
                }
                FileHelper.openCamera(this)
            } else {
                Toast.makeText(this, getString(R.string.app_permission_camera), Toast.LENGTH_SHORT)
                    .show()
            }
        }
        else if (requestCode == PermissionHelper.RESULT_CODE_LOCATION) {
            if (grantResults.count() > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                checkSettings()
            } else {
                showToast(R.string.app_need_location_permission)
            }
        }
    }

    private fun addTags(list: List<String>){
        if(AppConfigHelper.getConfigKey(AppConfigHelper.Keys.HIDE_LISTING_TAGS,false)){
            tagList.clear()
            tagList.addAll(list)
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
                chip.setOnClickListener { showTagActivity() }
                chipGroup.addView(chip)
            }
            if (tagList.count() > 0) {
                addTagIcon.visibility = View.GONE
                chipGroup.visibility = View.VISIBLE
                hashDivider?.visibility = View.VISIBLE
            } else {
                addTagIcon.visibility = View.VISIBLE
                chipGroup.visibility = View.GONE
                hashDivider?.visibility = View.GONE
            }
        }
    }

    private fun addTags(listString: String?) {
        listString?.let {
            addTags(it.toList<String>())
        }
    }

    private fun addGroups(listString: String?) {
        /*val baseType = object : TypeToken<List<Group>>() {
        }.type
        groupList.clear()
        groupList.addAll(Gson().fromJson(listString, baseType))
        groupChip.removeAllViews()
        for (item in groupList) {
            val chip = Chip(this)
            chip.isCheckable = false
            chip.isCloseIconVisible = false
            chip.chipIcon = ContextCompat.getDrawable(this, R.drawable.ic_group_black_24dp)
            chip.chipIconTint = resources.getColorStateList(R.color.colorBlueLight)
            chip.layoutDirection = View.LAYOUT_DIRECTION_LOCALE
            chip.isCloseIconEnabled = false
            chip.chipBackgroundColor = resources.getColorStateList(R.color.colorWhiteLight)
            chip.setTextColor(resources.getColorStateList(R.color.colorBlueLight))
            chip.text = item.groupName
            groupChip.addView(chip)
        }
        if (groupList.count() > 0) {
            groupDivider?.visibility = View.VISIBLE
        } else {
            groupDivider?.visibility = View.GONE
        }*/
    }

    private fun showTagActivity() {
        val intent = Intent(this, TagActivity::class.java)
        intent.putExtra("list", tagList.toJson<List<String>>())
        startActivityForResult(intent, REQUEST_CODE.TAG_RESULT)
    }

    override fun onBackPressed() {
        if (isVariantListChanged){
            setResult(Activity.RESULT_OK, Intent())
            finish()
        }
        else{
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        addProductPresenter.cancelRequest()
    }
}
