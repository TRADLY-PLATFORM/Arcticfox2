package tradly.social.ui.main

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.toolbar.*
import tradly.social.R
import tradly.social.common.*
import tradly.social.common.base.*
import tradly.social.common.cache.AppCache
import tradly.social.common.navigation.Activities
import tradly.social.common.navigation.common.NavigationIntent
import tradly.social.data.model.AppDataSyncHelper
import tradly.social.data.model.CoroutinesManager
import tradly.social.common.network.feature.common.datasource.StoreDataSourceImpl
import tradly.social.domain.repository.StoreRepository
import tradly.social.domain.usecases.GetStores
import tradly.social.common.base.BaseActivity
import tradly.social.common.subscription.InAppSubscriptionActivity
import tradly.social.ui.login.AuthenticationActivity
import tradly.social.ui.main.home.HomeFragment
import tradly.social.ui.message.list.ChatListFragment
import tradly.social.ui.message.thread.ChatThreadActivity
import tradly.social.ui.orders.MyOrderActivity
import tradly.social.ui.orders.OrderDetailActivity
import tradly.social.ui.payment.PaymentActivity
import tradly.social.ui.payment.configurePayout.PayoutConfigureActivity
import tradly.social.ui.product.addProduct.AddProductActivity
import tradly.social.ui.product.addProduct.productDetail.ProductDetailActivity
import tradly.social.ui.sellerProfile.ProfileFragment
import tradly.social.ui.socialFeed.trending.TrendingFragment
import tradly.social.ui.splash.SplashActivity
import tradly.social.ui.store.addStore.AddStoreDetailActivity
import tradly.social.ui.store.storeDetail.StoreDetailActivity


class MainActivity : BaseActivity(),MainInterface {
    private var selectedFragment:Int = 0
    private var isActivityVisible = false;
    private var moduletype = AppCache.getCacheAppConfig()!!.module

    var iconTint:Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        iconTint = ThemeUtil.getResourceValue(this,R.attr.colorPrimary)
        if(AppController.appController.getAppConfig() == null){
            startActivity(SplashActivity::class.java)
            finish()
            return
        }
        setHomeTitle()
        setToolbar(toolbar)
        setHomeFabIcon()
        setBottomTabLabel()
        if(!tradly.social.common.persistance.shared.PreferenceSecurity.getBoolean(AppConstant.PREF_SETTINGS_CHANGED,false)){
            bottomNavigation(R.id.actionHome)
            checkRecentMessages()
        }
        else{
            tradly.social.common.persistance.shared.PreferenceSecurity.putBoolean(AppConstant.PREF_SETTINGS_CHANGED,false)
            bottomNavigation(R.id.actionMore)
        }
        checkAndHandleNotification()
        checkDeepLink()
        ShareUtil.initFaceBookSdk()
    }

    override fun setHomeTitle() {
        val  appTitle = AppConfigHelper.getConfigKey<String>(AppConfigHelper.Keys.APP_TITLE_HOME,getString(R.string.app_home_title))
        toolbar.setClientTitle(Page.HOME,ViewItem.HOME_TOOLBAR,appTitle)
    }

    private fun setHomeFabIcon(){
        if(getString(R.string.sell_icon_name).isNotEmpty()){
            homeFab?.visibility = View.INVISIBLE
            iconHomeSell?.visibility = View.VISIBLE
            titleHomeSell?.apply {
                text = getString(R.string.sell_icon_name)
                setVisible()
            }
        }
        else{
            homeFab.setClientImageResource(Page.HOME,ViewItem.BOTTOM_FAB,R.drawable.ic_sell_icon)
        }
    }

    private fun setBottomTabLabel(){
        txtMore.text = AppConfigHelper.getConfigKey(AppConfigHelper.Keys.BOTTOM_TAB_MORE_LABEL,getString(R.string.home_more))
        if (moduletype == AppConstant.ConfigModuleType.EVENT){
            txtSocialFeed.text = getString(R.string.event_explore)
        }
        else{
             txtSocialFeed.text = getString(R.string.home_social_feed)
        }
    }

    fun onBottomClick(view: View) {
        bottomNavigation(view.id)
    }

    override fun onStart() {
        super.onStart()
        BranchHelper.branchSessionInit(this,intent.data , ::handleBranchData)
        BranchHelper.createAppLink()
    }

    fun checkRecentMessages(){
        if(AppController.appController.getAppConfig()!=null && AppController.appController.getUser()!=null){
            MessageHelper.checkRecentMessage(::updateChatIndicator)
        }
    }

    fun updateChatIndicator(show:Boolean){
        if(!isFinishing){
            if(show){
                messageIndicator?.visibility = View.VISIBLE
                messageIndicator?.background = Utils.getDrawable(this,0,R.color.white,R.color.colorRed,shape = GradientDrawable.OVAL)
            }
            else{
                messageIndicator?.visibility = View.GONE
            }
        }
    }

    private fun bottomNavigation(id:Int){
        when(id){
            R.id.actionHome -> {
                if(R.id.actionHome != selectedFragment){
                    resetBottom()
                    iconHome.setColorFilter(ContextCompat.getColor(this, iconTint), android.graphics.PorterDuff.Mode.SRC_IN)
                    txtHome.setTextColor(ContextCompat.getColor(this, iconTint))
                    navigateFragment(HomeFragment(), AppConstant.FragmentTag.HOME_FRAGMENT)
                    setHomeTitle()
                    selectedFragment = R.id.actionHome
                }
            }
            R.id.actionSocialFeed -> {
                if (moduletype == AppConstant.ConfigModuleType.EVENT){
                    startActivity(NavigationIntent.getIntent(Activities.EventExploreActivity))
                }
                else{
                    if(R.id.actionSocialFeed != selectedFragment){
                        resetBottom()
                        iconSocialFeed.setColorFilter(ContextCompat.getColor(this, iconTint), android.graphics.PorterDuff.Mode.SRC_IN)
                        txtSocialFeed.setTextColor(ContextCompat.getColor(this, iconTint))
                        navigateFragment(TrendingFragment(), AppConstant.FragmentTag.TRENDING_FRAGMENT)
                        setToolbarTitle(toolbar,R.string.home_social_feed)
                        selectedFragment = R.id.actionSocialFeed
                    }
                }
            }
            R.id.homeFab, R.id.iconHomeSell-> { if(AppController.appController.shouldAllowToClick()) { showAddProduct() } }

            R.id.actionTransaction -> {
                if(R.id.actionTransaction != selectedFragment){
                    resetBottom()
                    iconTransaction.setColorFilter(ContextCompat.getColor(this, iconTint), android.graphics.PorterDuff.Mode.SRC_IN)
                    txtTransaction.setTextColor(ContextCompat.getColor(this, iconTint))
                    navigateFragment(ChatListFragment(), AppConstant.FragmentTag.CHAT_FRAGMENT)
                    setToolbarTitle(toolbar,R.string.home_chats)
                    selectedFragment = R.id.actionTransaction
                }
            }
            R.id.actionMore -> {
                if(R.id.actionMore != selectedFragment){
                    resetBottom()
                    selectedFragment = R.id.actionMore
                    navigateFragment(ProfileFragment(), AppConstant.FragmentTag.PROFILE_FRAGMENT)
                    iconMore.setColorFilter(ContextCompat.getColor(this, iconTint), android.graphics.PorterDuff.Mode.SRC_IN)
                    txtMore.setTextColor(ContextCompat.getColor(this, iconTint))
                    setToolbarTitle(toolbar)
                }
            }
        }
    }

   private fun navigateFragment(newFragment:Fragment ,tag:String){
        var commitFragment:Fragment? = null
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        var currentFragment = supportFragmentManager.primaryNavigationFragment
        if(currentFragment != null){
            fragmentTransaction.detach(currentFragment)   //detach current fragment
        }
        var fragmentFromTag = supportFragmentManager.findFragmentByTag(tag)
        fragmentFromTag?.let{
            commitFragment = fragmentFromTag
            fragmentTransaction.attach(fragmentFromTag)   //attach fragment
        }?:run{
            commitFragment = newFragment
            fragmentTransaction.add(R.id.fragContainer,newFragment,tag)
        }
        fragmentTransaction.setPrimaryNavigationFragment(commitFragment)
        fragmentTransaction.setReorderingAllowed(true)
        if(!supportFragmentManager.isStateSaved){
            fragmentTransaction.commit()
        }
    }
    private fun resetBottom() {

        /*iconHome.setImageResource(R.drawable.ic_home)
        iconSocialFeed.setImageResource(R.drawable.ic_feed_icon)
        iconTransaction*/
        iconHome.setColorFilter(ContextCompat.getColor(this, R.color.colorDarkGrey), android.graphics.PorterDuff.Mode.SRC_IN)
        iconSocialFeed.setColorFilter(ContextCompat.getColor(this, R.color.colorDarkGrey), android.graphics.PorterDuff.Mode.SRC_IN)
        iconTransaction.setColorFilter(ContextCompat.getColor(this, R.color.colorDarkGrey), android.graphics.PorterDuff.Mode.SRC_IN)
        iconMore.setColorFilter(ContextCompat.getColor(this, R.color.colorDarkGrey), android.graphics.PorterDuff.Mode.SRC_IN)

        txtHome.setTextColor(ContextCompat.getColor(this, R.color.colorDarkGrey))
        txtSocialFeed.setTextColor(ContextCompat.getColor(this, R.color.colorDarkGrey))
        txtTransaction.setTextColor(ContextCompat.getColor(this, R.color.colorDarkGrey))
        txtMore.setTextColor(ContextCompat.getColor(this, R.color.colorDarkGrey))
    }

    private fun showAddProduct(){
        if (AppController.appController.getUser() != null) {
            if(NetworkUtil.isConnectingToInternet()){
                checkAndSyncStoreDetails{
                    checkAndAddProduct()
                }
            }
            else{
                showToast(getString(R.string.no_internet))
            }
        } else {
            val intent = Intent(this, AuthenticationActivity::class.java)
            intent.putExtra("isFor", AppConstant.LoginFor.STORE)
            startActivityForResult(intent, AppConstant.ActivityResultCode.LOGIN_FROM_HOME)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(data != null && resultCode == Activity.RESULT_OK){
            when(requestCode){
                AppConstant.ActivityResultCode.LOGIN_FROM_HOME->{
                    when(data.getStringExtra("isFor")){
                        AppConstant.LoginFor.STORE->checkAndSyncStoreDetails{ checkAndAddProduct() }
                        AppConstant.LoginFor.PRODUCT_DETAIL->{
                            startActivity(ProductDetailActivity::class.java, data.extras)
                        }
                    }
                }
            }
        }

    }

    private fun handleBranchData(bundle: Bundle){
        if(!bundle.isEmpty){
            when(bundle.getString(BranchHelper.BranchConstant.LINK_TYPE, AppConstant.EMPTY)){
                BranchHelper.LinkType.TYPE_LISTING->{
                    val mBundle = Bundle()
                    mBundle.putString("productId",bundle.getString(BranchHelper.BranchConstant.PRODUCT_ID))
                    startActivity(ProductDetailActivity::class.java,mBundle)
                }
                BranchHelper.LinkType.TYPE_STORE->{
                    startActivity(StoreDetailActivity::class.java,Bundle().apply {
                        putString("storeName",bundle.getString(BranchHelper.BranchConstant.STORE_NAME))
                        putString("id",bundle.getString(BranchHelper.BranchConstant.STORE_ID))
                    })
                }
            }
        }
    }

    private fun checkAndHandleNotification(){
        if(AppController.appController.getUser()!=null){
            if(intent.getBooleanExtra(AppConstant.BundleProperty.IS_FROM_NOTIFICATION,false)){
                when(intent.getStringExtra(NotificationHelper.PayloadKey.TYPE)){
                    NotificationHelper.NotificationTypes.TYPE_CHAT->{
                        startActivity(ChatThreadActivity::class.java,intent)
                    }
                    NotificationHelper.NotificationTypes.TYPE_LISTING->{
                        val bundle = Bundle().apply { putString("productId",getStringData(
                            NotificationHelper.PayloadKey.LISTING_ID)) }
                        startActivity(ProductDetailActivity::class.java,bundle)
                    }
                    NotificationHelper.NotificationTypes.TYPE_ORDER->{
                        val bundle = Bundle()
                        bundle.putString(
                            AppConstant.BundleProperty.ORDER_ID,intent.getStringExtra(
                            NotificationHelper.PayloadKey.ORDER_ID))
                        AppController.appController.getUserStore(getStringData(NotificationHelper.PayloadKey.ACCOUNT_ID))?.let {
                            bundle.putBoolean(AppConstant.BundleProperty.IS_FOR_MY_ORDER,true)
                            bundle.putString(AppConstant.BundleProperty.ACCOUNT_ID,it.id)
                        }
                        startActivity(OrderDetailActivity::class.java,bundle)
                    }
                    NotificationHelper.NotificationTypes.TYPE_ACCOUNT->{
                        val bundle = Bundle()
                        bundle.putString(
                            AppConstant.BundleProperty.ID,intent.getStringExtra(
                            NotificationHelper.PayloadKey.ACCOUNT_ID))
                        bundle.putString(
                            AppConstant.BundleProperty.STORE_NAME,intent.getStringExtra(
                            NotificationHelper.PayloadKey.ACCOUNT_NAME))
                        startActivity(StoreDetailActivity::class.java,bundle)
                    }
                }
            }
        }
    }


    private fun checkDeepLink(){
        if(AppController.appController.getUser()!=null){
            intent.data?.let { uri ->
                if (getString(R.string.uri_scheme) == uri.scheme){
                    when(uri.host){
                        BuildConfig.CONFIGURE_PAYOUT->{
                            if(AppController.appController.getUserStore()!=null) {
                                val intent = Intent(this,PayoutConfigureActivity::class.java)
                                intent.putExtra(AppConstant.BundleProperty.IS_FROM_BROWSER_RESULT,true)
                                intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                                startActivityForResult(intent, AppConstant.ActivityResultCode.STRIPE_DISCONNECT)
                            }
                        }
                        BuildConfig.SUBSCRIPTION,
                        BuildConfig.MANAGE_SUBSCRIPTION ->{
                            if(AppController.appController.getUserStore()!=null){
                                AppDataSyncHelper.syncAppConfig()
                                checkAndSyncStoreDetails {
                                    if(uri.host == BuildConfig.SUBSCRIPTION){
                                        AppController.appController.getUserStore()?.let { store ->
                                            val intent = Intent(this,StoreDetailActivity::class.java)
                                            intent.putExtra("id",store.id)
                                            intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                                            startActivity(intent)
                                        }
                                    }
                                }
                            }
                        }
                        BuildConfig.ORDER_PAYMENT->{
                            val status = uri.getQueryParameter("status")
                            val orderReference = uri.getQueryParameter("order_reference")
                            val paymentMethodId = uri.getQueryParameter("payment_method_id")
                            if((status == AppConstant.PaymentStatus.SUCCESS || status == AppConstant.PaymentStatus.FAILURE)
                                && orderReference.isNotNull()
                                && paymentMethodId.isNotNull()){
                                val bundle = Bundle().apply {
                                    putString(AppConstant.BundleProperty.ORDER_REFERENCE,orderReference)
                                    putString(AppConstant.BundleProperty.PAYMENT_METHOD_ID,paymentMethodId)
                                    putBoolean(AppConstant.BundleProperty.IS_FROM_WEB_PAYMENT,true)
                                    putBoolean(AppConstant.BundleProperty.STATUS,status == AppConstant.PaymentStatus.SUCCESS)
                                }
                                if (AppCache.getCacheAppConfig()!!.module == AppConstant.ConfigModuleType.EVENT){
                                    val intent = NavigationIntent.getIntent(Activities.BookingHostActivity,bundle)
                                    startActivity(intent)
                                }
                                else{
                                    val intent = Intent(this,PaymentActivity::class.java)
                                    intent.putExtras(bundle)
                                    startActivity(intent)
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    override fun onResume() {
        super.onResume()
        AppDataSyncHelper.syncAppConfig()
        isActivityVisible = true
    }

    override fun onPause() {
        super.onPause()
        isActivityVisible = false;
    }

    private fun checkAndSyncStoreDetails(callback:()->Unit){
        val parseStoreDataSource = StoreDataSourceImpl()
        val storeRepository = StoreRepository(parseStoreDataSource)
        val store = GetStores(storeRepository)
        showLoader(R.string.please_wait)
        CoroutinesManager.ioThenMain({store.syncUserStore(AppCache.getCacheUser()?.id)},{
            hideLoader()
            callback()
        })
    }

    private fun checkAndAddProduct(){
        val store = AppController.appController.getUserStore()
        if (store!=null) {
            if(store.status == AppConstant.StoreStatus.APPROVED){
                if(AppConfigHelper.getConfigKey(AppConfigHelper.Keys.SUBSCRIPTION_ENABLED)){
                    if(!store.subscription.isSubscriptionEnabled){
                        Utils.showAlertDialog(this,title = null,message = AppConfigHelper.getConfigKey<String>(
                            AppConfigHelper.Keys.SUBSCRIPTION_ALLOW_LISTINGS_EXHAUSTED_MESSAGE)
                            ,isCancellable = false,withCancel = false,positiveButtonName = R.string.upgrade_now,dialogInterface = object :
                                Utils.DialogInterface{
                            override fun onAccept() {
                                startActivity(Intent(this@MainActivity, InAppSubscriptionActivity::class.java))
                            }

                            override fun onReject() {

                            }

                        })
                        return
                    }
                }
                val intent = Intent(this, AddProductActivity::class.java)
                intent.putExtra("isFromHome",true)
                startActivity(intent)
            }
            else{
                val statusMessage = if (store.status == AppConstant.StoreStatus.WAITING_FOR_APPROVAL) {
                    getString(R.string.storedetail_alert_message_store_waiting_for_approval)
                } else {
                    getString(R.string.storedetail_alert_message_store_rejected)
                }
                showToast(statusMessage)
            }
        } else {
            showAddAccountActivity()
        }
    }


    private fun checkAndLaunchMyOrder(){
        if(intent.getBooleanExtra(AppConstant.BundleProperty.IS_FOR_MY_ORDER,false)){
            startActivity(MyOrderActivity::class.java)
        }
    }

    private fun showAddAccountActivity(){
        val bundle = Bundle().apply {
            putString("isFrom" , "home")
            putBoolean("isFromHome",true)
        }
        startActivity(AddStoreDetailActivity::class.java,bundle)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        this.intent = intent
        BranchHelper.branchSessionReInit(this,intent?.data,::handleBranchData)
        checkAndHandleNotification()
        checkAndLaunchMyOrder()
        checkDeepLink()
    }

    override fun onBackPressed() {
       if(R.id.actionHome == selectedFragment){
          MessageHelper.removeRecentMessageListener()
          finish()
       }
        else{
           bottomNavigation(R.id.actionHome)
       }
    }
}
