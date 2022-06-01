package tradly.social.ui.orders


import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.moe.pushlibrary.utils.MoEHelperUtils.showToast
import kotlinx.android.synthetic.main.fragment_my_order.*
import tradly.social.R
import tradly.social.adapter.MyOrderRecyclerAdapter
import tradly.social.common.base.*
import tradly.social.domain.entities.AppError
import tradly.social.domain.entities.Constant
import tradly.social.domain.entities.Order
import tradly.social.common.base.BaseFragment

class MyOrderFragment : BaseFragment(), MyOrderPresenter.View,MyOrderRecyclerAdapter.OnClickListener {

    private lateinit var myOrderPresenter: MyOrderPresenter
    private lateinit var adapter: MyOrderRecyclerAdapter
    private var pageNo: Int = 1
    var isPaginationEnd: Boolean = false
    private var orderList= mutableListOf<Order>()
    var startDate:String = Constant.EMPTY
    var endDate:String = Constant.EMPTY
    var accountId:String = Constant.EMPTY
    var orderStatus:Int = 0
    companion object{
        fun newInstance(isUserStoreOrders:Boolean ,category:String):MyOrderFragment{
            val bundle = Bundle().apply {
                putString(AppConstant.BundleProperty.IS_FOR,category)
                putBoolean(AppConstant.BundleProperty.IS_MY_STORE_ORDER,isUserStoreOrders)
                DateTimeHelper.getTimeRange(category).apply {
                    putString(AppConstant.BundleProperty.START_DATE,this.first)
                    putString(AppConstant.BundleProperty.END_DATE,this.second)
                }
            }
            val fragment = MyOrderFragment()
            fragment.arguments = bundle
            return fragment
        }
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        myOrderPresenter = MyOrderPresenter(this)
        arguments?.let {
            startDate = it.getString(AppConstant.BundleProperty.START_DATE, AppConstant.EMPTY)
            endDate = it.getString(AppConstant.BundleProperty.END_DATE, AppConstant.EMPTY)
            if(it.getBoolean(AppConstant.BundleProperty.IS_MY_STORE_ORDER)){
                val store = AppController.appController.getUserStore()
                store?.let { s->
                    accountId = s.id
                }
            }
        }
        return inflater.inflate(R.layout.fragment_my_order, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initAdapter()
        initList()
        arguments?.let {
            myOrderPresenter.getOrders(pageNo,accountId,startDate,endDate,false,this.orderStatus)
        }
    }

    fun getOrders(orderStatus:Int){
        if(::myOrderPresenter.isInitialized){
            this.orderStatus = orderStatus
            initList()
            txtEmptyStateMsg?.visibility = View.GONE
            myOrderPresenter.getOrders(pageNo,accountId,startDate,endDate,false,this.orderStatus)
        }
    }

    private fun initAdapter(){
        adapter = MyOrderRecyclerAdapter(requireContext(),LayoutInflater.from(context),orderList,recyclerViewOrders,this)
        recyclerViewOrders?.layoutManager = LinearLayoutManager(context)
        adapter.setOnLoadMoreListener(object : OnLoadMoreListener {
            override fun onLoadMore() {
                if(!isPaginationEnd && NetworkUtil.isConnectingToInternet(true)){
                    myOrderPresenter.getOrders(pageNo,accountId,startDate,endDate,true,orderStatus)
                }
            }
        })
        recyclerViewOrders?.adapter = adapter
    }

    private fun initList(){
        pageNo = 1
        orderList.clear()
        adapter.notifyDataSetChanged()
    }

    override fun showProgressLoader() {
        progressBar?.visibility = View.VISIBLE
    }

    override fun hideProgressLoader() {
        progressBar?.visibility = View.GONE
    }

    override fun showProgressDialogLoader() {
        showLoader(R.string.please_wait)
    }

    override fun hideProgressDialogLoader() {
        hideLoader()
    }

    override fun onFailure(appError: AppError) {
        ErrorHandler.handleError(context ,appError)
        showToast(getString(R.string.something_went_wrong),context)
    }

    override fun getOrders(order: List<Order>, isLoadMore:Boolean) {
        if(!isLoadMore){
            if(order.isNotEmpty()){
                pageNo++
                orderList.clear()
                orderList.addAll(order)
            }
            else{
                isPaginationEnd = true
                txtEmptyStateMsg?.visibility = View.VISIBLE
            }
        }
        else{
            if(order.isNotEmpty()){
                pageNo++
                orderList.addAll(order)
            }
            else{
                isPaginationEnd = true
            }
        }
        adapter.notifyDataSetChanged()
        adapter.setLoaded()
    }

    override fun onClick(id: String) {
        val intent = Intent(context,OrderDetailActivity::class.java)
        intent.putExtra(AppConstant.BundleProperty.ORDER_ID,id)
        intent.putExtra(AppConstant.BundleProperty.ACCOUNT_ID,accountId)
        intent.putExtra(
            AppConstant.BundleProperty.IS_MY_STORE_ORDER,arguments?.getBoolean(
                AppConstant.BundleProperty.IS_MY_STORE_ORDER))
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        myOrderPresenter.onDestroy()
    }
}
