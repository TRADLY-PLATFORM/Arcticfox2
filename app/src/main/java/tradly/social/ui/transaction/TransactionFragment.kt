package tradly.social.ui.transaction


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_transaction.view.*
import kotlinx.android.synthetic.main.fragment_transaction.*

import tradly.social.R
import tradly.social.adapter.ListingAdapter
import tradly.social.common.base.AppConstant
import tradly.social.common.base.NetworkUtil
import tradly.social.common.base.OnLoadMoreListener
import tradly.social.common.base.startActivity
import tradly.social.common.base.ErrorHandler
import tradly.social.domain.entities.AppError
import tradly.social.domain.entities.Earning
import tradly.social.domain.entities.Transaction
import tradly.social.common.base.BaseFragment
import tradly.social.ui.orders.OrderDetailActivity

/**
 * A simple [Fragment] subclass.
 */
class TransactionFragment : BaseFragment(),TransactionPresenter.View {

    private lateinit var presenter: TransactionPresenter
    lateinit var listAdapter: ListingAdapter
    var mView:View?=null
    var pagination: Int = 1
    var isPaginationEnd: Boolean = false
    var transactionList = mutableListOf<Transaction>()
    lateinit var accountId:String
    var isForPayoutList:Boolean = false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if(mView!=null)return mView
        mView = inflater.inflate(R.layout.fragment_transaction, container, false)
        accountId = arguments!!.getString(AppConstant.BundleProperty.ACCOUNT_ID)!!
        isForPayoutList = arguments!!.getBoolean(AppConstant.BundleProperty.IS_FOR_PAYOUT_LIST,false)
        initView()
        presenter = TransactionPresenter(this)
        presenter.getTransactions(pagination,accountId,isForPayoutList,false)
        return mView
    }

    fun initView(){
        mView?.recycler_view?.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        listAdapter = ListingAdapter(requireContext(),transactionList,
            AppConstant.ListingType.TRANSACTION_LIST,mView?.recycler_view){ position, obj ->
            val arg = Bundle().apply {
                putString(AppConstant.BundleProperty.ORDER_ID,(obj as Transaction).orderId.toString())
                putString(AppConstant.BundleProperty.ACCOUNT_ID,accountId)
                putBoolean(AppConstant.BundleProperty.IS_MY_STORE_ORDER,true)
            }
            context!!.startActivity(OrderDetailActivity::class.java,arg)
        }
        listAdapter.setOnLoadMoreListener(object : OnLoadMoreListener {
            override fun onLoadMore() {
                if (!isPaginationEnd && NetworkUtil.isConnectingToInternet(true)) {
                    presenter.getTransactions(pagination,accountId,isForPayoutList,true)
                }
            }
        })
        mView?.recycler_view?.adapter = listAdapter
    }

    fun initList(){
        isPaginationEnd = false;
        pagination = 1
        transactionList.clear()
    }

    override fun showBalance(earning: Earning?) {
        (activity as? TransactionHostActivity)?.showBalance(earning)
    }

    override fun onTransactionList(list: List<Transaction>,isLoadMore:Boolean) {
        if(isLoadMore){
            if(list.isEmpty()){
                isPaginationEnd = true
            }
            else{
                pagination++
                transactionList.addAll(list)
                listAdapter.notifyDataSetChanged()
            }
        }
        else{
            if(list.isEmpty()){
                isPaginationEnd = true
                txtEmptyStateMsg?.text = String.format(getString(R.string.sales_no_transaction),getString(if(!isForPayoutList)R.string.sales_transactions else R.string.sales_payouts))
                txtEmptyStateMsg?.visibility = View.VISIBLE
            }
            else{
                pagination++
                transactionList.addAll(list)
                listAdapter.notifyDataSetChanged()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as? TransactionHostActivity)?.let {
            it.setToolbar(if(isForPayoutList)R.string.sales_payouts else R.string.sales_transactions)
        }
    }
    override fun showProgressLoader() {
        progress_bar?.visibility = View.VISIBLE
    }

    override fun hideProgressLoader() {
        progress_bar?.visibility = View.GONE
    }
    override fun onError(appError: AppError) {
        ErrorHandler.handleError(exception = appError)
    }

}
