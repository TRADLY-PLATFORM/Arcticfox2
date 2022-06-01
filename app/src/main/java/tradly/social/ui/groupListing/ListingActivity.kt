package tradly.social.ui.groupListing

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import tradly.social.R
import tradly.social.adapter.ListingAdapter
import tradly.social.common.*
import tradly.social.domain.entities.*
import tradly.social.domain.entities.Set
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_group_listing_acticity.*
import kotlinx.android.synthetic.main.activity_tag.progressBar
import kotlinx.android.synthetic.main.toolbar.*
import tradly.social.common.base.*
import tradly.social.ui.orders.OrderDetailActivity
import tradly.social.ui.product.addProduct.productDetail.ProductDetailActivity
import tradly.social.ui.store.storeDetail.StoreDetailActivity

class ListingActivity : BaseActivity(), ListingPresenter.View {
    private var listingPresenter: ListingPresenter? = null
    private var listingAdapter: ListingAdapter? = null
    private var groupList = mutableListOf<Group>()
    private var storeList = mutableListOf<Store>()
    private var setList = mutableListOf<Set>()
    private var selectedList = mutableListOf<Group>()
    private var notificationList = mutableListOf<Notification>()
    private var reviewList = mutableListOf<Review>()
    private var isFor: Int? = null
    private var reviewListType:String = Constant.EMPTY
    internal var pagination: Int = 1
    internal var isPaginationEnd: Boolean = false
    private var collectionId:String = Constant.EMPTY
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_listing_acticity)
        setToolbar(toolbar, intent.getIntExtra("toolbarTitle", 0), R.drawable.ic_back)
        initData()
        listingPresenter = ListingPresenter(this)
        initAdapter()
        initList()
        intent?.let {
            when (intent.getIntExtra("isFor", 0)) {
                AppConstant.ListingType.GROUP_TAG_PRODUCT -> {
                    txtJoin?.text = getString(R.string.group_tap_to_join)
                    txtEmptyStateMsg?.text = getString(R.string.group_you_have_not_following_groups)
                    selectedList.clear()
                    selectedList.addAll(getSelectedListfromString(intent.getStringExtra("list")))
                    showProgressLoader()
                    listingPresenter?.getMyGroups(pagination, false)
                }
                AppConstant.ListingType.GROUP_FOLLOW -> {
                    showProgressLoader()
                    txtEmptyStateMsg?.text = getString(R.string.groups_no_available)
                    //listingPresenter?.getGroupList(pagination, false)
                }
                AppConstant.ListingType.STORE_LIST -> {
                    showProgressLoader()
                    txtEmptyStateMsg?.text = getString(R.string.storelist_no_stores_available)
                    getStores(pagination,false,collectionId)
                }
                AppConstant.ListingType.NOTIFICATION_LIST ->{
                    showProgressLoader()
                    txtEmptyStateMsg?.text = getString(R.string.notification_no_notification_at_the_moment)
                    listingPresenter?.getNotifications(pagination,false)

                }
                AppConstant.ListingType.SET_LIST->{
                    setList.clear()
                    val product = Gson().fromJson(intent.getStringExtra("product"),Product::class.java)
                    product.sets?.let {
                        it.forEach {
                           it.defaultImage = product.images[0]
                        }
                        setList.addAll(it)
                        listingAdapter?.notifyDataSetChanged()
                    }
                }
                AppConstant.ListingType.REVIEW_LIST->{
                    reviewList.clear()
                    reviewListType = getStringData(AppConstant.BundleProperty.TYPE)
                    listingPresenter?.getReviewList(getStringData(AppConstant.BundleProperty.ID),reviewListType,pagination,false)

                }
                else -> {}
            }

        }

        txtJoin.setOnClickListener {
            val intent = Intent(this, ListingActivity::class.java)
            intent.putExtra("isFor", AppConstant.ListingType.GROUP_FOLLOW)
            intent.putExtra("toolbarTitle", R.string.group)
            startActivityForResult(intent, 200)
        }

        swipeRefresh?.setOnRefreshListener {
            when (isFor) {
                AppConstant.ListingType.GROUP_TAG_PRODUCT -> {
                    swipeAction()
                    listingPresenter?.getMyGroups(pagination, false)
                }
                AppConstant.ListingType.GROUP_FOLLOW -> {
                    swipeAction()
                    listingPresenter?.getGroupList(pagination, false)
                }
                AppConstant.ListingType.STORE_LIST -> {
                    swipeAction()
                    getStores(pagination,false,collectionId)
                }
                AppConstant.ListingType.NOTIFICATION_LIST -> {
                    swipeAction()
                    listingPresenter?.getNotifications(pagination, false)
                }
                AppConstant.ListingType.REVIEW_LIST -> {
                    swipeAction()
                    listingPresenter?.getReviewList(getStringData(AppConstant.BundleProperty.PRODUCT_ID),reviewListType,pagination, false)
                }
            }
        }

    }

    private fun getStores(pagination: Int,isLoadMore: Boolean,collectionId:String){
        if(isHomeLocationEnabled()){
            with(LocationHelper.getInstance()){
                checkSettings { success, exception ->
                    if(success){
                        getMyLocation {
                            listingPresenter?.getStores(pagination, isLoadMore,collectionId = collectionId,latitude = it.latitude,longitude = it.longitude)
                        }
                    }
                    else{
                        listingPresenter?.getStores(pagination, isLoadMore,collectionId = collectionId)
                    }
                }
            }
        }
        else{
            listingPresenter?.getStores(pagination, isLoadMore,collectionId = collectionId)
        }
    }

    private fun initData(){
        isFor = intent?.getIntExtra("isFor", 0)
        when(isFor){
            AppConstant.ListingType.STORE_LIST-> collectionId = intent.getStringExtra(AppConstant.BundleProperty.COLLECTION_ID)!!
        }
    }

    private fun swipeAction(){
        swipeRefresh?.isRefreshing = true
        initList()
    }
    private fun initAdapter() {
        when (isFor) {
            AppConstant.ListingType.GROUP_TAG_PRODUCT->{
                grpList?.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
                listingAdapter =
                    ListingAdapter(this, groupList, intent.getIntExtra("isFor", 0), grpList) { position, obj ->
                        val group = groupList[position]
                        if(group.joined){
                            listingPresenter?.addUserToGroup(group.id)
                        }
                        else{
                            listingPresenter?.removeUserFromGroup(group.id)
                        }
                    }
            }
            AppConstant.ListingType.GROUP_FOLLOW -> {
                grpList?.layoutManager = GridLayoutManager(this,2)
                grpList?.addItemDecoration(SpaceGrid(2, 15, true))
                listingAdapter = ListingAdapter(this, groupList, intent.getIntExtra("isFor", 0), grpList) { position, obj ->
                        val group = groupList[position]
                        if(group.joined){
                            listingPresenter?.addUserToGroup(group.id)
                        }
                        else{
                            listingPresenter?.removeUserFromGroup(group.id)
                        }
                    }
            }
            AppConstant.ListingType.STORE_LIST -> {
                grpList?.layoutManager = GridLayoutManager(this,2)
                grpList?.addItemDecoration(SpaceGrid(2, 15, true))
                listingAdapter = ListingAdapter(this, storeList, intent.getIntExtra("isFor", 0), grpList) { position, obj ->
                        val store = storeList[position]
                        if(store.followed){
                            listingPresenter?.followStore(storeList[position].id)
                        }
                        else{
                            listingPresenter?.unFollowStore(store.id)
                        }
                    }
            }
            AppConstant.ListingType.SET_LIST -> {
                grpList?.layoutManager = GridLayoutManager(this,2)
                grpList?.addItemDecoration(SpaceGrid(2, 15, true))
                listingAdapter =
                    ListingAdapter(this, setList, intent.getIntExtra("isFor", 0), grpList) { position, obj ->
                       listingPresenter?.findItemInCart(setList[position].id, AppConstant.CartType.CART_SET){
                          if(!this.isFinishing){
                              val set = setList[position]
                              val dialog = SetVariantDialog()
                              val bundle = Bundle()
                              bundle.putString("set",Gson().toJson(set))
                              bundle.putBoolean("isInCart",it)
                              dialog.arguments = bundle
                              dialog.show(supportFragmentManager, AppConstant.FragmentTag.SET_VARIANT_FRAGMENT)
                          }
                       }
                    }
            }
            AppConstant.ListingType.NOTIFICATION_LIST->{
                grpList?.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
                listingAdapter = ListingAdapter(this, notificationList, intent.getIntExtra("isFor", 0), grpList){position, obj ->
                    val notification = obj as Notification
                    val bundle = Bundle()
                    val referenceId = notification.referenceId.toString()
                    when(notification.referenceType){
                        AppConstant.NotificationType.ACTIVITY_REFERENCE_TYPE_LISTING->{
                            bundle.putString(AppConstant.BundleProperty.PRODUCT_ID,referenceId)
                            startActivity(ProductDetailActivity::class.java,bundle)
                        }
                        AppConstant.NotificationType.ACTIVITY_REFERENCE_TYPE_ORDER->{
                            if(AppController.appController.getUserStore(notification.accountId)!=null){
                                bundle.putString(AppConstant.BundleProperty.ACCOUNT_ID,notification.accountId)
                            }
                            bundle.putString(AppConstant.BundleProperty.ORDER_ID,referenceId)
                            startActivity(OrderDetailActivity::class.java,bundle)
                        }
                        AppConstant.NotificationType.ACTIVITY_REFERENCE_TYPE_ACCOUNT->{
                            bundle.putString(AppConstant.BundleProperty.STORE_NAME,notification.store.storeName)
                            bundle.putString(AppConstant.BundleProperty.ID,referenceId)
                            startActivity(StoreDetailActivity::class.java,bundle)
                        }
                    }
                }
            }
            AppConstant.ListingType.REVIEW_LIST->{
                grpList?.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
                listingAdapter = ListingAdapter(this, reviewList, intent.getIntExtra(AppConstant.BundleProperty.IS_FOR, 0), grpList){ position, obj ->

                }
            }
        }
        listingAdapter?.setOnLoadMoreListener(object : OnLoadMoreListener {
            override fun onLoadMore() {
                if (!isPaginationEnd && NetworkUtil.isConnectingToInternet()) {
                    when (isFor) {
                        AppConstant.ListingType.GROUP_TAG_PRODUCT -> {
                            listingPresenter?.getMyGroups(pagination, true)
                        }
                        AppConstant.ListingType.GROUP_FOLLOW -> {
                            listingPresenter?.getGroupList(pagination, true)
                        }
                        AppConstant.ListingType.STORE_LIST -> {
                            getStores(pagination,true,collectionId)
                        }
                        AppConstant.ListingType.NOTIFICATION_LIST-> listingPresenter?.getNotifications(pagination,true)
                        AppConstant.ListingType.REVIEW_LIST-> listingPresenter?.getReviewList(getStringData(
                            AppConstant.BundleProperty.PRODUCT_ID),reviewListType,pagination,true)
                    }
                }
            }
        })
        grpList?.adapter = listingAdapter
    }

    private fun initList() {
        pagination = 1
        isPaginationEnd = false
    }

    private fun getSelectedListfromString(stringList: String?): List<Group> {
        val baseType = object : TypeToken<List<Group>>() {
        }.type
        return Gson().fromJson<List<Group>>(stringList, baseType)
    }

    override fun onLoadList(list: List<Any>, pagination: Int, isLoadMore: Boolean) {
        swipeRefresh?.isRefreshing = false
        if (list.count() > 0) {
            listingAdapter?.isLoading = false
            hideEmptyState(true)
            when (isFor) {
                AppConstant.ListingType.GROUP_FOLLOW,
                AppConstant.ListingType.GROUP_TAG_PRODUCT -> {
                    if (!isLoadMore) {
                        this.pagination++
                        groupList.clear()
                        groupList.addAll(list as List<Group>)
                    } else {
                        this.pagination = this.pagination + 1
                        groupList.addAll(list as List<Group>)

                    }
                    invalidateOptionsMenu()
                    mergeGroupList(groupList)
                }
                AppConstant.ListingType.STORE_LIST -> {
                    if(!isLoadMore){
                        this.pagination++
                        storeList.clear()
                        storeList.addAll(list as List<Store>)
                    }
                    else{
                        this.pagination = this.pagination + 1
                        storeList.addAll(list as List<Store>)
                    }
                    listingAdapter?.notifyDataSetChanged()
                }
                AppConstant.ListingType.NOTIFICATION_LIST->{
                    if(!isLoadMore){
                        this.pagination++
                        notificationList.clear()
                        notificationList.addAll(list as List<Notification>)
                    }
                    else{
                        this.pagination = this.pagination + 1
                        notificationList.addAll(list as List<Notification>)
                    }
                    listingAdapter?.notifyDataSetChanged()
                }
                AppConstant.ListingType.REVIEW_LIST->{
                    if(!isLoadMore){
                        this.pagination++
                        reviewList.clear()
                        reviewList.addAll(list as List<Review>)
                    }
                    else{
                        this.pagination = this.pagination + 1
                        reviewList.addAll(list as List<Review>)
                    }
                    listingAdapter?.notifyDataSetChanged()
                }
            }
        } else {
            if (!isLoadMore) {
                when(isFor){
                    AppConstant.ListingType.GROUP_FOLLOW,
                    AppConstant.ListingType.GROUP_TAG_PRODUCT->{
                        groupList.clear()

                    }
                    AppConstant.ListingType.STORE_LIST -> {
                        storeList.clear()
                    }
                }
                listingAdapter?.notifyDataSetChanged()
                hideEmptyState(false)
            } else {
                listingAdapter?.isLoading = false
                isPaginationEnd = true
            }
        }

    }

    override fun onFailure(appError: AppError) {

    }

    override fun networkError(msg: String) {

    }

    override fun noNetwork() {

    }

    override fun showProgressLoader() {
        hideEmptyState(true)
        progressBar?.visibility = View.VISIBLE
    }

    override fun hideProgressLoader() {
        progressBar?.visibility = View.GONE
    }

    override fun showProgressDialog() {
        showLoader(R.string.please_wait)
    }

    override fun hideProgressDialog() {
        hideLoader()
    }

    private fun hideEmptyState(shouldHide: Boolean) {
        when (shouldHide) {
            true -> {
                txtEmptyStateMsg.visibility = View.GONE
                txtJoin.visibility = View.GONE
            }
            false -> {
                txtEmptyStateMsg.visibility = View.VISIBLE
                txtJoin.visibility = View.VISIBLE
            }
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        if (AppConstant.ListingType.GROUP_TAG_PRODUCT == intent?.getIntExtra("isFor", 0)) {
            val saveItem = menu?.findItem(R.id.action_save)
            saveItem?.isVisible = !groupList.isEmpty()
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_save) {
            val list = listingAdapter?.getSelectedList()
            sendResultBack(Gson().toJson(list))

        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        listingPresenter?.onDestroy()
    }

    private fun sendResultBack(listString: String?) {
        val intent = Intent()
        intent.putExtra("list", listString)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private fun mergeGroupList(groupList: List<Group>) {
        if (selectedList.count() > 0) {
            for (obj in selectedList) {
                groupList.find { it.id == obj.id }?.isSelected = true
            }
        }
        listingAdapter?.notifyDataSetChanged()
    }
}
