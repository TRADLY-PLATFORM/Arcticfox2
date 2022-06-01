package tradly.social.ui.socialFeed.trending


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager

import tradly.social.R
import tradly.social.adapter.ListingProductAdapter
import tradly.social.domain.entities.AppError
import tradly.social.domain.entities.Product
import tradly.social.common.base.BaseFragment
import tradly.social.ui.login.AuthenticationActivity
import tradly.social.ui.socialFeed.SocialFeedPresenter
import kotlinx.android.synthetic.main.fragment_trending.*
import kotlinx.android.synthetic.main.fragment_trending.view.*
import tradly.social.common.base.*

/**
 * A simple [Fragment] subclass.
 */
class TrendingFragment : BaseFragment(),SocialFeedPresenter.View {

    var mView: View? = null
    lateinit var socialFeedPresenter: SocialFeedPresenter
    var pagination: Int = 1
    var isPaginationEnd: Boolean = false
    var isLoading: Boolean = true
    var productList = mutableListOf<Product>()
    var listingProductAdapter: ListingProductAdapter? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mView =  inflater.inflate(R.layout.fragment_trending, container, false)
        socialFeedPresenter = SocialFeedPresenter(this)
        mView?.recycler_view?.layoutManager = GridLayoutManager(requireContext(),2)
        mView?.recycler_view?.addItemDecoration(SpaceGrid(2, 15, true))
        listingProductAdapter = ListingProductAdapter(requireContext(), productList, mView?.recycler_view)
        listingProductAdapter?.setOnLoadMoreListener(object : OnLoadMoreListener {
            override fun onLoadMore() {
                if (NetworkUtil.isConnectingToInternet() && !isPaginationEnd && !isLoading) {
                    isLoading = true
                    socialFeedPresenter.getSocialFeed(pagination,true)

                }
            }
        })
        mView?.recycler_view?.isNestedScrollingEnabled = false
        mView?.recycler_view?.adapter = listingProductAdapter
        mView?.swipeRefresh?.setOnRefreshListener {
            isLoading = true
            swipeRefresh?.isRefreshing = true
            initList(true)
            socialFeedPresenter.getSocialFeed(pagination,false)

        }
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initList()
        getSocialFeeds()
    }
    private fun initList(isRefreshList:Boolean = false) {
        pagination = 1
        isPaginationEnd = false
        if(!isRefreshList){
            productList.clear()
            listingProductAdapter?.notifyDataSetChanged()
        }
    }

    private fun getSocialFeeds(){
        if(AppController.appController.getUser() != null){
            iconFeed?.visibility = View.GONE
            txtEmptyStateMsg?.visibility = View.GONE
            btnLogin?.visibility = View.GONE
            progressBar?.visibility = View.VISIBLE
            socialFeedPresenter.getSocialFeed(pagination,false)
        }
        else{
            iconFeed?.visibility = View.VISIBLE
            txtEmptyStateMsg?.visibility = View.GONE
            btnLogin?.visibility = View.VISIBLE
            btnLogin?.setOnClickListener {
                val intent = Intent(requireContext(),AuthenticationActivity::class.java)
                intent.putExtra("isFor", AppConstant.LoginFor.SOCIAL)
                startActivityForResult(intent, AppConstant.ActivityResultCode.LOGIN_FROM_SOCIAL)
            }
        }
    }
    override fun hideProgressLoader() {
        progressBar?.visibility = View.GONE
    }

    override fun onLoadList(products: List<Product>, isLoadMore: Boolean) {
        isLoading = false
        swipeRefresh?.isRefreshing = false
        if (products.count() > 0) {
            txtEmptyStateMsg?.visibility = View.GONE
            if (!isLoadMore) {
                pagination++
                productList.clear()
                productList.addAll(products)
                /*  if(baseResult.promoBanners.count()>0){
                      mView?.viewPager?.visibility = View.VISIBLE
                      bannerList.clear()
                      bannerList.addAll(baseResult.promoBanners)
                      bannerAdapter?.notifyDataSetChanged()
                  }*/

            } else {
                this.pagination = this.pagination + 1
                productList.addAll(products)
            }
            listingProductAdapter?.notifyDataSetChanged()
        } else {
            if (!isLoadMore) {
                iconFeed?.visibility = View.VISIBLE
                txtEmptyStateMsg?.visibility = View.VISIBLE
            } else {
                listingProductAdapter?.setLoaded(false)
                isPaginationEnd = true
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == AppConstant.ActivityResultCode.LOGIN_FROM_SOCIAL && resultCode == Activity.RESULT_OK && data != null){
            getSocialFeeds()
        }
    }
    override fun noNetwork() {
        requireContext().showToast(R.string.no_internet)
    }

    override fun onFailure(appError: AppError) {
        ErrorHandler.handleError(requireContext(),appError)
    }

    override fun onDestroy() {
        super.onDestroy()
        socialFeedPresenter.onDestroy()
    }
}
