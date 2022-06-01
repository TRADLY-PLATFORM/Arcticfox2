package tradly.social.ui.product.addProduct.productDetail

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import tradly.social.R
import tradly.social.adapter.ListingAdapter
import tradly.social.adapter.ProductSliderAdapter
import tradly.social.common.*
import tradly.social.domain.common.VariantParser
import tradly.social.domain.entities.*
import tradly.social.common.base.BaseActivity
import tradly.social.ui.groupListing.ListingActivity
import tradly.social.ui.login.AuthenticationActivity
import tradly.social.ui.message.thread.ChatThreadActivity
import tradly.social.ui.product.addProduct.AddProductActivity
import tradly.social.ui.store.storeDetail.StoreDetailActivity
import tradly.social.ui.variant.DialogVariant
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.chip.Chip
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_product_detail.*
import kotlinx.android.synthetic.main.activity_product_detail.btnActionOne
import kotlinx.android.synthetic.main.activity_product_detail.dots
import kotlinx.android.synthetic.main.activity_product_detail.progress_circular
import kotlinx.android.synthetic.main.activity_product_detail.txtActionOne
import kotlinx.android.synthetic.main.layout_reviews.*
import kotlinx.android.synthetic.main.list_item_category_path.view.*
import tradly.social.common.base.AppConfigHelper
import tradly.social.common.base.*
import tradly.social.common.network.CustomError
import tradly.social.common.util.parser.extension.toJson
import tradly.social.databinding.ActivityProductDetailBinding
import tradly.social.ui.cart.CartListActivity
import tradly.social.ui.category.CategoryListActivity
import tradly.social.ui.product.ImageViewerActivity
import tradly.social.common.cache.AppCache
import tradly.social.common.navigation.Activities
import tradly.social.common.navigation.common.NavigationIntent
import tradly.social.common.network.converters.ProductConverter
import kotlin.math.abs
typealias VariantAdapter = tradly.social.common.adapter.VariantAdapter

class ProductDetailActivity : BaseActivity(), ProductDetailPresenter.View, CustomOnClickListener.OnCustomClickListener,DialogListener,GenericAdapter.OnClickItemListener<Variant> {

    private lateinit var productDetailPresenter: ProductDetailPresenter
    private lateinit var binding: ActivityProductDetailBinding
    private lateinit var variantAdapter: VariantAdapter
    private var productSliderAdapter: ProductSliderAdapter? = null
    private var productImageList = mutableListOf<String>()
    private var listingAdapter: ListingAdapter? = null
    private lateinit var horZListingAdapter:ListingAdapter
    private var store: Store? = null
    private var product: Product? = null
    private var variantList = mutableListOf<Variant>()
    private var similarProductList = mutableListOf<Product>()
    private var isToolbarExpanded = false
    private lateinit var listingLocation:GeoPoint
    private lateinit var productId:String
    private var user:User? = AppController.appController.getUser()
    private val mLayoutInflater: LayoutInflater by lazy { LayoutInflater.from(this) }
    private val colorPrimary: Int by lazy { ThemeUtil.getResourceValue(this, R.attr.colorPrimary) }
    private val btnFollowBg: Int by lazy { ThemeUtil.getResourceDrawable(this, R.attr.buttonGradientBg) }
    private var isPaginationEnd:Boolean = false
    private var pagination:Int = 1
    private var productShareLink:String = AppConstant.EMPTY
    private var isEventModule = AppCache.getCacheAppConfig()!!.module == AppConstant.ConfigModuleType.EVENT

    companion object{
        private const val EXTRAS_LISTING_ID = "productId"
        fun getIntent(context:Context,id:String) = Intent(context,ProductDetailActivity::class.java).apply {
            putExtra(EXTRAS_LISTING_ID,id)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_product_detail)
        setToolbar(toolbar, R.string.empty, R.drawable.ic_back)
        setTransparentBar()
        productDetailPresenter = ProductDetailPresenter(this)
        initAdapters()
        productId = getIntentExtra(EXTRAS_LISTING_ID)
        topLayout.visibility = View.GONE
        nestedScrollView.visibility = View.GONE
        productDetailPresenter.getProduct(productId)

        with(CustomOnClickListener(this)){
            likeFab?.setOnClickListener(this)
            storeLayout?.setOnClickListener(this)
            btnActionOne?.setOnClickListener(this)
            readAllReviewsBtn?.setOnClickListener(this)
            icIconDirectionLayout?.setOnClickListener(this)
            btnNegotiate?.setOnClickListener(this)
            llWhatsAppShare.setOnClickListener(this)
            llCopy.setOnClickListener(this)
            ivShareIcon.setOnClickListener(this)
            btnChat.setOnClickListener(this)
            btnBuyNow.setOnClickListener(this)
            btnNegotiate.setOnClickListener(this)
            tvMarkAsSold.setOnClickListener(this)
        }


        appbar?.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            if (abs(verticalOffset) -appBarLayout.totalScrollRange == 0) {
                //  Collapsed
                isToolbarExpanded = false
                toolbar.setNavigationIcon(R.drawable.ic_back)
                invalidateOptionsMenu()

            } else {
                isToolbarExpanded = true
                toolbar.setNavigationIcon(R.drawable.ic_left_arrow_circle)
                invalidateOptionsMenu()
                //Expanded
            }
        })
    }

    private fun initAdapters(){
        productSliderAdapter = ProductSliderAdapter(this, productImageList){
            val bundle = Bundle().apply { putString(AppConstant.BundleProperty.IMAGES,product?.images?.toJson<List<String>>()) }
            startActivity(ImageViewerActivity::class.java,bundle)
        }
        variantRecyclerView?.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rvSimilarProduct.layoutManager = LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false)
        listingAdapter = ListingAdapter(this, variantList, AppConstant.ListingType.VARIANT, variantRecyclerView){ position, obj ->

        }
        horZListingAdapter = ListingAdapter(this,similarProductList,
            AppConstant.ListingType.SIMILAR_PRODUCT_LIST,rvSimilarProduct){ position, obj ->

        }
        horZListingAdapter.setOnLoadMoreListener(object : OnLoadMoreListener {
            override fun onLoadMore() {
                if(!isPaginationEnd && NetworkUtil.isConnectingToInternet(true)){
                    productDetailPresenter.getSimilarProduct(productId,pagination,true)
                }
            }
        })

        variantRecyclerView?.adapter = listingAdapter
        rvSimilarProduct.apply {
            adapter = horZListingAdapter
            isNestedScrollingEnabled = false
        }
        viewPager?.adapter = productSliderAdapter
    }

    override fun onCustomClick(view: View) {
       when(view.id){
           R.id.btnChat,R.id.btnBuyNow-> handleBottomButtons(view)
           R.id.btnNegotiate->showMakeOfferDialog()
           R.id.likeFab->handleFabLike()
           R.id.storeLayout->showStoreDetail()
           R.id.btnActionOne->handleStoreFollow()
           R.id.tvMarkAsSold-> productDetailPresenter.markAsSold(productId)
           R.id.llWhatsAppShare-> shareListing(
               BranchHelper.Channel.WHATS_APP_SHARE,
               BranchHelper.Feature.PRODUCT_SHARING,true)
           R.id.llCopy-> shareListing(
               BranchHelper.Channel.GENERAL_SHARE,
               BranchHelper.Feature.COPY_LINK,false)
           R.id.ivShareIcon-> shareListing(
               BranchHelper.Channel.GENERAL_SHARE,
               BranchHelper.Feature.PRODUCT_SHARING,true)
           R.id.readAllReviewsBtn->ViewUtil.showReviewList(this,getStringData(AppConstant.BundleProperty.PRODUCT_ID),
               AppConstant.ModuleType.LISTINGS)
           R.id.icIconDirectionLayout-> Utils.showGoogleMap(listingLocation.latitude,listingLocation.longitude)
       }
    }


    private fun setTransparentBar() {
        if (Build.VERSION.SDK_INT >= 19) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
            )
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            val params = toolbar.layoutParams as? CollapsingToolbarLayout.LayoutParams
            params?.setMargins(0, getStatusBarHeight(), 0, 0)
            toolbar.layoutParams = params
        }
    }

    private fun getStatusBarHeight(): Int {
        var result = 0
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId)
        }
        return result
    }

    private fun handleFabLike(){
        if(NetworkUtil.isConnectingToInternet()){
            if(AppController.appController.getUser() != null){
                product?.let {
                    it.isLiked = !it.isLiked
                    setLikeInfo(it)
                    productDetailPresenter.likeListing(it.id,it.isLiked)
                    showToast(if(it.isLiked)R.string.wishlist_item_added_to_wish_list else R.string.wishlist_item_deleted_to_wish_list)
                }
            }
            else{
                startActivity(AuthenticationActivity::class.java)
            }
        }
        else{
            showToast(R.string.no_internet)
        }
    }

    private fun showStoreDetail(){
        val intent = Intent(this, StoreDetailActivity::class.java)
        intent.putExtra("storeName", store?.storeName)
        intent.putExtra("id", store?.id)
        startActivity(intent)
    }

    private fun showChatRoom(){
        if(user != null){
            store?.let {
                it.user?.let {user->
                    productDetailPresenter.checkChatRoom(user ,it.storeName)
                }
            }
        }
        else{
            showLoginActivity(AuthenticationActivity::class.java,AppConstant.ActivityResultCode.CHAT_FROM_PRODUCT_DETAIL, AppConstant.LoginFor.CHAT)
        }
    }

    private fun handleStoreFollow(){
        if(AppController.appController.getUser()!= null){
            store?.followed?.let {
                if (it) {
                    store?.followed = !it
                    productDetailPresenter.unFollowStore(store?.id)
                } else {
                    store?.followed = !it
                    productDetailPresenter.followStore(store?.id)
                }
                btnActionOne?.setBackgroundResource(if (!it) R.drawable.bg_button_outline else btnFollowBg)
                txtActionOne?.text = getString(if (!it) R.string.product_following else R.string.product_follow)
                txtActionOne?.setTextColor(ContextCompat.getColor(this, if (!it) colorPrimary else R.color.colorWhite))
            }
        }
        else{
            showLoginActivity(AuthenticationActivity::class.java,
                AppConstant.ActivityResultCode.FOLLOW_LOGIN_FROM_PRODUCT_DETAIL,
                AppConstant.LoginFor.FOLLOW)
        }
    }

    private fun showMakeOfferDialog() {
        if (AppController.appController.getUser() == null) {
            showLoginActivity(AuthenticationActivity::class.java,AppConstant.ActivityResultCode.MAKE_OFFER_PRODUCT_DETAIL, AppConstant.LoginFor.CHAT)
        } else {
            val sheet = BottomChooserDialog()
            sheet.arguments = Bundle().apply {
                putDouble(BottomChooserDialog.ARG_PRODUCT_PRICE, product!!.listPrice!!.amount)
                putString(BottomChooserDialog.ARG_CURRENCY, product?.offerPrice?.currency)
            }
            sheet.setDialogType(BottomChooserDialog.DialogType.MAKE_OFFER)
            sheet.setListener(this)
            sheet.show(supportFragmentManager, null)
        }
    }

    private fun handleBottomButtons(view: View?) {
        when (view?.id) {
            R.id.btnChat -> showChatRoom()
            R.id.btnBuyNow -> {
                if (!isEventModule) {
                    if (product?.isProductInCart == true) {
                        startActivity(CartListActivity::class.java)
                    } else {
                        if (AppController.appController.getUser() != null) {
                            productDetailPresenter.addCartItem(productId)
                        } else {
                            showLoginActivity(AuthenticationActivity::class.java,
                                AppConstant.ActivityResultCode.ADD_TO_CART_FROM_PRODUCT_DETAIL,
                                AppConstant.LoginFor.ADD_CART
                            )
                        }
                    }
                } else {
                    if (AppController.appController.getUser()!=null){
                        showConformBooking()
                    }
                    else{
                        showLoginActivity(AuthenticationActivity::class.java,AppConstant.ActivityResultCode.ADD_TO_CART_FROM_PRODUCT_DETAIL, AppConstant.LoginFor.ADD_CART)
                    }
                }
            }
        }
    }

    private fun showConformBooking(){
        val bundle = Bundle().apply {
            putString(Activities.BookingHostActivity.EXTRAS_EVENT,ProductConverter.mapFrom(this@ProductDetailActivity.product!!).toJson<Event>())
        }
        val intent = NavigationIntent.getIntent(Activities.BookingHostActivity,bundle)
        startActivity(intent)
    }

    override fun onLoadProduct(product: Product) {
        this.product = product
        invalidateOptionsMenu()
        topLayout.visibility = View.VISIBLE
        nestedScrollView.visibility = View.VISIBLE
        populateStoreDetail(product.store)
        populateCategories(product.category?.hierarchy)
        productImageList.clear()
        productImageList.addAll(product.images)
        productSliderAdapter?.notifyDataSetChanged()
        dots?.attachViewPager(viewPager)
        setListingTitleInfo(product)
        setLikeInfo(product)
        setShareView()
        showAddress(product.address,product.location)
        setSpecification(product)
        setTags(product.tags)
        setEventVariant(product)
        loadAd()
        if(!product.description.isNullOrEmpty()){
            descLayout?.visibility = View.VISIBLE
            txtProductDesc?.text = product.description
        }
        if(productDetailPresenter.isListingNotApproved(product.status) || !product.store!!.active){
            setListingStatus(product)
            return
        }
        setBottomActions(product)
    }

    private fun loadAd(){
       /* if (AppConfigHelper.getConfigKey(AppConfigHelper.Keys.LISTINGS_DETAILS_ADMOB_ENABLED) && getString(R.string.admob_appid).isNotEmpty()){
            val adUnitId = AppConfigHelper.getConfigKey<String>(AppConfigHelper.Keys.LISTING_DETAIL_ADMOB_ID)
            if (adUnitId.isNotEmpty()){
                val adRequest = AdRequest.Builder().build()
                val adView = AdView(this)
                binding.adViewLayout.addView(adView)
                binding.adViewLayout.setVisible()
                adView.adUnitId = adUnitId
                adView.adSize = AdSize.MEDIUM_RECTANGLE
                adView.loadAd(adRequest)
            }
        }*/
    }

    override fun onLoadSimilarProducts(productList: List<Product>, isLoadMore: Boolean, pagination: Int) {
        if (productList.isNotEmpty()){
            this.pagination = pagination+1
            val lastIndex = similarProductList.size
            similarProductList.addAll(productList)
            horZListingAdapter.notifyItemRangeInserted(lastIndex,similarProductList.size)
            horZListingAdapter.setLoaded()
            llSimilarProduct.setVisible()
        }
        else{
            if(isLoadMore){
                isPaginationEnd = true
                horZListingAdapter.setLoaded()
            }
            else{
                isPaginationEnd = true
                llSimilarProduct.setGone()
            }
        }
    }

    fun showAddress(address: Address?,geoPoint: GeoPoint) {
        if(address != null){
            setLocationView(address,geoPoint)
        }
    }

    private fun setLikeInfo(product: Product){
        if(user!=null && user?.id==product.store?.user?.id){
            likeFab?.visibility = View.GONE
            return
        }
        likeFab?.visibility = View.VISIBLE
        likeFab?.setImageResource(if(product.isLiked) R.drawable.ic_favorite_black_24dp else R.drawable.ic_favorite_border_black_24dp)
        binding.tvLikeCount.apply {
            text = product.likes.toString()
            setVisible()
        }
    }

    private fun setListingStatus(product: Product){
        clProductStatus?.setVisible()
        tvProductStatus?.setVisible()
        tvProductStatus.setTextColor(ContextCompat.getColor(this,R.color.colorWhite))
        tvProductStatus.setBackgroundColor(ContextCompat.getColor(this,if(!product.store!!.active || product.status == AppConstant.ListingStatus.WAITING_FOR_APPROVAL)R.color.warning_Yellow else R.color.colorRed))
        val message = if(!product.store!!.active){
            R.string.storedetail_store_currently_unavailable
        }
        else{
            if(product.status == AppConstant.ListingStatus.WAITING_FOR_APPROVAL) R.string.product_waiting_for_approval else R.string.product_rejected
        }
        tvProductStatus.text = getString(message)
    }

    private fun setSets(product: Product){
        product.sets?.let {
            setLayout?.visibility = View.VISIBLE
            val set = it[0]
            var setImage:String
            if(set.variants[0].images.count()>0){
                setImage= set.variants[0].images[0]
            }
            else{
                setImage = product.images[0]
            }
            ImageHelper.getInstance().showImage(this,setImage,setImg,R.drawable.placeholder_image,R.drawable.placeholder_image)
            txtSetTitle?.text = set.title?:product.title
            setOffer?.text = String.format(getString(R.string.off_data),set.offerPercent.toString())
            //txtSetPrice?.text = String.format(getString(R.string.two_data), productDetailPresenter?.getUserCurrency(), set.offerPrice)
            setLayout?.setOnClickListener {
                val intent = Intent(this,ListingActivity::class.java)
                intent.putExtra("product",Gson().toJson(product))
                intent.putExtra("isFor", AppConstant.ListingType.SET_LIST)
                startActivity(intent)
            }
        }
    }
    private fun setVariant(product: Product){
        product.variants?.let { variantLayout?.visibility = View.VISIBLE }
        VariantParser.parseVariant(product.variants) { propertyCollection, variantCollection ->
            if (propertyCollection.count() == 1) {
                variantRecyclerView?.visibility = View.VISIBLE
                variantList.clear()
                variantList.addAll(propertyCollection)
                listingAdapter?.notifyDataSetChanged()
            } else {
                val currentVariant = variantCollection[0]
                currentVariant.let {
                    actionVariant?.visibility = View.VISIBLE
                    val mvarient = currentVariant["parent"] as Variant
                    if (mvarient.values.count() > 3) {
                        mvarient.values.subList(0, 3)
                    }
                    for (i in 0 until mvarient.values.count()) {
                        val properties = mvarient.values
                        var view: View? = null
                        view = mLayoutInflater.inflate(R.layout.variant_horz_item, null, false)
                        view?.findViewById<TextView>(R.id.vTypeValue)?.text = properties[i].variantValue
                        view?.findViewById<TextView>(R.id.vTypeName)?.text = properties[i].variantType
                        if (mvarient.values.count() - 1 == i) {
                            view?.findViewById<View>(R.id.horZDiv)?.visibility = View.GONE
                        }
                        val params = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT)
                        params.weight = 1f
                        view?.layoutParams = params
                        rlVariant?.addView(view)
                    }
                    actionVariant?.setOnClickListener {
                        val dialogVariant = DialogVariant()
                        val bundle = Bundle()
                        bundle.putString("product", Gson().toJson(product))
                        dialogVariant.arguments = bundle
                        dialogVariant.show(supportFragmentManager, AppConstant.FragmentTag.DIALOG_VARIANT)
                    }

                }
            }
        }
    }

    private fun setShareView(){
        clShare.setVisible()
        llWhatsAppShare.background = Utils.getDrawable(this,1,colorPrimary,radius = 6f)
        llCopy.background = Utils.getDrawable(this,1,colorPrimary,radius = 6f)
    }

    private fun setListingTitleInfo(product:Product){
        if (isEventModule){
            setEventTitleInfo(
                product.title,
                product.offerPercent,
                product.displayOffer,
                product.displayPrice,
                product.stock
            )
        }
        else{
            setProductTitle(product)
        }
    }

    private fun setEventTitleInfo(
        title: String,
        offerPercentage: Int,
        price: String,
        originalPrice: String,
        quantity:Int
    ) {
        binding.apply {
            binding.eventDetailPriceLayout.root.setVisible()
            binding.eventLocationDetail.root.setVisible()
            binding.setVariable(BR.product,this@ProductDetailActivity.product)
            setVariable(BR.title,title)
            setVariable(BR.rating,this@ProductDetailActivity.product!!.rating?.ratingAverage?.toFloat())
            setVariable(BR.ratingAvg,this@ProductDetailActivity.product!!.rating?.ratingAverage?.toFloat())
            setVariable(BR.ratingCount,this@ProductDetailActivity.product!!.rating?.ratingCount?.toString())
            setVariable(BR.imagePlaceholder,R.drawable.placeholder_image)
            setVariable(BR.imageError,R.drawable.placeholder_image)
            setVariable(BR.offerPercent,offerPercentage.toString())
            setVariable(BR.quantity,quantity.toString())
            setVariable(BR.price,price)
            setVariable(BR.dateFormat,DateTimeHelper.FORMAT_DATE_DD_MMM_YYYY)
            setVariable(BR.timeFormat,DateTimeHelper.FORMAT_TIME_AM_PM)
            this.executePendingBindings()
        }
    }

    private fun setProductTitle(product: Product) {
        priceLayout?.setVisible()
        productTitle?.text = product.title
        var offerPrice: String
        var listPrice: String
        var offerPercent: Int = 0
        val sets = product.sets
        val variants = product.variants
        if(sets != null && sets.isNotEmpty()){
            val set = sets[0]
            offerPercent = set.offerPercent
            offerPrice = set.offerPrice.toString()
            listPrice = set.listPrice.toString()
        }
        else if(variants !=null && variants.isNotEmpty()){
            val variant = variants[0]
            offerPercent = variant.offerPercent
            offerPrice = variant.offerDisplayPrice
            listPrice = variant.listDisplayPrice
        }
        else{
            offerPercent = product.offerPercent
            offerPrice = product.displayOffer
            listPrice = product.displayPrice
        }

        txtPriceOne?.text = listPrice
        if (product.negotiation == null) {
            if (offerPercent == 0) {
                txtPriceOne?.text = listPrice
                txtPriceTwo?.visibility = View.GONE
                txtOffer?.visibility = View.GONE
            } else {
                txtPriceTwo?.visibility = View.VISIBLE
                txtOffer?.visibility = View.VISIBLE
                txtPriceOne?.text = offerPrice
                txtPriceTwo?.text = listPrice
                txtPriceTwo.paintFlags = txtPriceTwo.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                txtOffer?.text = String.format(getString(R.string.off_data), offerPercent)
            }
        } else {
            txtPriceTwo?.visibility = View.VISIBLE
            txtOffer?.visibility = View.VISIBLE
            txtPriceOne?.text = offerPrice
            txtPriceTwo?.text = listPrice
            txtPriceTwo.paintFlags = txtPriceTwo.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            txtOffer?.text = getString(R.string.product_negotiated)
        }
    }

    private fun setTags(list: List<Tag>){
        if(list.isNotEmpty()){
            addDetailsLayout?.visibility = View.VISIBLE
            chipGroup?.removeAllViews()
            list.forEach {
                val chip = Chip(this)
                chip.isCheckable = true
                chip.isFocusable = true
                chip.isCheckedIconVisible = false
                chip.isCloseIconVisible = false
                chip.layoutDirection = View.LAYOUT_DIRECTION_LOCALE
                chip.isCloseIconEnabled = false
                chip.rippleColor = null
                chip.chipBackgroundColor = resources.getColorStateList(R.color.colorProductTag)
                chip.setTextColor(resources.getColorStateList(R.color.colorTextBlack))
                chip.text = it.name
                chipGroup?.addView(chip)
            }
        }
    }

    private fun setBottomActions(product: Product){
        product.store?.let { store->
            if(user != null && user?.id == store.user?.id ){
                if (!isEventModule){
                    clProductStatus?.setVisible()
                    if(product.inStock){
                        tvMarkAsSold.setVisible()
                        tvMarkAsSold.text = getString(R.string.product_mark_as_sold)
                        tvMarkAsSold.setTextColor(ContextCompat.getColor(this,colorPrimary))
                        tvMarkAsSold.setBackgroundColor(ContextCompat.getColor(this,R.color.colorWhite))
                    }
                    else{
                        tvMarkAsSold.setVisible()
                        tvMarkAsSold.text = getString(R.string.product_sold_out)
                        tvMarkAsSold.setTextColor(ContextCompat.getColor(this,R.color.colorWhite))
                        tvMarkAsSold.setBackgroundColor(ContextCompat.getColor(this,R.color.colorRed))
                        tvMarkAsSold.setOnClickListener(null)
                        tvMarkAsSold.isEnabled = false
                    }
                }
            }
            else{
                bottomLayout?.visibility = View.VISIBLE
                when(AppController.appController.getBusinessType()){
                    AppConstant.BusinessType.C2C->{
                        btnChat?.setVisible()
                    }
                    AppConstant.BusinessType.B2C->{
                        btnChat?.setVisible()
                        btnBuyNow?.setVisible()
                        btnBuyNow?.text = if (isEventModule){
                            getString(R.string.product_book_now)
                        }
                        else{
                         getString(if(product.isProductInCart)R.string.product_go_to_cart else R.string.product_add_to_cart)
                        }
                        if (!product.inStock){
                            btnBuyNow?.setOnClickListener(null)
                            btnBuyNow?.isEnabled = false
                            btnBuyNow?.backgroundTintList = ContextCompat.getColorStateList(this, R.color.colorMediumGrey)
                        }
                    }
                }
                btnNegotiate.setConfigVisibility(AppConfigHelper.Keys.ENABLE_NEGOTIATE)
                txtSoldOut?.visibility = if(product.inStock)View.GONE else View.VISIBLE
            }
        }
    }


    private fun populateCategories(list: List<CategoryHierarchy>?) {
        if (list != null) {
            clCategoriesGroup.setVisible()
            chipGroupCategories?.removeAllViews()
            if (list.size == 1) {
                val chipView =
                    LayoutInflater.from(this).inflate(R.layout.list_item_category_path, null, false)
                chipView.ivPathIcon.setGone()
                chipView.tvCategory.text = list[0].name
                chipView.setOnClickListener { showProductList(list[0])  }
                chipGroupCategories.addView(chipView)
            } else {
                for (index in list.indices) {
                    val categoryHierarchy = list[index]
                    val chipView = LayoutInflater.from(this)
                        .inflate(R.layout.list_item_category_path, null, false)
                    chipView.tvCategory.text = categoryHierarchy.name
                    if (index == list.size - 1) {
                        chipView.ivPathIcon.setGone()
                    }
                    chipView.setOnClickListener { showProductList(categoryHierarchy) }
                    chipGroupCategories.addView(chipView)
                }
            }
        }
    }

    private fun showProductList(categoryHierarchy:CategoryHierarchy){
        val arg = Bundle().apply {
            putString(AppConstant.BundleProperty.ISFOR, AppConstant.FragmentTag.PRODUCT_LIST_FRAGMENT)
            putString(AppConstant.BundleProperty.CATEGORY_NAME,categoryHierarchy.name)
            putString(AppConstant.BundleProperty.CATEGORY_ID,categoryHierarchy.id.toString())
        }
        startActivity(CategoryListActivity::class.java,arg)
    }

    private fun populateStoreDetail(store: Store?) {
        store?.let {
            this.store = store
            storeLayout?.visibility = View.VISIBLE
            shopName?.text = store.storeName
            if(it.webAddress.isNotEmpty()){
                txtShopWebAddress?.visibility = View.VISIBLE
                txtShopWebAddress?.text = store.webAddress
            }
            if(user?.id != it.user?.id){
                btnActionOne.setBackgroundResource(if (store.followed) R.drawable.bg_button_outline else btnFollowBg)
                txtActionOne?.text = getString(if (store.followed) R.string.product_following else R.string.product_follow)
                txtActionOne?.setTextColor(
                    ContextCompat.getColor(
                        this,
                        if (store.followed) colorPrimary else R.color.colorWhite
                    )
                )
            }
            else{
                btnActionOne.visibility = View.GONE
            }
            ImageHelper.getInstance().showImage(
                this,
                store.storePic,
                storeProfile,
                R.drawable.ic_store_placeholder,
                R.drawable.ic_store_placeholder
            )
        }
    }

    private fun setEventVariant(product:Product){
        if (isEventModule && product.variants.isNotEmpty()){
            variantAdapter = VariantAdapter()
            variantAdapter.onClickListener = this
            variantAdapter.items = product.variants.toMutableList().apply { this[0].isSelected = true }
            binding.rvVariantList.layoutManager = LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)
            binding.rvVariantList.adapter = variantAdapter
            binding.rvVariantList.setVisible()
            val variant = product.variants[0]
            onVariantSelected(variant)
        }
    }

    override fun onClick(value: Variant, view: View, itemPosition: Int) {
        setEventTitleInfo(value.variantName,value.offerPercent,value.offerDisplayPrice,value.listDisplayPrice,value.quantity)
        val index = variantAdapter.items.indexOfFirst { it.isSelected }
        if (index!=-1){
            variantAdapter.items[index].isSelected = false
            variantAdapter.notifyItemChanged(index)
        }
        value.isSelected = true
        variantAdapter.notifyItemChanged(itemPosition)
        onVariantSelected(value)
    }

    private fun onVariantSelected(variant: Variant){
        setEventTitleInfo(variant.variantName,variant.offerPercent,variant.offerDisplayPrice,variant.listDisplayPrice,variant.quantity)
        this.productImageList.clear()
        this.productImageList.addAll(variant.images)
        this.productSliderAdapter?.notifyDataSetChanged()
    }


    private fun setSpecification(product: Product){
        if(product.attributes.count()>0){
            specificationLayout?.visibility = View.VISIBLE
            specRecyclerView?.layoutManager = LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)
            val adapter = ListingAdapter(this,product.attributes,
                AppConstant.ListingType.SPECIFICATION,specRecyclerView){ position, obj ->  }
            specRecyclerView?.adapter = adapter
        }
    }

    private fun setLocationView(address: Address,geoPoint: GeoPoint){
        if(address.formattedAddress.isNotEmpty() && geoPoint.latitude!=0.0){
            this.listingLocation = geoPoint
            if (isEventModule){
                binding.eventLocationDetail.root.setVisible()
                binding.apply {
                    setVariable(BR.timeFormat,DateTimeHelper.FORMAT_TIME_AM_PM)
                    setVariable(BR.dateFormat,DateTimeHelper.FORMAT_DATE_EEE_D_MMM_YYYY)
                    setVariable(BR.product,this.product)
                    executePendingBindings()
                }
            }
            else{
                txtAddress?.text = address.formattedAddress
                locationLayoutProductDetail?.visibility = View.VISIBLE
                txtAddress?.safeClickListener {
                    Utils.showAlertDialog(
                        this, getString(R.string.product_location), address.formattedAddress,
                        isCancellable = true,
                        withCancel = false,
                        dialogInterface = null
                    )
                }
            }
        }
    }

    override fun onCartAdded() {
        btnBuyNow?.text = getString(R.string.product_go_to_cart)
        product?.isProductInCart = true
        showToast(getString(R.string.cart_item_added_cart))
    }

    override fun loadCurrency(currency: String?) {

    }

    override fun noInternet() {
        showToast(R.string.no_internet)
    }

    override fun onFailure(appError: AppError) {
        ErrorHandler.handleError(this, appError)
        if(appError.code== CustomError.MULTI_SELLER_CART_SUPPORT){
            Utils.showAlertDialog(this,
                AppConstant.EMPTY,getString(R.string.product_clear_cart_info),true,true,R.string.product_clear_cart,object :
                    Utils.DialogInterface{
                override fun onAccept() {
                    productDetailPresenter.clearCartItems(product?.id)
                }

                override fun onReject() {

                }
            })
        }
    }

    override fun showProgressLoader() {
        progress_circular?.visibility = View.VISIBLE
    }

    override fun hideProgressLoader() {
        progress_circular?.visibility = View.GONE
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        if(product !=null){
                if(user != null){
                    product?.store?.let {
                        if(user?.id == it.user?.id){
                            val editItem = menu?.findItem(R.id.action_edit)
                            val deleteItem = menu?.findItem(R.id.action_delete)
                            editItem?.isVisible = true
                            deleteItem?.isVisible = true
                        }
                    }
                }

                val shareItem = menu?.findItem(R.id.action_share)
                val shareItemOne = menu?.findItem(R.id.action_share_One)
                if(isToolbarExpanded){
                    shareItem?.isVisible = false
                    shareItemOne?.isVisible = true
                }
                else{
                    shareItem?.isVisible = true
                    shareItemOne?.isVisible = false
                }

        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_edit->{
                val intent = Intent(this, AddProductActivity::class.java)
                intent.putExtra("product",product?.toJson<Product>())
                intent.putExtra("isForEdit",true)
                startActivityForResult(intent, AppConstant.ActivityResultCode.REFRESH_PRODUCT_DETAIL)
            }
            R.id.action_delete->{
                Utils.showAlertDialog(this,getString(R.string.product_delete_product),getString(R.string.product_alert_product_delete),true,true,object :
                    Utils.DialogInterface{
                    override fun onAccept() {
                        product?.let { productDetailPresenter.deleteProduct(it.id) }
                    }
                    override fun onReject() {

                    }
                })

            }
            R.id.action_share,R.id.action_share_One-> shareListing(
                BranchHelper.Channel.GENERAL_SHARE,
                BranchHelper.Feature.PRODUCT_SHARING,true)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun shareListing(channel:String,feature:String,showProgress:Boolean){
        product?.let {
            val productId = it.id
            val title = it.title
            val desc =  String.format(getString(R.string.link_listing_desc),getString(R.string.app_name))
            val image = it.images[0]
            if (feature == BranchHelper.Feature.COPY_LINK && productShareLink.isNotEmpty()){
                onUrlGenerated(productId,channel,feature)
            }
            else{
                productDetailPresenter.getShareUrl(this,productId,title,desc,image,channel,feature,showProgress)
            }
        }
    }

    override fun onUrlGenerated(url: String, channel: String,feature: String) {
        if(!isFinishing){
            if (feature == BranchHelper.Feature.COPY_LINK){
                productShareLink = url
                ShareUtil.copyToClipBoard(this,url)
                showToast(R.string.product_link_copied)
            }
            else{
             if(channel== BranchHelper.Channel.GENERAL_SHARE){
                ShareUtil.showIntentChooser(this,url)
            }
            else if(channel== BranchHelper.Channel.WHATS_APP_SHARE){
                ShareUtil.openThirdPartyApp(this, ShareUtil.WHATSAPP_PCKG,url)
            }
            }
        }
    }

    override fun showProgressDialog() {
        showLoader(R.string.please_wait)
    }

    override fun hideProgressDialog() {
        hideLoader()
    }

    override fun networkError(msg: Int) {
        Toast.makeText(this, getString(msg), Toast.LENGTH_SHORT).show()
    }

    override fun showChatThread(chatRoom: ChatRoom?, provider: User) {
        chatRoom?.let {
            val intent = Intent(this, ChatThreadActivity::class.java)
            intent.putExtra("chatRoom", Gson().toJson(it))
            intent.putExtra("product", Gson().toJson(product))
            intent.putExtra("provider", Gson().toJson(provider))
            startActivity(intent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        user = AppController.appController.getUser()
        invalidateOptionsMenu()
        if(resultCode == Activity.RESULT_OK && data!= null){
            when(requestCode){
                AppConstant.ActivityResultCode.CHAT_FROM_PRODUCT_DETAIL->{
                    store?.let {
                        it.user?.let { provider->
                            productDetailPresenter.checkChatRoom(provider,it.storeName)
                        }
                    }
                }
                AppConstant.ActivityResultCode.FOLLOW_LOGIN_FROM_PRODUCT_DETAIL->productDetailPresenter.getProduct(getIntentExtra(EXTRAS_LISTING_ID))
                AppConstant.ActivityResultCode.ADD_TO_CART_FROM_PRODUCT_DETAIL->productDetailPresenter.addCartItem(product?.id)
                AppConstant.ActivityResultCode.REFRESH_PRODUCT_DETAIL->{
                    productDetailPresenter.getProduct(getStringData(AppConstant.BundleProperty.PRODUCT_ID),false)
                }
                AppConstant.ActivityResultCode.MAKE_OFFER_PRODUCT_DETAIL->showMakeOfferDialog()
            }
        }
    }

    override fun showReviewInfo(rating: Rating) {
        ViewUtil.showReviewInfo(false,reviewLayout,rating){ any ->
            val review = any as Review
            productDetailPresenter.likeReview(review.id.toString(),review.likeStatus)
        }
    }

    override fun showReviewProgress() {
        review_progress?.visibility = View.VISIBLE
    }

    override fun hideReviewProgress() {
        review_progress?.visibility = View.GONE
    }

    override fun productDeleted() {
        val intent = Intent()
        intent.putExtra("productId",product?.id)
        setResult(Activity.RESULT_OK,intent)
        finish()
    }

    override fun productMarkedAsSold() {
        tvMarkAsSold.setOnClickListener(null)
        tvMarkAsSold.text = getString(R.string.product_sold_out)
        tvMarkAsSold.setTextColor(ContextCompat.getColor(this,R.color.colorWhite))
        tvMarkAsSold.setBackgroundColor(ContextCompat.getColor(this,R.color.colorRed))
    }

    override fun onNegotiationInitiated(negotiationId:Int , negotiateAmount:String) {
        AppController.appController.getUser()?.let { user->
            store?.let { store ->
                productDetailPresenter.initiateChatNegotiation(store.user!!,store.storeName,product!!,negotiateAmount,negotiationId)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        productDetailPresenter.onDestroy()
    }

    override fun onClick(any: Any) {
        productDetailPresenter.getChatNegotiationId(productId,any as String)
    }
}

