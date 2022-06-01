package tradly.social.ui.payment.configurePayout

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import kotlinx.android.synthetic.main.toolbar.*
import tradly.social.R
import tradly.social.common.base.AppConfigHelper
import tradly.social.common.base.AppConstant
import tradly.social.common.FragmentListener
import tradly.social.common.base.AppController
import tradly.social.domain.entities.User
import tradly.social.common.base.BaseActivity

class PayoutConfigureActivity : BaseActivity() , FragmentListener{

    lateinit var user:User
    private var isInSuccessPage:Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_configure)
        setToolbar(toolbar,R.string.payouts_header_title,R.drawable.ic_clear_black_24dp)
        user = AppController.appController.getUser()!!
        val store = AppController.appController.getUserStore()
        toolbar?.setNavigationOnClickListener {
            if(isInSuccessPage){
                finishActivityWithResult()
            }
            else{
                onBackPressed()
            }
        }

        val stripeAccountType = AppConfigHelper.getConfigKey<String>(AppConfigHelper.Keys.STRIPE_CONNECT_ACCOUNT_TYPE)
        var showSuccessPage = false
        if(AppConstant.StripeConnectAccountType.STANDARD==stripeAccountType){
            showSuccessPage =  user.isStripeConnected
        }
        if(showSuccessPage){
            navigateFragment(PayoutSuccessFragment(),
                AppConstant.FragmentTag.CONFIGURE_PAYOUT_FRAGMENT_SUCCESS,false)
            isInSuccessPage = true
        }
        else{
            val arg = Bundle().apply {
                putString(AppConstant.BundleProperty.ACCOUNT_ID,store!!.id)
                putBoolean(
                    AppConstant.BundleProperty.IS_FROM_BROWSER_RESULT,intent.getBooleanExtra(
                        AppConstant.BundleProperty.IS_FROM_BROWSER_RESULT,false))
            }
            navigateFragment(ConfigurePayoutInitFragment().apply { arguments = arg },
                AppConstant.FragmentTag.CONFIGURE_PAYOUT_FRAGMENT,false)
        }
    }

    override fun callNextFragment(any: Any?, tag: String?) {
        when(tag){
            AppConstant.FragmentTag.CONFIGURE_PAYOUT_FRAGMENT->{
                val bundle = Bundle().apply { putString(AppConstant.BundleProperty.OAUTH_URL,any as String) }
                navigateFragment(ConfigurePayoutFragment().apply { arguments = bundle },tag)
            }
            AppConstant.FragmentTag.CONFIGURE_PAYOUT_FRAGMENT_SUCCESS-> {
                navigateFragment(PayoutSuccessFragment(),tag)
                isInSuccessPage = true
            }
        }
    }

    override fun popFragment(tag: String) = supportFragmentManager.popBackStack(tag, FragmentManager.POP_BACK_STACK_INCLUSIVE)

    private fun navigateFragment(fragment: Fragment, tag:String?, shouldAddToStack:Boolean = true){
        with(supportFragmentManager.beginTransaction()){
            this.replace(R.id.fragContainer,fragment,tag)
            if(shouldAddToStack){
                this.addToBackStack(tag)
            }
            commitTransaction(this)
        }
        isInSuccessPage = false
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        val fragment =  supportFragmentManager.findFragmentByTag(AppConstant.FragmentTag.CONFIGURE_PAYOUT_FRAGMENT)
        if(fragment != null && fragment.isVisible){
            (fragment as? ConfigurePayoutFragment)?.let {
                if(keyCode == KeyEvent.KEYCODE_BACK && it.canGoBack()){
                    it.goBack()
                    return true
                }
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        this.intent = intent
        if(this.intent.getBooleanExtra(AppConstant.BundleProperty.IS_FROM_BROWSER_RESULT,false)){
            val fragment = supportFragmentManager.findFragmentByTag(AppConstant.FragmentTag.CONFIGURE_PAYOUT_FRAGMENT)
            if(fragment!=null && fragment.isVisible){
                (fragment as ConfigurePayoutInitFragment).fetchStripeStatus()
            }
        }
    }

    fun finishActivityWithResult(){
        val intent = Intent()
        setResult(Activity.RESULT_OK,intent)
        finish()
    }

    override fun onBackPressed() {
        if(isInSuccessPage){
            finishActivityWithResult()
        }else{
            super.onBackPressed()
        }
    }
}
