package tradly.social.ui.wishlist

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_wish_list.*
import kotlinx.android.synthetic.main.activity_wish_list.btnLogin
import kotlinx.android.synthetic.main.activity_wish_list.progressBar
import kotlinx.android.synthetic.main.activity_wish_list.recycler_view
import kotlinx.android.synthetic.main.activity_wish_list.swipeRefresh
import kotlinx.android.synthetic.main.toolbar.*
import tradly.social.R
import tradly.social.adapter.ListingProductAdapter
import tradly.social.common.*
import tradly.social.common.base.*
import tradly.social.domain.entities.AppError
import tradly.social.domain.entities.Product
import tradly.social.common.base.BaseActivity
import tradly.social.ui.login.AuthenticationActivity
import tradly.social.ui.product.addProduct.productList.ProductListPresenter

class WishListActivity : BaseActivity(),ProductListPresenter.View {

    lateinit var productListPresenter:ProductListPresenter
    lateinit var listingProductAdapter:ListingProductAdapter
    var pagination: Int = 1
    var isPaginationEnd: Boolean = false
    var isLoading: Boolean = true
    var productList = mutableListOf<Product>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wish_list)
        setToolbar(toolbar,R.string.wishlist_header_title,R.drawable.ic_back)
        productListPresenter = ProductListPresenter(this)
        recycler_view.layoutManager = LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)
        listingProductAdapter = ListingProductAdapter(this, productList, recycler_view , isFor = AppConstant.ListingType.WISH_LIST)
        listingProductAdapter.setOnLoadMoreListener(object : OnLoadMoreListener {
            override fun onLoadMore() {
                if (NetworkUtil.isConnectingToInternet() && !isPaginationEnd && !isLoading) {
                    isLoading = true
                    productListPresenter.getWishList(pagination,true)
                }
            }
        })

        recycler_view?.adapter = listingProductAdapter
        swipeRefresh?.setOnRefreshListener {
            isLoading = true
            swipeRefresh?.isRefreshing = true
            initList(true)
            productListPresenter.getWishList(pagination,false)
        }

        btnLogin?.setOnClickListener {
            if(AppController.appController.getUser() == null){
                val intent = Intent(this, AuthenticationActivity::class.java)
                intent.putExtra("isFor", AppConstant.LoginFor.SOCIAL)
                startActivityForResult(intent, AppConstant.ActivityResultCode.LOGIN_FROM_WISH_LIST)
                return@setOnClickListener
            }
            finish()
        }

        getWishList()
    }

    private fun getWishList(){
        if(AppController.appController.getUser() != null){
            llEmptyState?.visibility = View.GONE
            btnLogin?.visibility = View.GONE
            progressBar?.visibility = View.VISIBLE
            initList()
            productListPresenter.getWishList(pagination,false)
        }
        else{
            llEmptyState?.visibility = View.VISIBLE
            btnLogin?.visibility = View.VISIBLE
            btnLoginTxt?.text = getString(R.string.login_to_continue)
        }
    }

    private fun initList(isRefreshList:Boolean = false) {
        pagination = 1
        isPaginationEnd = false
        if(!isRefreshList){
            productList.clear()
            listingProductAdapter.notifyDataSetChanged()
        }
    }

    override fun onLoadList(any: Any, isLoadMore: Boolean) {
        isLoading = false
        val products = any as List<Product>
        swipeRefresh?.isRefreshing = false
        if (products.count() > 0) {
            llEmptyState?.visibility = View.GONE
            if (!isLoadMore) {
                pagination++
                productList.clear()
                productList.addAll(products)
            } else {
                this.pagination = this.pagination + 1
                productList.addAll(products)
            }
            listingProductAdapter.notifyDataSetChanged()
        } else {
            if (!isLoadMore) {
                llEmptyState?.visibility = View.VISIBLE
                btnLogin?.visibility = View.VISIBLE
                btnLoginTxt?.text = getString(R.string.wishlist_continue_wish_list)
            } else {
                listingProductAdapter.setLoaded(false)
                isPaginationEnd = true
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && requestCode == AppConstant.ActivityResultCode.LOGIN_FROM_WISH_LIST && data!= null){
            getWishList()
        }
    }

    override fun noNetwork() {
        showToast(R.string.no_internet)
    }
    override fun onFailure(appError: AppError) {
        ErrorHandler.handleError(this,appError)
    }

    override fun showProgressLoader() {

    }
    override fun hideProgressLoader() {
        progressBar?.visibility = View.GONE
    }

    override fun onStoreBlockStatus(storeId:String) {

    }

    override fun showProgressDialog() {
        showLoader(R.string.please_wait)
    }

    override fun hideProgressDialog() {
        hideLoader()
    }

    override fun onDestroy() {
        super.onDestroy()
        productListPresenter.onDestroy()
    }
}
