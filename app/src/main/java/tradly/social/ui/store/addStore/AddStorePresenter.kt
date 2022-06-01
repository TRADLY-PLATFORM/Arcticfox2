package tradly.social.ui.store.addStore

import kotlinx.coroutines.*
import tradly.social.R
import tradly.social.common.*
import tradly.social.common.base.*
import tradly.social.common.cache.AppCache
import tradly.social.common.network.NetworkConstant
import tradly.social.common.network.feature.common.datasource.CategoryDataSourceImpl
import tradly.social.common.network.feature.common.datasource.ParseMediaDataSource
import tradly.social.data.model.dataSource.ParseShippingAddressDataSource
import tradly.social.common.network.feature.common.datasource.StoreDataSourceImpl
import tradly.social.domain.entities.*
import tradly.social.domain.repository.CategoryRepository
import tradly.social.domain.repository.MediaRepository
import tradly.social.domain.repository.ShippingAddressRepository
import tradly.social.domain.repository.StoreRepository
import tradly.social.domain.usecases.*
import tradly.social.ui.base.BaseView
import tradly.social.domain.entities.ProductAttribute
import java.io.File

class AddStorePresenter(private var view: View? = null) {
    private var addStore: AddStore
    private var getStore: GetStores? = null
    private var uploadMedia: UploadMedia? = null
    private var getCategories: GetCategories
    private var manageShippingAddress: ManageShippingAddress
    private var coroutineScope: CoroutineScope
    private var shouldCreateBranchUrl = false

    interface View : BaseView {
        fun noInternet()
        fun inputError(id: Int, msg: Int = 0, any: String = Constant.EMPTY)
        fun onSuccess(store: Store)
        fun onFailure(appError: AppError)
        fun onLoadStore(store: Store)
        fun onLoadCategories(list: List<Category>)
        fun onMediaUploadFailed(msg: Int)
        fun onLoadShippingMethod(list: List<ShippingMethod>)
        fun onLoadAttribute(attributeList: List<Attribute>)
        fun onShowLinkStatus(success:Boolean)
    }

    init {
        val parseStoreDataSource = StoreDataSourceImpl()
        val storeRepository = StoreRepository(parseStoreDataSource)
        val mediaDataSource = ParseMediaDataSource()
        val categoryDataSource = CategoryDataSourceImpl()
        val categoryRepository = CategoryRepository(categoryDataSource)
        val mediaRepository = MediaRepository(mediaDataSource)
        val shippingAddressDataSource = ParseShippingAddressDataSource()
        val shippingAddressRepository = ShippingAddressRepository(shippingAddressDataSource)
        manageShippingAddress = ManageShippingAddress(shippingAddressRepository)
        getCategories = GetCategories(categoryRepository)
        uploadMedia = UploadMedia(mediaRepository)
        addStore = AddStore(storeRepository)
        getStore = GetStores(storeRepository)
        coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    }

    fun addStoreDetails(
        isFor: Boolean,
        accountId: String,
        categoryId: String,
        storeName: String,
        webAddress: String,
        geoPoint: GeoPoint?,
        storeDescription: String,
        storeLinkName:String,
        filePath: String,
        attributeList: List<ProductAttribute>?,
        selectedShipments: List<Int>
    ) {
        if (isValid(storeName, webAddress,geoPoint, storeDescription,storeLinkName, attributeList, selectedShipments)) {
            if (NetworkUtil.isConnectingToInternet()) {
                shouldCreateBranchUrl = false
                view?.showProgressDialog(R.string.please_wait)
                coroutineScope.launch(Dispatchers.Main) {
                    val shouldCheckLinkName = AppConfigHelper.getConfigKey(AppConfigHelper.Keys.ENABLE_BRANCH_UNIQUE_LINK) && AppController.appController.getUserStore()?.uniqueName.isNullOrEmpty()
                    if(shouldCheckLinkName){
                        val result = async(Dispatchers.IO){ getStore?.getStores(pagination = 1,key = storeLinkName) }.await()
                        when(result){
                            is Result.Success->{
                                view?.onShowLinkStatus(result.data.isEmpty())
                                shouldCreateBranchUrl = result.data.isEmpty()
                                if(!shouldCreateBranchUrl){
                                    view?.hideProgressDialog()
                                    return@launch
                                }
                            }
                            is Result.Error->{
                                view?.onFailure(result.exception)
                                view?.hideProgressDialog()
                                return@launch
                            }
                        }
                    }
                    if (filePath.startsWith("http")) {
                        addStore(isFor, accountId, categoryId, storeName, webAddress, geoPoint, storeDescription, if(shouldCheckLinkName)storeLinkName else AppConstant.EMPTY,filePath, attributeList, selectedShipments)
                    } else {
                        val uploadMediaCall = async(Dispatchers.IO) { uploadMedia?.invoke(listOf(FileInfo(fileUri = filePath, type = FileHelper.getMimeType(filePath), name = File(filePath).name)), true) }
                        when (val uploadResult = uploadMediaCall.await()) {
                            is Result.Success -> addStore(isFor, accountId, categoryId, storeName, webAddress, geoPoint, storeDescription,if(shouldCheckLinkName)storeLinkName else AppConstant.EMPTY, uploadResult.data[0].uploadedUrl, attributeList, selectedShipments)
                            is Result.Error -> {
                                view?.hideProgressDialog()
                                view?.onMediaUploadFailed(R.string.media_failed_msg)
                            }
                        }
                    }

                }
            } else {
                view?.noInternet()
            }
        }
    }

    private fun addStore(
        isFor: Boolean,
        accountId: String,
        categoryId: String,
        storeName: String,
        webAddress: String,
        geoPoint: GeoPoint?,
        desc: String,
        storeLinkName:String,
        storePic: String,
        attributeList: List<ProductAttribute>?,
        selectedShipments: List<Int>
    ) {
        coroutineScope.launch(Dispatchers.Main) {
            attributeList?.find { it.fieldType== AppConstant.AttrTypes.UPLOAD_FIELD }?.let {
                if(it.selectedValues.isNotEmpty()){
                    val fileInfo = it.selectedValues[0].any as FileInfo
                    if(!fileInfo.fileUri.isNullOrEmpty() && !fileInfo.fileUri!!.startsWith("http")){
                        val filePath = fileInfo.fileUri
                        val fileUploadCall = async(Dispatchers.IO){ uploadMedia?.invoke(listOf(FileInfo(fileUri = filePath, type = FileHelper.getMimeType(filePath), name = File(filePath).name)),false) }
                        when(val fileUploadResult = fileUploadCall.await()){
                            is Result.Success-> {
                                attributeList.find { it.fieldType == AppConstant.AttrTypes.UPLOAD_FIELD}.let {
                                    (it!!.selectedValues[0].any as FileInfo).uploadedUrl = fileUploadResult.data[0].uploadedUrl
                                }
                            }
                            is Result.Error->{
                                view?.onFailure(fileUploadResult.exception)
                                view?.hideProgressDialog()
                                return@launch
                            }
                        }
                    }
                }
            }
            val call = async(Dispatchers.IO) { addStore.invoke(isFor, accountId, categoryId, storeName, webAddress, geoPoint, desc,storeLinkName, storePic, AttributeHelper.getAttributeValues(attributeList), selectedShipments) }
            when (val result = call.await()) {
                is Result.Success ->{
                    val store = result.data
                    BranchHelper.getBranchStoreSharingUrl(AppController.appContext,store.id,storeName,storeLinkName,storePic,desc){ url, errorCode ->
                        view?.hideProgressDialog()
                        view?.onSuccess(result.data)
                    }
                }
                is Result.Error -> {
                    view?.onFailure(result.exception)
                    view?.hideProgressDialog()
                }
            }
        }
    }

    fun syncUserStore() {
        GlobalScope.launch(Dispatchers.IO) { getStore?.syncUserStore(AppCache.getCacheUser()?.id) }
    }

    fun getStoreData(id: String) {
        coroutineScope.launch(Dispatchers.Main) {
            view?.showProgressDialog(R.string.please_wait)
            val storeResult = async(Dispatchers.IO) { getStore?.getStore(id) }.await()
            val categoryCall= async(Dispatchers.IO){ getCategories.getCategories(type = NetworkConstant.Param.ACCOUNT) }
            val shippingMethodCall = async(Dispatchers.IO) { manageShippingAddress.getShippingMethods() }
            when (storeResult) {
                is Result.Success -> {
                    view?.onLoadStore(storeResult.data)
                    handleCategoryResult(categoryCall.await())
                    getAttributes(storeResult.data.categoryId, false)
                    handleShippingMethodResult(shippingMethodCall.await())
                }
                is Result.Error -> view?.onFailure(storeResult.exception)
            }
            view?.hideProgressDialog()
        }
    }

    fun getAttributes(categoryId: String, showProgress: Boolean) {
        if (NetworkUtil.isConnectingToInternet()) {
            if (showProgress) {
                view?.showProgressDialog(R.string.please_wait)
            }
            coroutineScope.launch(Dispatchers.Main) {
                val result = async(Dispatchers.IO) { addStore.getAttribute(categoryId) }.await()
                when (result) {
                    is Result.Success -> view?.onLoadAttribute(result.data)
                    is Result.Error -> view?.onFailure(result.exception)
                }
                view?.hideProgressDialog()
            }
        }
    }

    fun loadStoreData() {
        coroutineScope.launch {
            view?.showProgressDialog(R.string.please_wait)
            val categoryCall = async(Dispatchers.IO) { getCategories.getCategories(type = NetworkConstant.Param.ACCOUNT) }
            if(AppConfigHelper.getConfigKey(AppConfigHelper.Keys.SHIPPING_METHOD_PREFERENCE)){
                val shippingMethodCall = async(Dispatchers.IO) { manageShippingAddress.getShippingMethods() }
                val shippingMethodResult = shippingMethodCall.await()
                handleShippingMethodResult(shippingMethodResult)
            }
            val categoryResult = categoryCall.await()
            handleCategoryResult(categoryResult)
            when(categoryResult){
                is Result.Success->{
                    if(categoryResult.data.isNotEmpty()){
                        val attributeResult = async(Dispatchers.IO) { addStore.getAttribute(categoryResult.data[0].id) }
                        handleAttributeResult(attributeResult.await())
                    }
                }
            }
            view?.hideProgressDialog()
        }
    }

    fun getCategory(){
        coroutineScope.launch {
            val categoryCall = async(Dispatchers.IO) { getCategories.getCategories(type = NetworkConstant.Param.ACCOUNT) }
            handleCategoryResult(categoryCall.await())
        }
    }
    private fun handleShippingMethodResult(result:Result<List<ShippingMethod>>){
        when(result){
            is Result.Success -> view?.onLoadShippingMethod(result.data)
            is Result.Error -> view?.onFailure(result.exception)
        }
    }

    private fun handleCategoryResult(result:Result<List<Category>>){
        when(result){
            is Result.Success->view?.onLoadCategories(result.data)
            is Result.Error -> view?.onFailure(result.exception)
        }
    }

    private fun handleAttributeResult(result: Result<List<Attribute>>){
        when(result){
            is Result.Success->view?.onLoadAttribute(result.data)
            is Result.Error -> view?.onFailure(result.exception)
        }
    }

    private fun isValid(
        name: String?,
        webAddress: String?,
        geoPoint: GeoPoint?,
        desc: String?,
        storeLinkName: String,
        attributeList: List<ProductAttribute>?,
        selectedShipments: List<Int>
    ): Boolean {
        var isValid = true
        if (name.isNullOrEmpty()) {
            isValid = false
            view?.inputError(R.id.edStoreName, R.string.addstore_required)
        } else if (!webAddress.isNullOrEmpty() && !Utils.isValidUrl(webAddress)) {
            isValid = false
            view?.inputError(R.id.edStoreWebAddress, R.string.addstore_invalid_url)
        }
        if (AppConfigHelper.getConfigKey(AppConfigHelper.Keys.SHIPPING_METHOD_PREFERENCE)) {
            if (selectedShipments.isEmpty()) {
                isValid = false
                view?.inputError(0, R.string.shipment_alert_message_choose_shipment)
            }
        }
        if(AppConfigHelper.getConfigKey(AppConfigHelper.Keys.ENABLE_BRANCH_UNIQUE_LINK)){
            if(storeLinkName.isEmpty()){
                isValid = false
                view?.inputError(R.id.storeLinkInputEditText, R.string.addstore_required)
            }
        }
        if(AppConfigHelper.getConfigKey(AppConfigHelper.Keys.ACCOUNT_ADDRESS_ENABLED) && geoPoint==null){
            isValid = false
            view?.inputError(R.id.addressLayout, R.string.addstore_alert_please_select_address)
        }
        AttributeHelper.validateAttribute(attributeList) { isValidAttribute, fieldName ->
            if (!isValidAttribute) {
                view?.inputError(
                    R.id.recyclerAttribute,
                    any = AppController.appContext.getTwoStringData(R.string.choose, fieldName)
                )
            }
            if (isValid) {
                isValid = isValidAttribute
            }
        }

        return isValid
    }

    fun onDestory() {
        view = null
        coroutineScope.coroutineContext.cancelChildren()
    }
}