package tradly.social.ui.transaction

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.activity_transaction_host.*
import tradly.social.R
import tradly.social.common.base.*
import tradly.social.domain.entities.Earning

class TransactionHostActivity : BaseActivity() {

    private var isInPayoutList = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_host)
        setToolbar(toolbar,backNavIcon =R.drawable.ic_back)
        callFragment(false)
        if(AppConfigHelper.getConfigKey<String>(AppConfigHelper.Keys.PAYMENT_METHOD) == AppConstant.PayoutMethod.STRIPE_CONNECT){
            btnSeePayouts.visibility = View.VISIBLE
            btnSeePayouts.safeClickListener {
                callFragment(true)
            }
        }
    }

    public fun setToolbar(title:Int){
        collapsing_toolbar?.title = getString(title)
    }

    private fun callFragment(isForPayouts:Boolean){
        isInPayoutList = isForPayouts
        if(isInPayoutList){
            showTopLayout(false)
        }
        val transactionFragment = TransactionFragment()
        val bundle = Bundle().apply {
            val store = AppController.appController.getUserStore()
            putString(AppConstant.BundleProperty.ACCOUNT_ID,store?.id)
            putBoolean(AppConstant.BundleProperty.IS_FOR_PAYOUT_LIST,isForPayouts)
        }
        transactionFragment.arguments = bundle
        navigateFragment(transactionFragment,
            AppConstant.FragmentTag.TRANSACTION_LIST_FRAGMENT,isForPayouts)
    }

    fun showTopLayout(shouldShow:Boolean){
        if (shouldShow){
            setTopLayout()
        }
        else{
            topLayout?.setGone()
        }
        secondaryTitle?.visibility = if(!shouldShow)View.VISIBLE else View.GONE
        appbar?.setExpanded(shouldShow,true)
    }

    fun showBalance(earning: Earning?){
        setTopLayout()
        txtBalance?.text = earning?.pendingBalance?.displayCurrency
    }

    fun setTopLayout(){
        if (!AppConfigHelper.getConfigKey<Boolean>(AppConfigHelper.Keys.SHOW_PENDING_BALANCE,true)){
            secondaryTitle?.apply {
                text = getString(R.string.sales_transactions)
                setVisible()
            }
            topLayout?.setGone()
        }
        else{
            topLayout?.setVisible()
        }
    }

    private fun navigateFragment(fragment: Fragment, tag:String?, shouldAddToStack:Boolean = true){
        with(supportFragmentManager.beginTransaction()){
            this.replace(R.id.fragContainer,fragment,tag)
            if(shouldAddToStack){
                this.addToBackStack(tag)
            }
            commitTransaction(this)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if(isInPayoutList){
            showTopLayout(true)
        }
    }
}