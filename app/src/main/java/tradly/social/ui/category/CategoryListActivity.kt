package tradly.social.ui.category

import android.os.Bundle
import kotlinx.android.synthetic.main.toolbar.*
import tradly.social.R
import tradly.social.common.FragmentListener
import tradly.social.common.base.BaseActivity
import tradly.social.ui.product.addProduct.productList.ListingFragment
import tradly.social.common.base.AppConstant

class CategoryListActivity : BaseActivity(),FragmentListener {

    var hasParentFragment:Boolean=false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category_list)
        setToolbar(toolbar,backNavIcon = R.drawable.ic_back)
        intent?.let {
            when(intent.getStringExtra("isFor")){
                AppConstant.FragmentTag.PRODUCT_LIST_FRAGMENT->{ showProductListFragment(intent.extras)}
                AppConstant.FragmentTag.CATEGORY_FRAGMENT->{ showCategoryFragment(intent.extras)}
            }
        }
    }

    override fun callNextFragment(any: Any?, tag: String?) {
        hasParentFragment = true
        when(tag){
            AppConstant.FragmentTag.PRODUCT_LIST_FRAGMENT->{ showProductListFragment(any as? Bundle)}
            AppConstant.FragmentTag.CATEGORY_FRAGMENT->{ showCategoryFragment(any as? Bundle)}
        }
    }

    override fun popFragment(tag: String) {

    }
    private fun showProductListFragment(bundle: Bundle?){
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        val productListFragment = ListingFragment()
        bundle?.putInt(AppConstant.BundleProperty.IS_FROM, AppConstant.ListingType.PRODUCT_LIST)
        productListFragment.arguments = bundle
        fragmentTransaction.replace(R.id.fragContainer,productListFragment, AppConstant.FragmentTag.PRODUCT_LIST_FRAGMENT)
        if(hasParentFragment){
           fragmentTransaction.addToBackStack(null)
        }
        commitTransaction(fragmentTransaction)
    }
    private fun showCategoryFragment(bundle: Bundle?){
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        val categoryFragment = CategoryFragment()
        categoryFragment.arguments = bundle
        fragmentTransaction.replace(R.id.fragContainer,categoryFragment, AppConstant.FragmentTag.CATEGORY_FRAGMENT)
        if(hasParentFragment){
            fragmentTransaction.addToBackStack(null)
        }
        commitTransaction(fragmentTransaction)
    }
}
