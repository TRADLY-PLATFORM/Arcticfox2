package tradly.social.ui.sellerProfile


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.layout_upgrade.*
import tradly.social.R
import tradly.social.adapter.CountryAdapter
import tradly.social.common.*
import tradly.social.common.base.*
import tradly.social.common.cache.AppCache
import tradly.social.common.network.parseHelper.ParseConstant
import tradly.social.common.resources.ResourceConfig
import tradly.social.domain.entities.AppError
import tradly.social.domain.entities.Constant
import tradly.social.domain.entities.Country
import tradly.social.domain.entities.User
import tradly.social.common.base.BaseActivity
import tradly.social.common.base.BaseFragment
import tradly.social.common.subscription.InAppSubscriptionActivity
import tradly.social.common.util.common.LocaleHelper
import tradly.social.ui.base.BaseHostActivity
import tradly.social.ui.cart.CartListActivity
import tradly.social.ui.feedback.FeedbackActivity
import tradly.social.ui.login.AuthenticationActivity
import tradly.social.ui.orders.MyOrderActivity
import tradly.social.ui.payment.configurePayout.PayoutConfigureActivity
import tradly.social.ui.settings.SettingsActivity
import tradly.social.ui.splash.SplashActivity
import tradly.social.ui.store.addStore.AddStoreDetailActivity
import tradly.social.ui.store.storeDetail.StoreDetailActivity
import tradly.social.ui.subscription.SubscriptionInfoActivity
import tradly.social.ui.transaction.TransactionHostActivity
import tradly.social.ui.wishlist.WishListActivity

/**
 * A simple [Fragment] subclass.
 */
class ProfileFragment : BaseFragment(),ProfilePresenter.ProfilePresenterView, CustomOnClickListener.OnCustomClickListener {

    var mView:View?=null
    lateinit var presennter:ProfilePresenter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if(mView != null){
            return mView
        }
        mView = inflater.inflate(R.layout.fragment_profile, container, false)
        presennter = ProfilePresenter(this)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(CustomOnClickListener(this)){
            clSetting?.setOnClickListener(this)
            clCartList?.setOnClickListener(this)
            clLogout?.setOnClickListener(this)
            txtPrimary.setOnClickListener(this)
            txtSecondary.setOnClickListener(this)
            userProfile.setOnClickListener(this)
            clMyWishList.setOnClickListener(this)
            clMyStore.setOnClickListener(this)
            clMyCart.setOnClickListener(this)
            clLanguage.setOnClickListener(this)
            clTerms_condition.setOnClickListener(this)
            clChangeTenantLayout.setOnClickListener(this)
            clMyOrders.setOnClickListener(this)
            clMyStoreOrders.setOnClickListener(this)
            clFAQ.setOnClickListener(this)
            clPayout.setOnClickListener(this)
            clTransaction.setOnClickListener(this)
            clInvite.setOnClickListener(this)
            clRateOurApp.setOnClickListener(this)
            clSupport.setOnClickListener(this)
            clMyFollowingStores.setOnClickListener(this)
            clBlockedStores.setOnClickListener(this)
            clPurchases.setOnClickListener(this)
            clFeedback.setOnClickListener(this)
            btnUpgrade.setOnClickListener(this)
        }

        setProfileInfo()
    }

    override fun onCustomClick(view: View) {
        profileAction(view.id)
    }

    private fun profileAction(id:Int){
        when(id){
            R.id.txtSecondary,
            R.id.txtPrimary,
            R.id.userProfile->{
                AppController.appController.getUser()?.let {
                }?:run{
                    val intent = Intent(requireContext(), AuthenticationActivity::class.java)
                    intent.putExtra("isFor", AppConstant.LoginFor.SETTINGS)
                    startActivityForResult(intent, AppConstant.ActivityResultCode.LOGIN_FROM_SETTINGS)
                }
            }
            R.id.clSetting-> startActivityForResult(Intent(requireContext(),SettingsActivity::class.java),
                AppConstant.ActivityResultCode.REFRESH_PROFILE_FROM_SETTINGS)
            R.id.clMyStore-> showStoreDetail()
            R.id.clMyCart-> showMyCart()
            R.id.clMyWishList->requireContext().startActivity(WishListActivity::class.java)
            R.id.clLanguage-> showLocaleDialog(requireContext()){
                tradly.social.common.persistance.shared.PreferenceSecurity.putBoolean(AppConstant.PREF_SETTINGS_CHANGED, true)
                (activity as BaseActivity).refreshActivity(requireActivity())
            }
            R.id.clTerms_condition-> Utils.openUrlInBrowser(
                AppConfigHelper.getConfigKey(
                    AppConfigHelper.Keys.TERMS_URL,ResourceConfig.TERMS_URL))
            R.id.clFAQ-> Utils.openUrlInBrowser(
                AppConfigHelper.getTenantConfigKey<String>(
                    AppConfigHelper.Keys.FAQ_URL))
            R.id.clChangeTenantLayout-> Utils.changeTenant(requireActivity(),SplashActivity::class.java)
            R.id.clPayout->showStripeConnect()
            R.id.clMyOrders,
            R.id.clMyStoreOrders-> showMyStoreOrder(id == R.id.clMyStoreOrders)
            R.id.clMyFollowingStores, R.id.clBlockedStores-> launchBaseHost(id)
            R.id.clTransaction-> requireContext().startActivity(TransactionHostActivity::class.java)
            R.id.clInvite->showInviteDialog()
            R.id.clRateOurApp-> Utils.launchPlayStore(requireContext(),requireActivity().packageName)
            R.id.clSupport-> Utils.openUrlInBrowser(AppConfigHelper.getConfigKey(AppConfigHelper.Keys.SUPPORT_URL))
            R.id.clFeedback->requireContext().startActivity(FeedbackActivity::class.java)
            R.id.clPurchases->requireContext().startActivity(SubscriptionInfoActivity::class.java)
            R.id.btnUpgrade-> showInAppProducts()
        }
    }

    private fun showInAppProducts(){
        if (AppCache.getCacheUser()!=null){
            if (AppCache.getUserStore()!=null){
                startActivity(Intent(requireContext(),InAppSubscriptionActivity::class.java))
            }
            else{
                showAddAccountActivity()
            }
        }
        else{
            val intent = Intent(requireContext(), AuthenticationActivity::class.java)
            intent.putExtra("isFor", AppConstant.LoginFor.SETTINGS)
            startActivityForResult(intent, AppConstant.ActivityResultCode.LOGIN_FROM_IN_APP_PRODUCTS)
        }
    }

    private fun launchBaseHost(id:Int){
        when(id){
            R.id.clMyFollowingStores, R.id.clBlockedStores->  context?.startActivity(BaseHostActivity::class.java,Bundle().apply {
                putString(AppConstant.BundleProperty.FRAGMENT_TAG, AppConstant.FragmentTag.LISTING_FRAGMENT)
                putString(AppConstant.BundleProperty.TITLE,getString(if(id == R.id.clMyFollowingStores)R.string.more_stores_i_follow else R.string.more_stored_i_blocked))
                putInt(AppConstant.BundleProperty.IS_FROM,if(id == R.id.clMyFollowingStores) AppConstant.ListingType.STORE_FEEDS else AppConstant.ListingType.STORE_FEEDS_BLOCK)
            })
        }
    }
     private fun setProfileInfo(){
        showProfileLoginInfo()
        showTenantExitSettings()
        showLocaleChangeSettings()
        showOtherSettings()
    }

    private fun showOtherSettings(){
        val isEventModule = AppCache.getCacheAppConfig()!!.module == AppConstant.ConfigModuleType.EVENT
        AppController.appController.getUser()?.let { it->
            if(isUserHavingStore()){
                clMyStoreOrders?.visibility = if (isEventModule)View.GONE else View.VISIBLE
                clTransaction?.visibility = if (isEventModule)View.GONE else View.VISIBLE
                val paymentMethod = AppConfigHelper.getConfigKey<String>(AppConfigHelper.Keys.PAYMENT_METHOD)
                if(paymentMethod == AppConstant.PayoutMethod.STRIPE_CONNECT && AppConfigHelper.getConfigKey(
                        AppConfigHelper.Keys.STRIPE_ENABLED,false)){
                    clPayout?.visibility = View.VISIBLE
                }
                else{
                    clPayout?.visibility = View.GONE
                }

                val subscriptionEnabled = AppConfigHelper.getConfigKey<Boolean>(AppConfigHelper.Keys.SUBSCRIPTION_ENABLED)
                val store = AppController.appController.getUserStore()!!
                if(subscriptionEnabled && store.subscription.isSubscriptionEnabled){
                    clPurchases?.setVisible()
                }
                else{
                    clPurchases?.setGone()
                }
            }
            else{
                clMyStoreOrders?.visibility = View.GONE
                clTransaction?.visibility = View.GONE
                clPayout?.visibility = View.GONE
                clPurchases?.setGone()
            }
            clBlockedStores.setVisible()
            clMyFollowingStores.setVisible()
        }?:run{
            clMyStoreOrders?.visibility = View.GONE
            clPayout?.visibility = View.GONE
            clTransaction?.visibility = View.GONE
            clPurchases?.setGone()
            clBlockedStores.setGone()
            clMyFollowingStores.setGone()
        }
        clTerms_condition?.visibility = if(AppConfigHelper.getConfigKey<String>(AppConfigHelper.Keys.TERMS_URL,Constant.EMPTY).isEmpty())View.GONE else View.VISIBLE
        txtPoweredBy?.visibility = if(BuildConfig.FLAVOR.equals("client",true)) View.GONE else View.VISIBLE
        if(AppConfigHelper.getTenantConfigKey(AppConfigHelper.Keys.FEEDBACK_ENABLED)){
            clFeedback?.visibility = View.VISIBLE
        }
        clSupport?.visibility = if(AppConfigHelper.getConfigKey<String>(AppConfigHelper.Keys.SUPPORT_URL).isNotEmpty())View.VISIBLE else View.GONE
        if (isEventModule){
            clMyCart.setGone()
            clMyOrders.setGone()
        }

        cvUpgrade.visibility = if (AppConfigHelper.getConfigKey<Boolean>(AppConfigHelper.Keys.SUBSCRIPTION_ENABLED)) View.VISIBLE else View.GONE
    }

    private fun showProfileLoginInfo(){
        val user = AppController.appController.getUser()
        user?.let {
            ImageHelper.getInstance().showImage(requireContext(),it.profilePic,userProfile,R.drawable.ic_user_placeholder,R.drawable.ic_user_placeholder)
            txtPrimary?.text = it.name
            when(AppController.appController.getAppConfig()?.authType){
                ParseConstant.AuthType.EMAIL->{ txtSecondary?.text = it.email }
                ParseConstant.AuthType.MOBILE,
                ParseConstant.AuthType.MOBILE_PASSWORD,
                ParseConstant.AuthType.MOBILE_PASSWORD_NO_OTP->{ txtSecondary?.text = it.mobile }
            }
        }?:run{
            txtPrimary?.setText(R.string.more_welcome)
            txtSecondary?.setText(R.string.more_signin_signup)
            userProfile?.setImageResource(R.drawable.ic_user_placeholder)
        }
    }

    private fun showTenantExitSettings(){
        if(AppController.appController.isPlatformApp()){
            clChangeTenantLayout?.visibility = View.VISIBLE
            txtTenantInfo?.text = requireContext().getTwoStringData(R.string.more_exit_from,
                tradly.social.common.persistance.shared.PreferenceSecurity.getString(AppConstant.PREF_CURRENT_TENANT_ID))
        }
    }

    private fun showLocaleChangeSettings(){
        AppController.appController.getAppConfig()?.let {
            clLanguage?.visibility = if(it.localeSupported.size<=1)View.GONE else View.VISIBLE
        }?:run{
            clLanguage?.visibility = View.GONE
        }
    }

    private fun showStoreDetail(){
        AppController.appController.getUser()?.let {
            AppController.appController.getUserStore()?.let {
                val storeIntent = Intent(requireContext(), StoreDetailActivity::class.java)
                storeIntent.putExtra("id",it.id)
                startActivity(storeIntent)
            }?:run{
                val bundle = Bundle().apply { putString("isFrom" , "profile") }
                val intent = Intent(requireContext(),AddStoreDetailActivity::class.java)
                intent.putExtras(bundle)
                startActivity(intent)
            }
        }?:run{
            val intent = Intent(requireContext(),AuthenticationActivity::class.java)
            intent.putExtra("isFor", AppConstant.LoginFor.STORE)
            startActivityForResult(intent, AppConstant.ActivityResultCode.LOGIN_FROM_MY_STORE_PROFILE)
        }
    }

    private fun showInviteDialog(){
        val inviteFragment = InviteFragment()
        inviteFragment.show(childFragmentManager, AppConstant.FragmentTag.INVITE_FRAGMENT)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        setProfileInfo()
        if(Activity.RESULT_OK == resultCode && data != null){
            when(requestCode){
                AppConstant.ActivityResultCode.LOGIN_FROM_MY_STORE_PROFILE,
                AppConstant.ActivityResultCode.ADD_STORE_RESULT_CODE-> showStoreDetail()
                AppConstant.ActivityResultCode.LOGIN_FROM_MY_CART-> requireContext().startActivity(CartListActivity::class.java)
                AppConstant.ActivityResultCode.LOGIN_FROM_MY_ORDER -> requireContext().startActivity(MyOrderActivity::class.java,data.extras)
            }
        }
    }

    private fun showAddAccountActivity(){
        val bundle = Bundle().apply { putString("isFrom" , "profile") }
        val intent = Intent(requireContext(),AddStoreDetailActivity::class.java)
        intent.putExtras(bundle)
        startActivityForResult(intent, AppConstant.ActivityResultCode.ADD_STORE_RESULT_CODE)
    }

    private fun showMyStoreOrder(isMyStoreOrder:Boolean){
        val bundle = Bundle().apply { putBoolean(AppConstant.BundleProperty.IS_MY_STORE_ORDER, isMyStoreOrder) }
        AppController.appController.getUser()?.let {
            requireContext().startActivity(MyOrderActivity::class.java, bundle)
        } ?: run {
            val intent = Intent(requireContext(), AuthenticationActivity::class.java)
            intent.putExtras(bundle)
            startActivityForResult(intent, AppConstant.ActivityResultCode.LOGIN_FROM_MY_ORDER)
        }
    }

    private fun showMyCart(){
        AppController.appController.getUser()?.let {
            requireContext().startActivity(CartListActivity::class.java)
        }?:run{
            val intent = Intent(requireContext(),AuthenticationActivity::class.java)
            intent.putExtra("isFor", AppConstant.LoginFor.SETTINGS)
            startActivityForResult(intent, AppConstant.ActivityResultCode.LOGIN_FROM_MY_CART)
        }
    }

    private fun showStripeConnect(){
        val store = AppController.appController.getUserStore()
        if(store!=null){
            requireContext().startActivity(PayoutConfigureActivity::class.java)
        }
    }

    fun showLocaleDialog(ctx: Context, onClick:(language:String)->Unit){
        var dialog: AlertDialog? = null
        val alertDialog = AlertDialog.Builder(ctx)
        alertDialog.setTitle(tradly.social.common.base.R.string.language_select)
        val prevLocale = tradly.social.common.persistance.shared.PreferenceSecurity.getString(
            preferenceConstant.APP_LOCALE, LocaleHelper.getDefaultLocale(), AppConstant.PREF_LANGUAGE
        )
        var localeList = AppController.appController.getAppConfig()?.localeSupported
        localeList?.apply {
            if(this.isNotEmpty()){
                this.find { it.default }?.default = false
                this.find { it.locale == prevLocale }?.default = true
                val adapter = CountryAdapter(this,ctx, AppConstant.ListingType.LOCALE_LIST)
                alertDialog.setNegativeButton(android.R.string.cancel) { p0, p1 -> dialog?.dismiss()}
                alertDialog.setPositiveButton(android.R.string.ok) { p0, p1 ->
                    localeList.find { it.default }?.apply {
                        if(prevLocale != this.locale){
                            LocaleHelper.setLocale(ctx,this.locale)
                            onClick(this.locale)
                        }
                    }
                }
                alertDialog.setAdapter(adapter) { p0, pos -> }
                dialog = alertDialog.create()
            }
            dialog?.show()
        }
    }


    override fun onSuccess(user: User) {

    }

    override fun onFailure(appError: AppError) {

    }

    override fun noInternet() {
        requireContext().showToast(R.string.no_internet)
    }

    override fun inputError(id: Int, msg: Int) {

    }


    override fun showProgressDialog(msg: Int) {
        showLoader(msg)
    }

    override fun hideProgressDialog() {
        hideLoader()
    }

    override fun onLoadCountries(list: List<Country>?) {
    }


    override fun onDisconnectSuccess() {
        context?.showToast(R.string.payments_stripe_disconnected_success)
        setProfileInfo()
    }

    override fun onError(appError: AppError) {
        ErrorHandler.handleError(exception = appError)
    }
}
