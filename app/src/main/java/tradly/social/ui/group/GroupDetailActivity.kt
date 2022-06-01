package tradly.social.ui.group

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import tradly.social.R
import tradly.social.adapter.HorizontalMemberAdapter
import tradly.social.adapter.ListingProductAdapter
import tradly.social.adapter.TabAdapter
import tradly.social.common.*
import tradly.social.domain.entities.*
import tradly.social.common.base.BaseActivity
import kotlinx.android.synthetic.main.activity_group_detail.*
import kotlinx.android.synthetic.main.activity_group_detail.progress_circular
import kotlinx.android.synthetic.main.activity_group_detail.recycler_view
import kotlinx.android.synthetic.main.activity_group_detail.txtEmptyStateMsg
import kotlinx.android.synthetic.main.toolbar.*
import tradly.social.common.base.*

class GroupDetailActivity : BaseActivity(), GroupDetailPresenter.View {

    var pagination: Int = 0
    var isLoading: Boolean = true
    var defaultSort: String? = AppConstant.SORT.PRICE_LOW_HIGH
    internal var isPaginationEnd: Boolean = false
    private var groupDetailPresenter: GroupDetailPresenter? = null
    private var tabAdapter: TabAdapter? = null
    private var productList = mutableListOf<Product>()
    private var memberList = mutableListOf<User>()
    private var listingProductAdapter: ListingProductAdapter? = null
    private var memberListAdapter: HorizontalMemberAdapter? = null
    private var group: Group? = null
    private val btnJoin:Int by lazy { ThemeUtil.getResourceDrawable(this, R.attr.buttonGradientBg) }
    private val colorPrimary:Int by lazy { ThemeUtil.getResourceValue(this, R.attr.colorPrimary) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_detail)
        setToolbar(toolbar, R.string.group_information, R.drawable.ic_back)
        groupDetailPresenter = GroupDetailPresenter(this)
        tabAdapter = TabAdapter(this, supportFragmentManager)
        recycler_view.layoutManager = GridLayoutManager(this, 2)
        recycler_view.addItemDecoration(SpaceGrid(2, 15, true))
        listingProductAdapter = ListingProductAdapter(this, productList,recycler_view)
        listingProductAdapter?.setOnLoadMoreListener(object: OnLoadMoreListener {
            override fun onLoadMore() {
                if(NetworkUtil.isConnectingToInternet() && !isPaginationEnd && !isLoading){
                    isLoading = true
                    groupDetailPresenter?.getGroup(intent.getStringExtra("groupId"),defaultSort,pagination,false,true)

                }
            }
        })
        memberListAdapter = HorizontalMemberAdapter(this, memberList)
        horZMembersList?.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        horZMembersList?.addItemDecoration(OverlapDecoration())
        horZMembersList?.adapter = memberListAdapter
        recycler_view.adapter = listingProductAdapter
        actionBtn?.setOnClickListener {
            actionJoin(group)
        }
        initList()
        progress_circular?.visibility = View.VISIBLE
        intent?.let {
            groupDetailPresenter?.getGroup(intent.getStringExtra("groupId"),defaultSort,pagination,false,false)
        }

        actionBtn?.setOnClickListener {
            group?.joined?.let {
                if(it){
                    group?.joined = !it
                    groupDetailPresenter?.removeUserFromGroup(group?.id)
                }
                else{
                    group?.joined = !it
                    groupDetailPresenter?.addUserToGroup(group?.id)
                }
                actionBtn?.setBackgroundResource(if (!it) R.drawable.bg_button_outline else btnJoin)
                txtActionBtn?.text = getString(if (!it) R.string.group_joined else R.string.group_join)
                txtActionBtn?.setTextColor(
                    ContextCompat.getColor(
                        this,
                        if (!it) colorPrimary else R.color.colorWhite
                    )
                )
            }
        }
    }

    override fun showProgressLoader() {

    }

    override fun hideProgressLoader() {
        progress_circular?.visibility = View.GONE
    }

    override fun onSuccess(result: BaseResult, isLoadMore: Boolean) {
        isLoading = false
        nestedScrollview?.visibility = View.VISIBLE
        if(!isLoadMore){
            updateView(result.group)
            if (result.products.count() > 0) {
                pagination = 1
                productList.clear()
                productList.addAll(result.products)
                txtMarketPlace?.visibility = View.VISIBLE
                viewOne?.visibility = View.VISIBLE
            } else {
                txtEmptyStateMsg?.visibility = View.VISIBLE
                listingProductAdapter?.setLoaded(false)
            }
            groupDetailPresenter?.getMembers(result.group.id)
            listingProductAdapter?.notifyDataSetChanged()
        } else {
            if (result.products.count() > 0) {
                this.pagination = this.pagination+1
                productList.addAll(result.products)
            }
            else{
                listingProductAdapter?.setLoaded(false)
                isPaginationEnd = true
            }
            listingProductAdapter?.notifyDataSetChanged()
        }
    }

    private fun updateView(group: Group) {
        this.group = group
        ImageHelper.getInstance().showImage(this, group.groupPic, coverImage, R.drawable.placeholder_image, R.drawable.placeholder_image)
        grpName?.text = group.groupName
        txtMembers?.text = String.format(getString(R.string.group_members), group.membersCount)
        txtAbout?.text = group.groupDescription
        txtLocation?.text = group.groupAddress
        txtDate?.text = DateTimeHelper.TimeAgo.getTimeAgo(this, group.createdAt.toString())
        appBarLayout?.visibility = View.VISIBLE
        actionBtn?.setBackgroundResource(if (group.joined) R.drawable.bg_button_outline else btnJoin)
        txtActionBtn?.text = getString(if (group.joined) R.string.group_joined else R.string.group_join)
        txtActionBtn?.setTextColor(
            ContextCompat.getColor(
                this,
                if (group.joined) colorPrimary else R.color.colorWhite
            )
        )
    }

    private fun actionJoin(group: Group?){
        group?.let {
            if(it.joined){
                groupDetailPresenter?.removeUserFromGroup(it.id)
            }
            else{
                groupDetailPresenter?.addUserToGroup(it.id)
            }
            it.joined =!it.joined
            changeJoinBtn(it.joined)
        }
    }
    private fun changeJoinBtn(isJoined: Boolean) {
        if (isJoined) {
            txtActionBtn?.setTextColor(ContextCompat.getColor(this, colorPrimary))
            actionBtn?.setBackgroundResource(R.drawable.bg_button_outline)
            txtActionBtn?.text = getString(R.string.group_joined)
        } else {
            txtActionBtn?.setTextColor(ContextCompat.getColor(this, R.color.colorWhite))
            actionBtn?.setBackgroundResource(btnJoin)
            txtActionBtn?.text = getString(R.string.group_join)
        }
    }

    override fun onFailure(appError: AppError) {
        ErrorHandler.handleError(this, appError)
    }

    override fun noNetwork() {

    }

    override fun loadRelatedProduct(list: List<Product>) {
        progress_circular_small?.visibility = View.GONE
        productList.clear()
        productList.addAll(list)
        listingProductAdapter?.notifyDataSetChanged()
    }

    override fun loadMembers(list: List<User>) {
        if (list.count() > 0) {
            members?.visibility = View.VISIBLE
            horZMembersList?.visibility = View.VISIBLE
            members?.text = String.format(getString(R.string.group_members_data), group?.membersCount)
            memberList.clear()
            memberList.addAll(list)
            memberListAdapter?.notifyDataSetChanged()
        }
    }

    fun initList() {
        pagination = 0
        isLoading = true
        isPaginationEnd = false
        productList.clear()
    }

    override fun onDestroy() {
        super.onDestroy()
        group?.let {
            groupDetailPresenter?.onDestroy(group?.id)
        }
    }
}
