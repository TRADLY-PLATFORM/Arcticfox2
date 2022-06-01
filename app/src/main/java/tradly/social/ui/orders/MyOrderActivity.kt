package tradly.social.ui.orders

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.activity_my_order.*
import kotlinx.android.synthetic.main.toolbar.*
import tradly.social.R
import tradly.social.adapter.TabViewAdapter
import tradly.social.common.base.AppConstant
import tradly.social.common.base.DateTimeHelper
import tradly.social.common.base.BaseActivity

class MyOrderActivity : BaseActivity() {

    lateinit var fragmentAll:MyOrderFragment
    lateinit var fragmentWeek:MyOrderFragment
    lateinit var fragmentMonth:MyOrderFragment
    lateinit var fragmentYear:MyOrderFragment
    var shouldApplyFilter:Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_order)
        var titleId = R.string.myorders_my_orders
        val isUserStoreOrders = intent.getBooleanExtra(AppConstant.BundleProperty.IS_MY_STORE_ORDER,false)
        if(isUserStoreOrders){
            titleId = R.string.myorders_my_store_orders
        }
        setToolbar(toolbar,titleId, R.drawable.ic_back)

         fragmentAll = MyOrderFragment.newInstance(isUserStoreOrders, DateTimeHelper.TimeRangeType.ALL)
         fragmentWeek = MyOrderFragment.newInstance(isUserStoreOrders, DateTimeHelper.TimeRangeType.LAST_WEEK)
         fragmentMonth = MyOrderFragment.newInstance(isUserStoreOrders, DateTimeHelper.TimeRangeType.LAST_MONTH)
         fragmentYear = MyOrderFragment.newInstance(isUserStoreOrders, DateTimeHelper.TimeRangeType.LAST_YEAR)

        val myOrdersViewAdapter = TabViewAdapter(
            supportFragmentManager,
            listOf(
                getString(R.string.myorders_all),
                getString(R.string.myorders_week),
                getString(R.string.myorders_month),
                getString(R.string.myorders_year)
            ),
            listOf(fragmentAll, fragmentWeek, fragmentMonth, fragmentYear)
        );
        ordersViewPager.adapter = myOrdersViewAdapter
        tabLayout.setupWithViewPager(ordersViewPager)
        setStatusSpinner()
        shouldApplyFilter = true
    }

    private fun setStatusSpinner(){
        val list = getFilterByStatus()
        val namedList = mutableListOf<String>()
        list.forEach { namedList.add(getString(it)) }
        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, namedList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerFilter.setSelection(0)
        spinnerFilter.onItemSelectedListener = object :AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
               if(shouldApplyFilter){
                   val statusValue = getFilterStatusValue(list[position])
                   fragmentAll.getOrders(statusValue)
                   fragmentWeek.getOrders(statusValue)
                   fragmentMonth.getOrders(statusValue)
                   fragmentYear.getOrders(statusValue)
               }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }
        spinnerFilter.adapter = adapter
    }

    private fun getFilterByStatus() = listOf(
        R.string.myorders_all,
        R.string.myorders_filter_by_shipped,
        R.string.myorders_filter_by_order_delivered,
        R.string.myorders_filter_by_order_in_progress,
        R.string.myorders_filter_by_cancelled_order
    )

    fun getFilterStatusValue(pos:Int) = when(pos){
        R.string.orderdetail_shipped-> AppConstant.OrderStatus.ORDER_STATUS_SHIPPED
        R.string.orderdetail_order_delivered -> AppConstant.OrderStatus.ORDER_STATUS_SHIPPED
        R.string.orderdetail_order_in_progress-> AppConstant.OrderStatus.ORDER_STATUS_IN_PROCESS
        R.string.myorders_filter_by_cancelled_order -> AppConstant.OrderStatus.ORDER_STATUS_CANCELED_BY_CUSTOMER
        else->0
    }

}
