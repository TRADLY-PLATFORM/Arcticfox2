package tradly.social.ui.base

import android.os.Bundle
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.toolbar.*
import tradly.social.R
import tradly.social.common.base.AppConstant
import tradly.social.common.base.BaseActivity
import tradly.social.common.base.getStringData
import tradly.social.ui.product.addProduct.productList.ListingFragment
import tradly.social.ui.store.followers.AccountFollowersFragment

class BaseHostActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base_host)
        setToolbar(toolbar,backNavIcon = R.drawable.ic_back,titleTxt = getStringData(AppConstant.BundleProperty.TITLE))
        initFragment()
    }

    private fun initFragment(){
        when(getStringData(AppConstant.BundleProperty.FRAGMENT_TAG)){
            AppConstant.FragmentTag.LISTING_FRAGMENT->{
                val fragment = ListingFragment().apply { arguments = intent.extras }
                navigateFragment(fragment,false)
            }
            AppConstant.FragmentTag.ACCOUNT_FOLLOWERS_FRAGMENT->{
                val accountId = getStringData(AppConstant.BundleProperty.ACCOUNT_ID)
                navigateFragment(AccountFollowersFragment.newInstance(accountId),false)
            }
        }
    }

    private fun navigateFragment(fragment:Fragment, addToBackStack:Boolean){
        with(supportFragmentManager.beginTransaction()){
            if (addToBackStack){
                replace(R.id.fragContainer,fragment)
                addToBackStack(null)
            }
            else{
                add(R.id.fragContainer,fragment)
            }
            commit()
        }
    }
}