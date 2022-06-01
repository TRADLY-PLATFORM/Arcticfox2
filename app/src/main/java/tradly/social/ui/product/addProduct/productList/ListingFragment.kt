package tradly.social.ui.product.addProduct.productList


import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_product_list.*

import tradly.social.R
import tradly.social.ui.category.CategoryListActivity
import kotlinx.android.synthetic.main.fragment_product_list.view.*
import kotlinx.android.synthetic.main.fragment_product_list.view.progressBar
import tradly.social.adapter.ListingAdapter
import tradly.social.common.base.*
import tradly.social.common.uiEntity.AdView
import tradly.social.domain.entities.*
import tradly.social.ui.store.storeDetail.StoreDetailActivity


open class ListingFragment : BaseFragment(), ProductListPresenter.View {

    var mView: View? = null
    lateinit var productListPresenter: ProductListPresenter
    var mList = mutableListOf<Any>()
    var categoryId: String? = null
    var categoryName: String? = null
    var collectionId:String = Constant.EMPTY
    var searchKeyword:String = AppConstant.EMPTY
    var listingAdapter: ListingAdapter? = null
    var pagination: Int = 1
    var isLoading: Boolean = true
    var defaultSort: String? = AppConstant.SORT.RECENT
    var isFrom:Int = 0
    var shouldShowSearch:Boolean = false
    lateinit var geoPoint: GeoPoint
    var sortBy:String = AppConstant.EMPTY
    var filterByDistance:String = AppConstant.EMPTY
    var storeId:String = AppConstant.EMPTY
    internal var isPaginationEnd: Boolean = false

    companion object {
        fun newInstance(isFrom: Int , extras:Bundle?=null): ListingFragment {
            val args = Bundle()
            args.putInt(AppConstant.BundleProperty.IS_FROM, isFrom)
            if(extras!=null){
                args.putBundle(AppConstant.BundleProperty.EXTRAS,extras)
            }
            val fragment = ListingFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (mView != null) {
            return mView
        }
        mView = inflater.inflate(R.layout.fragment_product_list, container, false)
        productListPresenter = ProductListPresenter(this)
        initData()
        initView()
        initAdapter()
        initList(true)
        when(isFrom){
            AppConstant.ListingType.PRODUCT_LIST,
            AppConstant.ListingType.PRODUCT_BY_SEARCH,
            AppConstant.ListingType.STORE_BY_SEARCH,
            AppConstant.ListingType.STORE_PRODUCT_LIST,
            AppConstant.ListingType.STORE_FEEDS,
            AppConstant.ListingType.STORE_FEEDS_BLOCK->{
                if(isFrom== AppConstant.ListingType.PRODUCT_LIST
                    || isFrom == AppConstant.ListingType.STORE_PRODUCT_LIST
                    || isFrom == AppConstant.ListingType.STORE_FEEDS
                    || isFrom == AppConstant.ListingType.STORE_FEEDS_BLOCK){

                    showProgressLoader()
                }
                getData(false)
            }
        }
        return mView
    }

    private fun initData(){
        isFrom = arguments!!.getInt(AppConstant.BundleProperty.IS_FROM)
        if(isFrom== AppConstant.ListingType.PRODUCT_LIST){
            categoryId = arguments?.getString(AppConstant.BundleProperty.CATEGORY_ID)
            categoryName = arguments?.getString(AppConstant.BundleProperty.CATEGORY_NAME)
            collectionId = requireArguments().getStringOrEmpty(AppConstant.BundleProperty.COLLECTION_ID)
        }
    }

    private fun initView(){
        when(isFrom){
            AppConstant.ListingType.PRODUCT_LIST->{
                mView?.txtEmptyStateMsg?.text = getString(R.string.productlist_no_products_found)
                mView?.txtEmptyStateMsg?.typeface = Typeface.DEFAULT
                mView?.txtEmptyStateMsg?.setTextColor(ContextCompat.getColor(requireContext(),R.color.colorDarkGrey))
            }
            AppConstant.ListingType.PRODUCT_BY_SEARCH->{
                mView?.txtEmptyStateMsg?.visibility = View.VISIBLE
                mView?.txtEmptyStateMsg?.text = getString(R.string.search_search_a_product)
                mView?.txtEmptyStateMsg?.typeface = Typeface.DEFAULT_BOLD
                mView?.searchProductImage?.visibility = View.VISIBLE
            }
            AppConstant.ListingType.STORE_BY_SEARCH->{
                mView?.txtEmptyStateMsg?.visibility = View.VISIBLE
                mView?.txtEmptyStateMsg?.text = getString(R.string.search_search_a_store)
                mView?.txtEmptyStateMsg?.typeface = Typeface.DEFAULT_BOLD
                mView?.searchProductImage?.visibility = View.VISIBLE
            }
            AppConstant.ListingType.STORE_PRODUCT_LIST->{
                mView?.txtEmptyStateMsg?.text = getString(R.string.productlist_no_products_found)
                mView?.txtEmptyStateMsg?.typeface = Typeface.DEFAULT
                mView?.txtEmptyStateMsg?.setTextColor(ContextCompat.getColor(requireContext(),R.color.colorDarkGrey))
            }
            AppConstant.ListingType.STORE_FEEDS->{
                mView?.txtEmptyStateMsg?.text = getString(R.string.storelist_no_stores_found)
                mView?.txtEmptyStateMsg?.typeface = Typeface.DEFAULT
                mView?.txtEmptyStateMsg?.setTextColor(ContextCompat.getColor(requireContext(),R.color.colorDarkGrey))
            }
            AppConstant.ListingType.STORE_FEEDS_BLOCK->{
                mView?.txtEmptyStateMsg?.text = getString(R.string.storelist_no_stores_blocked)
                mView?.txtEmptyStateMsg?.typeface = Typeface.DEFAULT
                mView?.txtEmptyStateMsg?.setTextColor(ContextCompat.getColor(requireContext(),R.color.colorDarkGrey))
            }
        }
    }

    private fun initAdapter(){
        val layoutManager = if (isFrom == AppConstant.ListingType.STORE_FEEDS || isFrom == AppConstant.ListingType.STORE_FEEDS_BLOCK){
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        }
        else{
            mView?.recycler_view?.addItemDecoration(SpaceGrid(2, 15, true))
            GridLayoutManager(requireContext(), 2)
        }
        mView?.recycler_view?.layoutManager = layoutManager
        val listingType = when (isFrom) {
            AppConstant.ListingType.PRODUCT_LIST,
            AppConstant.ListingType.PRODUCT_BY_SEARCH,
            AppConstant.ListingType.STORE_PRODUCT_LIST -> AppConstant.ListingType.PRODUCT_LIST
            AppConstant.ListingType.STORE_BY_SEARCH -> AppConstant.ListingType.STORE_LIST
            AppConstant.ListingType.STORE_FEEDS -> AppConstant.ListingType.STORE_FEEDS
            AppConstant.ListingType.STORE_FEEDS_BLOCK-> AppConstant.ListingType.STORE_FEEDS_BLOCK
            else-> AppConstant.ListingType.STORE_LIST
        }
        listingAdapter = ListingAdapter(requireContext(), mList,listingType, mView?.recycler_view){ position, obj ->
           if(isFrom == AppConstant.ListingType.STORE_FEEDS_BLOCK){
               productListPresenter.unBlockStore((obj as Store).id)
           }
           else if (isFrom == AppConstant.ListingType.STORE_FEEDS){
               val store = (obj as Store)
               val intent = Intent(requireContext(), StoreDetailActivity::class.java)
               intent.putExtra("storeName",store.storeName)
               intent.putExtra("id",store.id)
               startActivityForResult(intent,StoreDetailActivity.REQUEST_CODE)
           }
           else{
               val store = mList[position] as Store
               if (store.followed) {
                   productListPresenter.followStore(store.id)
               } else {
                   productListPresenter.unFollowStore(store.id)
               }
           }
        }
        mView?.nestedScrollView?.setOnScrollChangeListener { v: NestedScrollView, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int ->
            if(v.getChildAt(v.childCount - 1) != null){
                if ((scrollY >= (v.getChildAt(v.childCount - 1).measuredHeight - v.measuredHeight)) &&
                    scrollY > oldScrollY){
                    val visibleItemCount = layoutManager.childCount
                    val totalItemCount = layoutManager.itemCount
                    val pastVisiblesItems = layoutManager.findFirstVisibleItemPosition()
                    if (!isLoading && (visibleItemCount + pastVisiblesItems) >= totalItemCount){
                        if (NetworkUtil.isConnectingToInternet() && !isPaginationEnd){
                            isLoading = true
                            getData(true)
                        }
                    }
                }
            }
        }
        mView?.recycler_view?.isNestedScrollingEnabled = false
        mView?.recycler_view?.adapter = listingAdapter
        if(isFrom!= AppConstant.ListingType.STORE_PRODUCT_LIST){
            mView?.swipeRefresh?.setOnRefreshListener {
                if(isFrom == AppConstant.ListingType.PRODUCT_LIST || searchKeyword.isNotEmpty()){
                    isLoading = true
                    swipeRefresh?.isRefreshing = true
                    initList(false)
                    getData(false)
                }
                else{
                    swipeRefresh?.isRefreshing = false
                }
            }
        }
        else{
            mView?.swipeRefresh?.isEnabled = false
        }
    }

    private fun  getData(isLoadMore: Boolean){
        when(isFrom){
            AppConstant.ListingType.PRODUCT_LIST->{
                if(isHomeLocationEnabled()){
                    with(LocationHelper.getInstance()){
                        checkSettings { success, exception ->
                            if(success){
                                getMyLocation {
                                    productListPresenter.getProductList(categoryId, defaultSort, pagination, isLoadMore,collectionId = collectionId,latitude = it.latitude,longitude = it.longitude,allowLocation = true)
                                }
                            }
                            else{
                                productListPresenter.getProductList(categoryId, defaultSort, pagination, isLoadMore,collectionId = collectionId)
                            }
                        }
                    }
                }
                else{
                    productListPresenter.getProductList(categoryId, defaultSort, pagination, isLoadMore,collectionId = collectionId)
                }
            }
            AppConstant.ListingType.STORE_BY_SEARCH->{
                if(searchKeyword.isNotEmpty()){
                    productListPresenter.getStoreList(pagination,isLoadMore,
                        AppConstant.SORT.RECENT,searchKeyword)
                }
            }
            AppConstant.ListingType.PRODUCT_BY_SEARCH->{
                if(searchKeyword.isNotEmpty() && ::geoPoint.isInitialized){
                    productListPresenter.getProductList(categoryId, sortBy, pagination, isLoadMore,key = searchKeyword,latitude = geoPoint.latitude,longitude = geoPoint.longitude,filterRadius = filterByDistance)
                }
            }
            AppConstant.ListingType.STORE_PRODUCT_LIST->{
                if(storeId.isNotEmpty()){
                    productListPresenter.getStoreProducts(storeId,isLoadMore,pagination)
                }
            }
            AppConstant.ListingType.STORE_FEEDS-> productListPresenter.getStoreFeeds(AppConstant.AccountFeedType.FOLLOWING,pagination,isLoadMore)
            AppConstant.ListingType.STORE_FEEDS_BLOCK-> productListPresenter.getStoreFeeds(
                AppConstant.AccountFeedType.BLOCKED,pagination,isLoadMore)

        }

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(isFrom == AppConstant.ListingType.PRODUCT_LIST){
            (activity as? CategoryListActivity)?.supportActionBar?.title = categoryName
        }
    }

    private fun initList(clearData:Boolean) {
        pagination = 1
        isPaginationEnd = false
        if(clearData){
            mList.clear()
            listingAdapter?.notifyDataSetChanged()
        }
    }

    fun setFilters(loadMore:Boolean,shouldShowSearch:Boolean,searchKey:String,sort:String,geoPoint: GeoPoint?,filterByDistance:String,categoryId:String){
        this.sortBy = sort
        this.categoryId = categoryId
        this.searchKeyword = searchKey
        this.shouldShowSearch = shouldShowSearch
        if(geoPoint!=null){
            this.geoPoint = geoPoint
            this.filterByDistance = filterByDistance
        }
        if(!loadMore){
            initList(true)
            if(shouldShowSearch && searchKey.isNotEmpty()){
                showProgressLoader()
            }
        }
        getData(loadMore)
    }

    fun refresh(){
        isLoading = false
        if(isFrom == AppConstant.ListingType.STORE_PRODUCT_LIST){
            with(arguments!!){
                if(this.containsKey(AppConstant.BundleProperty.EXTRAS)){
                    storeId = this.getBundle(AppConstant.BundleProperty.EXTRAS).getStringOrEmpty(
                        AppConstant.BundleProperty.STORE_ID)
                    initList(false)
                    getData(false)
                }
            }
        }
    }

    override fun hideProgressLoader() {
        progressBar?.visibility = View.GONE
    }

    override fun showProgressLoader() {
        mView?.progressBar?.visibility = View.VISIBLE
        mView?.searchProductImage?.visibility = View.GONE
        mView?.txtEmptyStateMsg?.visibility = View.GONE
    }

    override fun onStoreBlockStatus(storeId:String) {
        if (mList.isNotEmpty()){
            val list = mList as MutableList<Store>
            list.indexOfFirst { it.id ==storeId }.takeIf { it!=-1 }?.let { index->
                mList.removeAt(index)
                listingAdapter?.notifyItemRemoved(index)
            }
        }
    }

    override fun showProgressDialog() {
        showLoader(R.string.please_wait)
    }

    override fun hideProgressDialog() {
        hideLoader()
    }

    override fun onLoadList(any: Any, isLoadMore: Boolean) {
        isLoading = false
        swipeRefresh?.isRefreshing = false
        val list = when(isFrom){
            AppConstant.ListingType.PRODUCT_LIST,
            AppConstant.ListingType.PRODUCT_BY_SEARCH,
            AppConstant.ListingType.STORE_PRODUCT_LIST->any as List<Product>
            AppConstant.ListingType.STORE_BY_SEARCH,
            AppConstant.ListingType.STORE_FEEDS,
            AppConstant.ListingType.STORE_FEEDS_BLOCK-> any as List<Store>
            else-> listOf()
        }
        if (list.count() > 0) {
            txtEmptyStateMsg?.visibility = View.GONE
            if (!isLoadMore) {
                pagination++
                mList.clear()
                mList.addAll(list)
                listingAdapter?.notifyDataSetChanged()
            } else {
                this.pagination = this.pagination + 1
                val index = mList.size
                mList.addAll(list)
                listingAdapter?.notifyItemRangeInserted(index,mList.size)
            }
            if (isFrom == AppConstant.ListingType.PRODUCT_LIST){
                if (AppConfigHelper.getConfigKey<Boolean>(AppConfigHelper.Keys.LISTINGS_ADMOB_ENABLED) &&
                    AppConfigHelper.getConfigKey<String>(AppConfigHelper.Keys.LISTINGS_ADMOB_ID).isNotEmpty() &&
                    getString(R.string.admob_appid).isNotEmpty()){
                    mList.add(AdView())
                    listingAdapter?.notifyItemInserted(mList.size-1)
                }
            }
            listingAdapter?.setLoaded()
        } else {
            if (!isLoadMore) {
                showEmptyStateMessage()
            } else {
                listingAdapter?.setLoaded()
                isPaginationEnd = true
            }
        }
    }

    private fun showEmptyStateMessage(){
        mView?.txtEmptyStateMsg?.visibility = View.VISIBLE
        mView?.txtEmptyStateMsg?.typeface = Typeface.DEFAULT
        mView?.txtEmptyStateMsg?.setTextColor(ContextCompat.getColor(requireContext(),R.color.colorDarkGrey))
        when(isFrom){
            AppConstant.ListingType.PRODUCT_LIST,
            AppConstant.ListingType.PRODUCT_BY_SEARCH,
            AppConstant.ListingType.STORE_PRODUCT_LIST->mView?.txtEmptyStateMsg?.text = getString(R.string.productlist_no_products_found)
            AppConstant.ListingType.STORE_BY_SEARCH->mView?.txtEmptyStateMsg?.text = getString(R.string.storelist_no_stores_found)
        }
    }

    override fun noNetwork() {
        requireContext().showToast(R.string.no_internet)
    }

    override fun onFailure(appError: AppError) {
        ErrorHandler.handleError(requireContext(),appError)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(StoreDetailActivity.REQUEST_CODE == requestCode){
            isPaginationEnd = false
            pagination = 1
            getData(false)
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        productListPresenter.onDestroy()
    }
}

