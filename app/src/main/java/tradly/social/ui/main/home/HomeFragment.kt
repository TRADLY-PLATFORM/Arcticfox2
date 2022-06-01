package tradly.social.ui.main.home


import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import tradly.social.R
import tradly.social.adapter.BannerViewPagerAdapter
import tradly.social.adapter.HomeAdapter
import tradly.social.adapter.HomeCategoryGridAdapter
import tradly.social.common.*
import tradly.social.common.network.parseHelper.ParseConstant
import tradly.social.domain.entities.*
import tradly.social.domain.entities.Collection
import tradly.social.common.base.BaseFragment
import tradly.social.ui.category.CategoryListActivity
import tradly.social.ui.group.createGroup.CreateGroupActivity
import tradly.social.ui.login.AuthenticationActivity
import tradly.social.ui.product.addProduct.AddProductActivity
import tradly.social.ui.product.addProduct.productDetail.ProductDetailActivity
import tradly.social.ui.store.addStore.AddStoreDetailActivity
import tradly.social.ui.store.storeDetail.StoreDetailActivity
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_home.view.*
import tradly.social.common.base.Utils
import tradly.social.common.base.*
import tradly.social.common.navigation.Activities
import tradly.social.common.navigation.common.NavigationIntent
import tradly.social.common.util.parser.extension.toJson
import tradly.social.ui.cart.CartListActivity
import tradly.social.ui.groupListing.ListingActivity
import tradly.social.ui.main.MainInterface
import tradly.social.ui.wishlist.WishListActivity
typealias preferenceConstant = tradly.social.common.common.AppConstant


/**
 * A simple [Fragment] subclass.
 */
class HomeFragment : BaseFragment(), HomePresenter.View {
    private lateinit var homePresenter: HomePresenter
    private var homeCategoryGridAdapter: HomeCategoryGridAdapter? = null
    private var bannerAdapter: BannerViewPagerAdapter? = null
    private var bannerList = mutableListOf<PromoBanner>()
    private var homeAdapter: HomeAdapter? = null
    private var categoryList = mutableListOf<Category>()
    private var collections = ArrayList<Collection>()
    private var mainInterface:MainInterface?=null
    private var mView: View? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (mView != null && AppController.appController.getHome() != null) {
            homePresenter.setInteractor(this)
            return mView
        }
        mView = inflater.inflate(R.layout.fragment_home, container, false)
        setHasOptionsMenu(true)
        initList()
        homePresenter = HomePresenter(this)
        homeCategoryGridAdapter = HomeCategoryGridAdapter(requireContext(), false, categoryList) { category: Category ->
                if (category.subCategory.isNotEmpty()) {
                    val intent = Intent(requireContext(), CategoryListActivity::class.java)
                    intent.putExtra("isFor", AppConstant.FragmentTag.CATEGORY_FRAGMENT)
                    intent.putExtra(AppConstant.BundleProperty.CATEGORY_ID, category.id)
                    intent.putExtra(AppConstant.BundleProperty.CATEGORY_NAME, category.name)
                    intent.putExtra("category",category.toJson<Category>())
                    startActivity(intent)
                } else {
                    val intent = Intent(requireContext(), CategoryListActivity::class.java)
                    intent.putExtra("isFor", if (category.isMore) AppConstant.FragmentTag.CATEGORY_FRAGMENT else AppConstant.FragmentTag.PRODUCT_LIST_FRAGMENT)
                    intent.putExtra(AppConstant.BundleProperty.CATEGORY_ID, if (category.isMore) "0" else category.id)
                    intent.putExtra(AppConstant.BundleProperty.CATEGORY_NAME, if (category.isMore) getString(R.string.category_header_title) else category.name)
                    startActivity(intent)
                }
            }
        homeAdapter = HomeAdapter(requireContext(), collections) { position: Int, scopeType: Int, obj: Any ->
                if (scopeType == ParseConstant.HOME_SCOPE.GROUP) {
                    val group = obj as? Group
                    group?.let {
                        homePresenter?.addUserToGroup(group.id)
                    }
                } else if (ParseConstant.HOME_SCOPE.STORE == scopeType) {
                    if(AppController.appController.getUser() != null){
                        val store = obj as? Store
                        store?.let {
                            homePresenter?.followStore(store.id)
                        }
                    }
                    else{
                       val intent = Intent(requireContext(),AuthenticationActivity::class.java)
                        intent.putExtra("isFor", AppConstant.LoginFor.FOLLOW)
                        startActivityForResult(intent, AppConstant.ActivityResultCode.LOGIN_FROM_HOME)
                    }
                }
                else if(ParseConstant.HOME_SCOPE.INVITE_FRIENDS==scopeType){
                   val inviteFragment= InviteFragment()
                    inviteFragment.show(childFragmentManager, AppConstant.FragmentTag.INVITE_FRAGMENT)
                }
            }
        bannerAdapter = BannerViewPagerAdapter(requireContext(), bannerList) {
            when (it.type) {
                AppConstant.BannerType.BANNER_HOME_ACCOUNT -> {
                    val intent = Intent(requireContext(), StoreDetailActivity::class.java)
                    intent.putExtra(AppConstant.BundleProperty.ID, it.reference)
                    startActivity(intent)
                }
                AppConstant.BannerType.BANNER_HOME_LISTING->{
                    val intent = Intent(requireContext(), ProductDetailActivity::class.java)
                    intent.putExtra(AppConstant.BundleProperty.PRODUCT_ID, it.reference)
                    startActivity(intent)
                }
                AppConstant.BannerType.BANNER_HOME_EXTERNAL-> Utils.openUrlInBrowser(it.reference)
            }
        }
        mView?.homeCategoryRecycler?.setHasFixedSize(true)
        mView?.homeCategoryRecycler?.isNestedScrollingEnabled = false

        mView?.homeCategoryRecycler?.layoutManager = GridLayoutManager(requireContext(), 4)
        mView?.homeCategoryRecycler?.adapter = homeCategoryGridAdapter
        mView?.homeBanners?.clipToPadding = false
        mView?.homeBanners?.setPadding(40, 20, 70, 20)
        mView?.homeBanners?.pageMargin = 25
        mView?.homeBanners?.adapter = bannerAdapter
        mView?.collection?.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        mView?.collection?.adapter = homeAdapter
        mView?.actionMore?.setOnClickListener {
           if(AppController.appController.getUser() != null){
               val intent = Intent(requireContext(), CreateGroupActivity::class.java)
               startActivity(intent)
           }
            else{
               val intent = Intent(requireContext(), AuthenticationActivity::class.java)
               intent.putExtra("isFor", AppConstant.LoginFor.GROUP)
               startActivityForResult(intent, AppConstant.ActivityResultCode.LOGIN_FROM_HOME)
           }
        }
        homePresenter.syncAndUpdate()
        loadHomeData(true)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        loadAds()
        homePresenter.syncConfigs()
        val homeLocationConfigEnabled = AppConfigHelper.getTenantConfigKey<Boolean>(AppConfigHelper.Keys.HOME_LOCATION_ENABLED)
        if(homeLocationConfigEnabled && tradly.social.common.persistance.shared.PreferenceSecurity.getBoolean(
                AppConstant.PREF_SHOULD_ASK_HOME_LOCATION_CONSENT,true)){
            showHomeLocationConsentDialog()
        }
    }

    private fun initList(){
        categoryList.clear()
        collections.clear()
        bannerList.clear()

    }
    private fun initView() {
        //TODO Try in xml
        val bgHomeTopItem = ThemeUtil.getResourceValue(context, R.attr.homeTopViewItemBg)
        actionSell.setBackgroundResource(bgHomeTopItem)
        actionDonate.setBackgroundResource(bgHomeTopItem)
        actionRent.setBackgroundResource(bgHomeTopItem)
        actionMore.setBackgroundResource(bgHomeTopItem)
        swipeRefresh?.setOnRefreshListener {
            swipeRefresh?.isRefreshing = true
            loadHomeData(false)
            homePresenter.syncConfigs()
        }
    }

    private fun loadHomeData(shouldShowProgressBar:Boolean){
        if (isHomeLocationEnabled()) {
            LocationHelper.getInstance().checkSettings { success, exception ->
                if(success){
                    homePresenter.loadData(shouldShowProgressBar,true)
                }
                else{
                    homePresenter.loadData(shouldShowProgressBar,false)
                }
            }
        } else {
            homePresenter.loadData(shouldShowProgressBar,false)
        }
    }

    private fun loadAds(){
        /*if (AppConfigHelper.getConfigKey(AppConfigHelper.Keys.HOME_ADMOB_ENABLED) && getString(R.string.admob_appid).isNotEmpty()){
            val adUnitId =  AppConfigHelper.getConfigKey<String>(AppConfigHelper.Keys.HOME_ADMOB_ID)
            if (adUnitId.isNotEmpty()){
                val adView = AdView(requireContext())
                adViewContainer.addView(adView)
                val adRequest = AdRequest.Builder().build()
                adView.setAdWidth(requireActivity())
                adView.adUnitId = adUnitId
                adView.loadAd(adRequest)
            }
        }*/
    }

    override fun noInternet() {
        requireContext().showToast(R.string.no_internet)
    }

    override fun netWorkError() {
        progressBar?.visibility = View.GONE
    }

    override fun showProgressLoader() {
        mView?.progressBar?.visibility = View.VISIBLE
    }

    override fun hideProgressLoader() {
        mView?.progressBar?.visibility = View.GONE
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onLoadData(home: Home) {
        AppController.appController.setHomeData(home)
        applyHomeConfigs(home)
        progressBar?.visibility = View.GONE
        swipeRefresh?.isRefreshing = false
        if (home.collections.count() > 0) collection?.visibility = View.VISIBLE
        categoryList.clear()
        if (home.categories.size >= 8) {
            categoryList.addAll(home.categories.subList(0, 7))
            categoryList.add(Category(name = getString(R.string.home_more), isMore = true))
        } else {
            categoryList.addAll(home.categories)
        }
        bannerList.clear()
        bannerList.addAll(home.promoBanners)
        collections.clear()
        collections.addAll(home.collections)
        homeCategoryGridAdapter?.notifyDataSetChanged()
        bannerAdapter?.notifyDataSetChanged()
        homeAdapter?.notifyDataSetChanged()
        checkAndUpdateApp(home.clientVersion)
    }

    private fun applyHomeConfigs(home: Home){
        mainInterface?.setHomeTitle()
        val homeCategoryEnabled = AppConfigHelper.getConfigKey<Boolean>(AppConfigHelper.Keys.HOME_CATEGORY_ENABLED,true)
        val homeBannerEnabled = AppConfigHelper.getConfigKey<Boolean>(AppConfigHelper.Keys.HOME_PROMO_BANNER_ENABLED,true)
        val homeInviteEnabled = AppConfigHelper.getConfigKey<Boolean>(AppConfigHelper.Keys.HOME_INVITE_FRIEND_COLLECTION,true)
        homeCategoryRecycler?.visibility = if(homeCategoryEnabled && home.categories.count() > 0)View.VISIBLE else View.GONE
        homeBanners?.visibility = if(homeBannerEnabled && home.promoBanners.count() > 0)View.VISIBLE else View.GONE
        if(!homeInviteEnabled){
           val index = home.collections.indexOfFirst { c->c.scopeType == ParseConstant.HOME_SCOPE.INVITE_FRIENDS }
            if(index!=-1){
                home.collections.removeAt(index)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.toolbar_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        val searchItem = menu.findItem(R.id.action_search)
        val notificationItem = menu.findItem(R.id.action_notification)
        val wishListItem = menu.findItem(R.id.action_wish_list)
        val cartItem = menu.findItem(R.id.action_my_cart)
        cartItem.isVisible = true
        searchItem.isVisible = true
        wishListItem.isVisible = true
        notificationItem.isVisible = true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null && Activity.RESULT_OK == resultCode && requestCode == AppConstant.ActivityResultCode.LOGIN_FROM_HOME) {
            when(data.getStringExtra("isFor")){
                AppConstant.LoginFor.STORE->{
                    if (tradly.social.common.persistance.shared.PreferenceSecurity.getString(
                            preferenceConstant.PREF_USER_STORE).isNotEmpty()) {
                        requireContext().startActivity(AddProductActivity::class.java)
                    } else {
                        requireContext().startActivity(AddStoreDetailActivity::class.java)
                    }
                }
                AppConstant.LoginFor.GROUP->startActivity(Intent(requireContext(),CreateGroupActivity::class.java))
                AppConstant.LoginFor.FOLLOW->loadHomeData(false)
                AppConstant.LoginFor.MY_CART->requireContext().startActivity(CartListActivity::class.java)
                AppConstant.LoginFor.NOTIFICATION->showNotificationCenter()
            }
        }
        else if(requestCode == LocationHelper.LocationConstant.REQUEST_CHECK_SETTINGS && resultCode ==  Activity.RESULT_OK){
            LocationHelper.getInstance().checkSettings { success, exception ->
                if(success){
                    homePresenter.loadData(false,true)
                }
                else{
                    homePresenter.loadData(false,false)
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_search-> requireContext().startActivity(NavigationIntent.getIntent(Activities.SearchActivity))
            R.id.action_wish_list->requireContext().startActivity(WishListActivity::class.java)
            R.id.action_notification->{
              if(AppController.appController.getUser()!=null){
                  showNotificationCenter()
              }
              else{
                  val intent = Intent(requireContext(),AuthenticationActivity::class.java)
                  intent.putExtra("isFor", AppConstant.LoginFor.NOTIFICATION)
                  startActivityForResult(intent, AppConstant.ActivityResultCode.LOGIN_FROM_HOME)
              }
            }
            R.id.action_my_cart-> {
                if(AppController.appController.getUser() != null){
                    requireContext().startActivity(CartListActivity::class.java)
                }
                else{
                    val intent = Intent(requireContext(),AuthenticationActivity::class.java)
                    intent.putExtra("isFor", AppConstant.LoginFor.MY_CART)
                    startActivityForResult(intent, AppConstant.ActivityResultCode.LOGIN_FROM_HOME)
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode== PermissionHelper.RESULT_CODE_LOCATION){
            if(grantResults.count() > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                tradly.social.common.persistance.shared.PreferenceSecurity.putBoolean(AppConstant.PREF_HOME_LOCATION_ENABLED,true)
                loadHomeData(false)
            }
            else{
                tradly.social.common.persistance.shared.PreferenceSecurity.putBoolean(AppConstant.PREF_SHOULD_ASK_HOME_LOCATION_CONSENT,true)
                tradly.social.common.persistance.shared.PreferenceSecurity.putBoolean(AppConstant.PREF_HOME_LOCATION_ENABLED,false)
                requireContext().showToast(R.string.app_need_location_permission)
            }
        }
    }

    private fun showHomeLocationConsentDialog(){
        tradly.social.common.persistance.shared.PreferenceSecurity.putBoolean(AppConstant.PREF_SHOULD_ASK_HOME_LOCATION_CONSENT,false)
        Utils.showAlertDialog(requireContext(),getString(R.string.near_by_store_title),getString(R.string.near_by_store_desc),false,true,object :
            Utils.DialogInterface{
            override fun onAccept() {
                if (PermissionHelper.checkPermission(requireContext(), PermissionHelper.Permission.LOCATION)) {
                    checkLocationSettings(false)
                } else {
                  LocationHelper.getInstance().requestLocationPermission(this@HomeFragment)
                }
            }

            override fun onReject() {
                tradly.social.common.persistance.shared.PreferenceSecurity.putBoolean(AppConstant.PREF_HOME_LOCATION_ENABLED,false)
            }
        })
    }

    fun checkLocationSettings(shouldShowProgressBar:Boolean){
        LocationHelper.getInstance().checkSettings { success, exception ->
            if (success) {
                homePresenter.loadData(shouldShowProgressBar,true)
            } else {
                exception?.let {
                    startIntentSenderForResult(it.resolution.intentSender,
                        LocationHelper.LocationConstant.REQUEST_CHECK_SETTINGS,null, 0, 0, 0, null)
                }
            }
        }
    }

    private fun showNotificationCenter(){
        val bundle =  Bundle().apply {
            putInt(AppConstant.BundleProperty.ISFOR, AppConstant.ListingType.NOTIFICATION_LIST)
            putInt(AppConstant.BundleProperty.TOOLBAR_TITLE,R.string.notification_header_title)
        }
        requireContext().startActivity(ListingActivity::class.java,bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        swipeRefresh?.isRefreshing = false
        homePresenter.onDestroy()
    }

    override fun onFailure(appError: AppError) {
        progressBar?.visibility = View.GONE
        swipeRefresh?.isRefreshing = false
        ErrorHandler.handleError(requireContext(), appError)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainInterface = context as MainInterface
    }

    override fun onDetach() {
        super.onDetach()
        mainInterface = null
    }

    override fun onDestroy() {
        super.onDestroy()
        homePresenter.onDestroy()
    }
}
