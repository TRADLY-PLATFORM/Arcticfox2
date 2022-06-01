package tradly.social.ui.store.storeDetail

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import tradly.social.R
import tradly.social.adapter.ListingProductAdapter
import tradly.social.common.*
import tradly.social.common.base.BaseActivity
import tradly.social.ui.login.AuthenticationActivity
import tradly.social.ui.product.addProduct.AddProductActivity
import kotlinx.android.synthetic.main.activity_store_detail.*
import kotlinx.android.synthetic.main.activity_store_detail.progress_circular
import kotlinx.android.synthetic.main.layout_reviews.*
import tradly.social.adapter.TabViewAdapter
import tradly.social.common.base.AppConfigHelper
import tradly.social.common.base.*
import tradly.social.common.subscription.InAppSubscriptionActivity
import tradly.social.common.util.parser.extension.toJson
import tradly.social.common.util.parser.extension.toList
import tradly.social.domain.entities.ImageFeed
import tradly.social.domain.entities.*
import tradly.social.ui.base.BaseHostActivity
import tradly.social.ui.message.thread.ChatThreadActivity
import tradly.social.ui.product.ImageViewerActivity
import tradly.social.ui.product.addProduct.productList.ListingFragment
import tradly.social.ui.store.addStore.AddStoreDetailActivity

class StoreDetailActivity : BaseActivity(), StorePresenter.View, CustomOnClickListener.OnCustomClickListener {

    private lateinit var storePresenter: StorePresenter
    var pagination: Int = 1
    var isLoading: Boolean = true
    var isUserStore:Boolean = false
    var bottomChooserDialog:BottomChooserDialog?=null
    internal var isPaginationEnd: Boolean = false
    private var productList = mutableListOf<Product>()
    private val colorPrimary: Int by lazy { ThemeUtil.getResourceValue(this, R.attr.colorPrimary) }
    private var listingProductAdapter: ListingProductAdapter? = null
    private var store:Store?=null
    private var userStore:Store?=null
    private var storeId:String?=null
    private var isStoreLoaded = false
    private lateinit var btnOutlinedBg:Drawable
    private lateinit var outlineFollowingBg:Drawable
    private lateinit var btnFilledBg:Drawable
    private lateinit var outlineFollowBg:Drawable
    private lateinit var outlineYellowBg:Drawable
    private lateinit var storeAboutFragment: StoreAboutFragment
    private lateinit var listingFragment: ListingFragment

    companion object{
        const val REQUEST_CODE = 110
        private const val EXTRAS_STORE_ID = "id"
        private const val EXTRAS_STORE_NAME = "storeName"

        fun newIntent(context: Context,storeId:String,storeName:String?) = Intent(context,StoreDetailActivity::class.java).apply {
            putExtra(EXTRAS_STORE_ID,storeId)
            putExtra(EXTRAS_STORE_NAME,storeName)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_store_detail)
        setToolbar(toolbar, backNavIcon = R.drawable.ic_back)
        supportActionBar?.title = intent?.getStringExtra(EXTRAS_STORE_NAME) ?: getString(R.string.storedetail_header_title)
        storePresenter = StorePresenter(this)
        userStore = AppController.appController.getUserStore()
        storeId = intent.getStringExtra(EXTRAS_STORE_ID)
        initView()
        initList()
        showProgressLoader()
        storePresenter.getStore(isUserStore,storeId)
        initTabs()
    }

    private fun initView(){
        val outLineBackground = Utils.getDrawable(this,1,colorPrimary,radius = 8f)
        val yellowOutLineBackground = Utils.getDrawable(this,1,R.color.colorYellow,radius = 8f)
        val filledBackground = Utils.getDrawable(this,1,colorPrimary,colorPrimary,radius = 8f)
        outlineFollowBg = Utils.getRippleDrawable(this,1,colorPrimary,radius = 8f,pressedColor = ContextCompat.getColor(this,R.color.defaultRippleGreen))
        outlineFollowingBg = Utils.getRippleDrawable(this,1,colorPrimary,radius = 8f,pressedColor = ContextCompat.getColor(this,R.color.defaultRippleGreen))
        btnOutlinedBg = Utils.getRippleDrawable(ContextCompat.getColor(this,R.color.defaultRippleGreen),outLineBackground)
        btnFilledBg = Utils.getRippleDrawable(ContextCompat.getColor(this,R.color.defaultRippleGreen),filledBackground)
        outlineYellowBg = Utils.getRippleDrawable(ContextCompat.getColor(this,R.color.defaultRippleGreen),yellowOutLineBackground)
        isUserStore = if(userStore != null) (userStore?.id == storeId) else false

        CustomOnClickListener(this).also {
            addFab?.setOnClickListener(it)
            storeProfile?.setOnClickListener(it)
            txtWriteReview?.setOnClickListener(it)
            txtFollowersTitle?.setOnClickListener(it)
            txtFollowValue?.setOnClickListener(it)
        }

        swipeRefresh?.setOnRefreshListener {
            swipeRefresh?.isRefreshing = true
            storePresenter.getStore(isUserStore,storeId)
        }
    }

    private fun initTabs(){
        storeAboutFragment = StoreAboutFragment()
        val extras = Bundle().apply { putString(AppConstant.BundleProperty.STORE_ID,storeId) }
        listingFragment = ListingFragment.newInstance(AppConstant.ListingType.STORE_PRODUCT_LIST,extras)
        val adapter = TabViewAdapter(supportFragmentManager, listOf(getString(R.string.storedetail_listings),getString(R.string.storedetail_about)), listOf(listingFragment,storeAboutFragment))
        viewPager.adapter = adapter
        tabLayout.setupWithViewPager(viewPager)
    }

    override fun onCustomClick(view: View) {
        when(view.id){
            R.id.addFab->checkAndOpenAddProduct()
            R.id.storeProfile->{
                this.store?.let {
                    val bundle = Bundle()
                    bundle.putString(AppConstant.BundleProperty.IMAGES, listOf(it.storePic).toJson<List<String>>())
                    startActivity(ImageViewerActivity::class.java,bundle)
                }
            }
            R.id.txtFollowersTitle,
            R.id. txtFollowValue->{
                val intent = Intent(this,BaseHostActivity::class.java)
                intent.putExtra(AppConstant.BundleProperty.TITLE,getString(R.string.store_followers_title))
                intent.putExtra(AppConstant.BundleProperty.FRAGMENT_TAG,AppConstant.FragmentTag.ACCOUNT_FOLLOWERS_FRAGMENT)
                intent.putExtra(AppConstant.BundleProperty.ACCOUNT_ID,store!!.id)
                startActivity(intent)
            }
        }
    }

    override fun onSuccess(resultStore: Store) {
        hideProgressLoader()
        swipeRefresh?.isRefreshing = false
        app_bar?.visibility = View.VISIBLE
        this.store = resultStore
        userStore = AppController.appController.getUserStore()
        populateHeader(resultStore)
        storeAboutFragment.setStoreDetail(resultStore)
        storePresenter.getStoreReviews(resultStore.id)
        if(isUserStore && resultStore.status== AppConstant.StoreStatus.APPROVED)addFab?.show()
    }

    override fun loadStoreReview(rating: Rating) {
        storeAboutFragment.showStoreReviews(rating)
    }

    override fun onMailTriggerSuccess() {
        showToast("Mail Sent Successfully")
    }

    override fun storeNotFound() {
        showToast(R.string.storedetail_store_not_available)
        finish()
    }


    private fun populateHeader(store: Store) {
        isStoreLoaded = true
        invalidateOptionsMenu()
        storeName?.text = store.storeName
        supportActionBar?.title = store.storeName
        resetStoreActionLayout()
        setStoreActionLayout(store)
        if(store.category!=null){
            storeCategory?.visibility = View.VISIBLE
            storeCategory?.text = store.category!!.name
        }
        txtFollowValue?.text = store.followersCount.toString()
        txtTotalProductValue?.text = store.listingsCount.toString()
        if(store.geoPoint.latitude!=0.0){
            val storeLocation = store.address.city
            txtStoreLocation?.visibility = View.VISIBLE
            iconLocation?.visibility = View.VISIBLE
            txtStoreLocation?.text = if(storeLocation.isNotEmpty())storeLocation else getString(R.string.storedetail_view_location)
            txtStoreLocation?.paintFlags = Paint.UNDERLINE_TEXT_FLAG
            txtStoreLocation?.setOnClickListener {
                store.geoPoint.let { geoPoint->
                    Utils.showGoogleMap(geoPoint.latitude,geoPoint.longitude)
                }
            }
        }
        ImageHelper.getInstance().showImage(this, store.storePic, storeProfile, R.drawable.ic_store_placeholder, R.drawable.ic_store_placeholder)
    }

    fun checkAndOpenAddProduct(){
        if (AppConfigHelper.getConfigKey(AppConfigHelper.Keys.SUBSCRIPTION_ENABLED)){
            if(!store!!.subscription.isSubscriptionEnabled){
                Utils.showAlertDialog(this,title = null,message = AppConfigHelper.getConfigKey<String>(
                    AppConfigHelper.Keys.SUBSCRIPTION_ALLOW_LISTINGS_EXHAUSTED_MESSAGE)
                    ,isCancellable = false,withCancel = false,positiveButtonName = R.string.upgrade_now,dialogInterface = object :
                        Utils.DialogInterface{
                        override fun onAccept() {
                            startActivity(Intent(this@StoreDetailActivity, InAppSubscriptionActivity::class.java))
                        }

                        override fun onReject() {

                        }

                    })
                return
            }
        }
        startActivityForResult(AddProductActivity::class.java, AppConstant.ActivityResultCode.REFRESH_LISTING_IN_STORE_DETAIL)
    }

    private fun resetStoreActionLayout(){
        btnActionThree?.background = null
        btnActionTwo?.background = null
        btnActionOne?.background = null
    }

    private fun setStoreActionLayout(store: Store){
        if(userStore?.id == store.id){

            val marginSize = Utils.getPixel(this,4)
            val btnHeight = Utils.getPixel(this,40)
            val param = LinearLayout.LayoutParams(0,btnHeight)
            param.weight = 3f
            param.marginStart = marginSize
            btnActionOne?.layoutParams = param
            btnActionThree?.layoutParams = param

            btnActionOne?.background = if(store.active) outlineYellowBg else btnOutlinedBg
            btnActionTwo?.visibility = View.GONE
            btnActionThree?.background = btnOutlinedBg
            iconActionThree?.visibility = View.GONE

            txtActionOne?.text = getString(if(store.active) R.string.storedetail_inactive else R.string.storedetail_active)
            txtActionThree?.text = getString(R.string.edit_profile)
            txtActionThree?.visibility = View.VISIBLE
            txtActionThree?.setTextColor(ContextCompat.getColor(this,colorPrimary))
            txtActionOne?.setTextColor(ContextCompat.getColor(this,if(store.active) R.color.warning_Yellow else colorPrimary))
            btnActionOne?.safeClickListener { showInActiveSheet() }
            btnActionThree?.safeClickListener { openEditStore() }
            setStoreStatusInfo(store)
        }
        else{
            txtActionOne?.text = getString(if (store.followed) R.string.storelist_following else R.string.storelist_follow)
            btnActionOne?.background = if (store.followed) outlineFollowingBg else btnFilledBg
            txtActionOne?.setTextColor(ContextCompat.getColor(this, if (store.followed) colorPrimary else R.color.colorWhite))
            btnActionOne?.setOnClickListener {
                if (AppController.appController.getUser() != null) {
                    updateStoreFollow()
                } else {
                    val intent = Intent(this, AuthenticationActivity::class.java)
                    intent.putExtra("isFor", AppConstant.LoginFor.FOLLOW)
                    startActivityForResult(intent, AppConstant.ActivityResultCode.FOLLOW_LOGIN_FROM_STORE_DETAIL)
                }
            }
            btnActionTwo?.background = btnOutlinedBg
            btnActionThree?.background = btnOutlinedBg
            btnActionTwo?.setOnClickListener { shareStore() }
            btnActionThree?.setOnClickListener { openChat() }
            setStoreStatusInfo(store)
        }
    }

    private fun updateStoreFollow(){
        this.store?.let {
            if(it.followed){
                it.followed = !it.followed
                storePresenter.unFollowStore(it.id)
            }
            else{
                it.followed = !it.followed
                storePresenter.followStore(it.id)
            }
            btnActionOne?.background = null
            txtActionOne?.text = getString(if (it.followed) R.string.storelist_following else R.string.storelist_follow)
            btnActionOne?.background =if (it.followed) outlineFollowBg else btnFilledBg
            txtActionOne?.setTextColor(ContextCompat.getColor(this, if (it.followed) colorPrimary else R.color.colorWhite))
        }
    }

    private fun showManageListingDialog(position:Int){
        val bottomChooserDialog = BottomChooserDialog()
        bottomChooserDialog.setDialogType(BottomChooserDialog.DialogType.MANAGE_LIST)
        bottomChooserDialog.setListener(object : DialogListener{
            override fun onClick(any: Any) {
                val id = any as Int
                when(id){
                    BottomChooserDialog.ManageListAction.DELETE->showDeletePrompt(position)
                    BottomChooserDialog.ManageListAction.MARK_AS_SOLD->storePresenter?.markAsSold(productList[position].id)
                    BottomChooserDialog.ManageListAction.EDIT->{
                        storePresenter.getProduct(productList[position].id){
                            val intent = Intent(this@StoreDetailActivity,AddProductActivity::class.java)
                            intent.putExtra("product",it.toJson<Product>())
                            intent.putExtra("isForEdit",true)
                            startActivity(intent)
                        }
                    }
                }
            }
        })
        bottomChooserDialog.show(supportFragmentManager, AppConstant.FragmentTag.BOTTOM_CHOOSER_FRAGMENT)
    }

    private fun showDeletePrompt(position:Int){
        Utils.showAlertDialog(this,getString(R.string.product_delete_product),getString(R.string.product_alert_product_delete),true,true,object :
            Utils.DialogInterface{
            override fun onAccept() {
                storePresenter.deleteProduct(productList[position].id)
            }

            override fun onReject() {

            }
        })
    }
    override fun onFailure(appError: AppError) {
        ErrorHandler.handleError(this, appError)
        swipeRefresh?.isRefreshing = false
        hideProgressLoader()
    }

    override fun noInternet() {
        showToast(R.string.no_internet)
    }

    override fun hideProgressLoader() {
        progress_circular?.visibility = View.GONE
    }

    override fun showProgressLoader() {
        progress_circular?.visibility = View.VISIBLE
    }

    override fun loadStoreProducts(shouldRefresh: Boolean) {
        if(shouldRefresh){
            tabParentLayout?.visibility = View.VISIBLE
            listingFragment.refresh()
        }
    }

    override fun showActivationStatus(status: Boolean) {
        if(status){
            this.store!!.active = !store!!.active
            var actionMsg:String
            var txtActionMsg:String
            val txtActionColor:Int
            if(this.store!!.active){
                actionMsg = getString(R.string.storedetail_activated)
                txtActionMsg = getString(R.string.storedetail_inactive)
                txtActionColor = ContextCompat.getColor(this,R.color.colorYellow)
                btnActionOne?.background = outlineYellowBg
            }
            else{
                actionMsg = getString(R.string.storedetail_inactivated)
                txtActionMsg = getString(R.string.storedetail_active)
                txtActionColor = ContextCompat.getColor(this,colorPrimary)
                btnActionOne?.background = btnOutlinedBg
            }
            setStoreStatusInfo(this.store!!)
            txtActionOne?.setTextColor(txtActionColor)
            txtActionOne?.text = txtActionMsg
            showToast(String.format(getString(R.string.storedetail_account_activated_successfully),actionMsg))
        }
    }

    private fun setStoreStatusInfo(store: Store){
        val status = store.status
        if (status == AppConstant.StoreStatus.APPROVED) {
            if (store.active) {
                activeInfoLayout?.setGone()

            } else {
                txtActiveInfo?.text = getString(R.string.storedetail_store_currently_unavailable)
                activeInfoLayout?.setVisible()
            }
            btnActionLayout?.setVisible()
        } else {
            txtActiveInfo?.text = if (status == AppConstant.StoreStatus.WAITING_FOR_APPROVAL) {
                getString(R.string.storedetail_alert_message_store_waiting_for_approval)
            } else {
                getString(R.string.storedetail_alert_message_store_rejected)
            }
            activeInfoLayout?.setVisible()
            btnActionLayout?.setGone()
        }
    }

    private fun initList() {
        pagination = 1
        isLoading = true
        isPaginationEnd = false
        productList.clear()
    }

    override fun productDeleted(productId:String) {
        productList.find { i->i.id == productId }?.let {
            productList.remove(it)
            listingProductAdapter?.notifyDataSetChanged()
        }
        Utils.showAlertDialog(this, null, getString(R.string.product_alert_success_product_delete),true,  false,null)
    }

    override fun productMarkedAsSold(productId: String) {
        productList.find { i->i.id == productId }?.let {
            it.inStock = false
            val index = productList.indexOf(it)
            listingProductAdapter?.notifyItemChanged(index)
        }
    }
    override fun showProgressDialog(msg: Int) {
        showLoader(msg)
    }

    override fun hideProgressDialog() {
        hideLoader()
    }

    override fun onStoreBlockStatus(status: Boolean) {
        if(status){
            onBackPressed()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && data != null) {
            when (requestCode) {
                AppConstant.ActivityResultCode.CHAT_FROM_STORE_DETAIL-> openChat()
                AppConstant.ActivityResultCode.FOLLOW_LOGIN_FROM_STORE_DETAIL -> storePresenter?.getStore(storeId = store?.id,shouldGetStoreProduct = false)
                AppConstant.ActivityResultCode.REFRESH_LISTING_IN_STORE_DETAIL,
                AppConstant.ActivityResultCode.EDIT_STORE->{
                    storePresenter.getStore(isUserStore, store?.id, true)
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        if(isStoreLoaded){
            if(this.store!=null)
                if(userStore?.id != storeId && this.store!!.active){
                    menu?.findItem(R.id.action_block)?.isVisible = true
                }
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_block-> store?.let { storePresenter.blockStore(it.id) }
            R.id.action_share->shareStore()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun openEditStore(){
        if(userStore != null){
            val bundle = Bundle().apply {
                putBoolean(AppConstant.BundleProperty.IS_EDIT,true)
                putString(AppConstant.BundleProperty.IS_FROM, AppConstant.BundleProperty.MY_STORE)
                putString(AppConstant.BundleProperty.STORE , userStore.toJson<Store>())
            }
            startActivityForResult(AddStoreDetailActivity::class.java,
                AppConstant.ActivityResultCode.EDIT_STORE,bundle)
        }
    }


    private fun showInActiveSheet(){
     val bottomChooserDialog = BottomChooserDialog()
     bottomChooserDialog.setDialogType(BottomChooserDialog.DialogType.ACCOUNT_ACTIVATE)
     bottomChooserDialog.arguments = Bundle().apply { putBoolean(AppConstant.BundleProperty.IS_ACTIVE,store!!.active) }
     bottomChooserDialog.setListener(object : DialogListener{
         override fun onClick(any: Any) {
             bottomChooserDialog.dismiss()
             storePresenter.accountActivate(store!!.id,!store!!.active)
         }
     })
     bottomChooserDialog.show(supportFragmentManager, AppConstant.FragmentTag.BOTTOM_CHOOSER_FRAGMENT)
    }

    private fun shareStore(){
        store?.let { it ->
            var desc = AppConfigHelper.getTenantConfigKey<String>(AppConfigHelper.Keys.BRANCH_LINK_DESCRIPTION)
            val branchBaseUrl = AppConfigHelper.getTenantConfigKey<String>(AppConfigHelper.Keys.BRANCH_LINK_BASE_URL)
            if(branchBaseUrl.isNotEmpty() && it.uniqueName.isNotEmpty()){
                ShareUtil.showIntentChooser(this,branchBaseUrl.plus(it.uniqueName))
            }
            else{
                showProgressDialog(R.string.please_wait)
                BranchHelper.getBranchStoreSharingUrl(this,it.id,it.storeName,it.uniqueName,it.storePic,desc){ url, errorcode->
                    hideProgressDialog()
                    if(!url.isNullOrEmpty()){
                        ShareUtil.showIntentChooser(this,url)
                    }
                }
            }
        }
    }

    fun likeStoreReview(reviewId:String,status:Int) = storePresenter.likeReview(reviewId,status)

    private fun openChat(){
        if(AppController.appController.getUser()==null){
            startActivityForResult(AuthenticationActivity::class.java, AppConstant.ActivityResultCode.CHAT_FROM_STORE_DETAIL)
        }
        else{
            if(store!=null && store?.user!=null){
                val provider = store?.user!!
                val currentUser = AppController.appController.getUser()!!
                showProgressDialog(R.string.please_wait)
                MessageHelper.checkChatRoomAvailable(currentUser.name,currentUser.id,provider.id,provider.name,currentUser.profilePic,provider.profilePic,object :MessageHelper.Companion.ChatListener{
                    override fun onFailure(message: String?, code: Int) {
                        hideProgressDialog()
                    }

                    override fun onSuccess(any: Any?) {
                        hideProgressDialog()
                        (any as? ChatRoom)?.let {
                            startActivity(ChatThreadActivity::class.java, Bundle().apply { putString(
                                AppConstant.BundleProperty.CHAT_ROOM,it.toJson<ChatRoom>()) })
                        }
                    }
                })
            }
        }
    }

    fun showReviewSheet(){
        bottomChooserDialog = ViewUtil.showReviewSheet(AppConstant.ModuleType.ACCOUNTS,store!!){ resultBundle->
            val images = resultBundle.getString(AppConstant.BundleProperty.IMAGES).toList<ImageFeed>()
            storePresenter.submitReview(
                resultBundle.getString(AppConstant.BundleProperty.ID)!!,
                resultBundle.getInt(AppConstant.BundleProperty.REVIEW_TITLE),
                resultBundle.getString(AppConstant.BundleProperty.REVIEW_CONTENT)!!,
                resultBundle.getInt(AppConstant.BundleProperty.REVIEW_RATING),
                images = images!!,
                callback = {
                    if(it){
                        showToast(getString(R.string.addreview_review_success_message))
                        if(!isFinishing){
                            bottomChooserDialog?.let {  dialog->
                                if(dialog.isAdded)dialog.dismiss()
                                storePresenter.getStore(isUserStore,storeId)
                            }
                        }
                    }
                }
            )
        }
        bottomChooserDialog?.show(supportFragmentManager, AppConstant.FragmentTag.BOTTOM_CHOOSER_FRAGMENT)
    }

    override fun onDestroy() {
        super.onDestroy()
        storePresenter.onDestroy()
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_OK,Intent())
        finish()
    }
}
