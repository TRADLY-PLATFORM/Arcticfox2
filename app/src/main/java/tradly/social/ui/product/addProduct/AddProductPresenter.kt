package tradly.social.ui.product.addProduct

import tradly.social.R
import tradly.social.common.*
import tradly.social.domain.entities.ImageFeed
import tradly.social.data.model.dataSource.*
import tradly.social.domain.entities.*
import tradly.social.domain.repository.*
import tradly.social.domain.usecases.*
import tradly.social.domain.entities.ProductAttribute
import kotlinx.coroutines.*
import tradly.social.common.base.Utils
import tradly.social.common.base.*
import tradly.social.common.network.feature.common.datasource.CategoryDataSourceImpl
import tradly.social.common.network.feature.common.datasource.ParseMediaDataSource
import tradly.social.common.network.feature.common.datasource.ProductDataSourceImpl
import java.io.File
import kotlin.coroutines.CoroutineContext

class AddProductPresenter(private var view: View?) : CoroutineScope {
    private var job: Job
    private var searchTags: SearchTags? = null
    private var getCategories: GetCategories? = null
    private var getCountry: GetCountry? = null
    private var addProduct:AddProduct?= null
    private var getProduct:GetProduct
    private var uploadMedia:UploadMedia?=null
    private var getCurrency:GetCurrency
    private var manageShippingAddress: ManageShippingAddress? = null

    interface View {
        fun showTags(list: List<Tag>, keyword: String)
        fun networkError()
        fun onFailure(appError: AppError)
        fun loadCategories(list: List<Category>)
        fun loadCurrency(currencies:List<Currency>)
        fun onSuccess(id:String)
        fun onMediaUploadFailed(msg:Int)
        fun showFieldError(id:Int,msg:Any)
        fun loadAttributes(attributeList:List<Attribute>)
        fun showProgressDialog(msg:Int , title: Int = 0)
        fun changeProgressMessage(msg:Int , title: Int = 0)
        fun hideProgressDialog()
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    init {
        val parseCategoryDataSource = CategoryDataSourceImpl()
        val categoryRepository = CategoryRepository(parseCategoryDataSource)
        val parseTagDataSource = ParseTagDataSource()
        val tagRepository = TagRepository(parseTagDataSource)
        val parseCountryDataSource = ParseCountryDataSource()
        val countryRepository = CountryRepository(parseCountryDataSource)
        val parseProductDataSource = ProductDataSourceImpl()
        val productRepository = ProductRepository(parseProductDataSource)
        val currencyDataSource = ParseCurrencyDataSource()
        val currencyRepository = CurrencyRepository(currencyDataSource)
        val shippingAddressDataSource = ParseShippingAddressDataSource()
        val shippingAddressRepository = ShippingAddressRepository(shippingAddressDataSource)
        manageShippingAddress = ManageShippingAddress(shippingAddressRepository)
        getCurrency = GetCurrency(currencyRepository)
        addProduct = AddProduct(productRepository)
        getCountry = GetCountry(countryRepository)
        searchTags = SearchTags(tagRepository)
        getCategories = GetCategories(categoryRepository)
        getProduct = GetProduct(productRepository)
        val mediaDataSource = ParseMediaDataSource()
        val mediaRepository = MediaRepository(mediaDataSource)
        uploadMedia = UploadMedia(mediaRepository)
        job = Job()
    }

    fun loadCategories(shouldShowProgress: Boolean) {
        if (NetworkUtil.isConnectingToInternet()) {
            if(shouldShowProgress){
                view?.showProgressDialog(R.string.please_wait)
            }
            launch(Dispatchers.Main) {
                val categoryCall = async(Dispatchers.IO) { getCategories?.getCategories(type = AppConstant.CategoryType.LISTINGS) }
                when (val categoryResult = categoryCall.await()) {
                    is Result.Success -> view?.loadCategories(categoryResult.data)
                    is Result.Error -> {
                        view?.onFailure(categoryResult.exception)
                    }
                }
                view?.hideProgressDialog()
            }
        }
    }

    fun getAttribute(categoryId:String , shouldShowProgress:Boolean = false){
        if(shouldShowProgress){
            view?.showProgressDialog(R.string.please_wait)
        }
        launch(Dispatchers.Main){
            val attributeCall = async(Dispatchers.IO){getProduct.getAttributes(categoryId, Utils.getAppLocale())}
            when(val attributeResult = attributeCall.await()){
                is Result.Success->view?.loadAttributes(attributeResult.data)
                is Result.Error->view?.onFailure(attributeResult.exception)
            }
            view?.hideProgressDialog()
        }
    }

    fun addProduct(storeId:String?, productId:String?, title:String, currencyId:String?, price:String, offerPrice:String, cid:String, desc:String, maxQty:String, stock:String, shippingCharge:String, tagList:List<String>, attributeList:List<ProductAttribute>?, imageList:ArrayList<ImageFeed>, geoPoint: GeoPoint?, isForEdit:Boolean = false,variantList: List<Variant>,startTime:Long, endTime:Long){
        if(!isValid(title,currencyId,price,offerPrice,geoPoint,desc,maxQty,stock,cid,attributeList,imageList))return
        if(NetworkUtil.isConnectingToInternet()){
            launch(Dispatchers.Main) {
               attributeList?.find { it.fieldType == AppConstant.AttrTypes.UPLOAD_FIELD }?.let {  productAttribute ->
                   if (productAttribute.selectedValues.isNotEmpty()){
                       val fileInfo = productAttribute.selectedValues[0].any as FileInfo
                       if(fileInfo.fileUri.isNotNullOrEmpty() && !fileInfo.fileUri!!.startsWith("http")){
                           view?.showProgressDialog(R.string.addproduct_upload_image,R.string.please_wait)
                           val filePath = fileInfo.fileUri
                           val fileUploadResult = withContext(Dispatchers.IO){ uploadMedia?.invoke(listOf(FileInfo(fileUri = filePath, type = FileHelper.getMimeType(filePath), name = File(filePath).name)),false) }
                           when(fileUploadResult){
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
                val uploadFileInfoList = getFileInfoList(imageList)
                if(uploadFileInfoList.isNotEmpty()){
                    view?.showProgressDialog(R.string.addproduct_upload_image,R.string.please_wait)
                    val uploadCall = async(Dispatchers.IO) { uploadMedia?.invoke(uploadFileInfoList,true) }
                    when(val uploadResult = uploadCall.await()){
                        is Result.Success->{
                            view?.changeProgressMessage(R.string.addproduct_upload_listing)
                            val call = async(Dispatchers.IO) { addProduct?.addProduct(storeId,productId,title,currencyId,price,offerPrice,cid,desc,maxQty,stock,shippingCharge,tagList,getImageUrlList(imageList,uploadResult.data),AttributeHelper.getAttributeValues(attributeList),geoPoint,isForEdit,variantList,startTime,endTime) }
                            populateResult(call.await())
                        }
                        is Result.Error->{view?.onMediaUploadFailed(R.string.media_failed_msg)}
                    }
                    view?.hideProgressDialog()
                }
                else{
                    view?.showProgressDialog(R.string.addproduct_upload_listing,R.string.please_wait)
                    val call = async(Dispatchers.IO) { addProduct?.addProduct(storeId,productId,title,currencyId,price,offerPrice,cid,desc,maxQty,stock,shippingCharge,tagList,getImageUrlList(imageList),AttributeHelper.getAttributeValues(attributeList),geoPoint,isForEdit,variantList,startTime,endTime) }
                    populateResult(call.await())
                    view?.hideProgressDialog()
                }
            }
        }
    }

    private fun populateResult(result:Result<Product>?){
        when(result){
            is Result.Success->{view?.onSuccess(result.data.id)}
            is Result.Error->view?.onFailure(result.exception)
        }
    }

    fun loadCurrency() {
        launch(Dispatchers.Main){
            val call = async(Dispatchers.IO){ getCurrency.getCurrencies() }
            when(val result = call.await()){
                is Result.Success->view?.loadCurrency(result.data)
                is Result.Error->view?.onFailure(result.exception)
            }
        }
    }

    private fun getFileInfoList(list:ArrayList<ImageFeed>):List<FileInfo>{
        val mList = mutableListOf<FileInfo>()
        for(item in list){
            val filePath = item.filePath
            if(!filePath.startsWith("http")){
                mList.add(FileInfo(fileUri = filePath, name = File(filePath).name,type = FileHelper.getMimeType(filePath)))
            }
        }
        return mList
    }

    private fun getImageUrlList(list:List<ImageFeed>, uploadedList: List<FileInfo> = listOf()):List<String?>{
        val mList = mutableListOf<String?>()
        list.forEach {
            if(it.filePath.startsWith("http")){
                mList.add(it.filePath)
            }
        }
        for(item in uploadedList){
            mList.add(item.uploadedUrl)
        }
        return mList
    }

    private fun isValid(title:String, currencyId: String?, price:String, offerPrice:String, geoPoint: GeoPoint?, desc:String, maxQty:String, stock: String, cid:String, attributeList:List<ProductAttribute>?, list:ArrayList<ImageFeed>):Boolean{
        var isValid = true
        if(title.isNullOrEmpty()){
            isValid = false
            view?.showFieldError(R.id.edProductTitle,R.string.login_required)
        }
        else if(currencyId.isNullOrEmpty()){
            isValid = false
            with(AppController.appContext){
                view?.showFieldError(R.id.txtCurrency,getTwoStringData(R.string.choose,R.string.currency))
            }
        }
        else if(cid.isEmpty()){
            isValid = false
            view?.showFieldError(R.id.categoryLayout,R.string.product_choose_category)
        }
        else if(price.isEmpty() || price.toFloat()< AppConfigHelper.getConfigKey<Int>(AppConfigHelper.Keys.LISTING_MIN_PRICE)){
            isValid = false
            view?.showFieldError(R.id.edPrice,R.string.addproduct_min_price_error)
        }
        if(list.count()<1){
            view?.showFieldError(R.id.recycler_view,R.string.addproduct_alert_image_select_add_product)
            isValid = false
        }

        if(AppConfigHelper.getConfigKey(AppConfigHelper.Keys.LISTING_ADDRESS_ENABLED) && geoPoint==null){
            isValid = false
            view?.showFieldError(R.id.addAddressLayout,R.string.addstore_alert_please_select_address)
        }
        if(AppConfigHelper.getConfigKey(AppConfigHelper.Keys.SHOW_MAX_QUANTITY)){
            if(maxQty.isEmpty() || maxQty.toInt()==0){
                isValid = false
                view?.showFieldError(R.id.edMaxQty,R.string.addproduct_alert_message_max_atleast_one)
            }
        }

        if(AppConfigHelper.getConfigKey(AppConfigHelper.Keys.STOCK_ENABLED)){
            if(stock.isEmpty()){
                isValid = false
                view?.showFieldError(R.id.edStock,R.string.addproduct_required)
            }
        }
        if(attributeList != null && attributeList.isNotEmpty()){
            AttributeHelper.validateAttribute(attributeList){ isValidAttribute, fieldName ->
                if(!isValidAttribute){
                    view?.showFieldError(R.id.recyclerAttribute,AppController.appContext.getTwoStringData(R.string.choose,fieldName))
                }
                if(isValid){
                    isValid = isValidAttribute
                }
            }
        }
        return isValid
    }

    fun cancelRequest() {
        job.cancel()
        view = null
    }


}