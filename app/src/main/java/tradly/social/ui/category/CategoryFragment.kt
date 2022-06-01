package tradly.social.ui.category


import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager

import tradly.social.R
import tradly.social.adapter.HomeCategoryGridAdapter
import tradly.social.domain.entities.AppError
import tradly.social.domain.entities.Category
import tradly.social.domain.entities.Constant
import tradly.social.common.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_category.view.*
import tradly.social.common.*
import tradly.social.common.base.AppConfigHelper
import tradly.social.common.base.AppConstant
import tradly.social.common.base.ErrorHandler
import tradly.social.common.util.parser.extension.toJson
import tradly.social.common.util.parser.extension.toObject


class CategoryFragment : BaseFragment(),CategoryPresenter.View {
    var categoryList= mutableListOf<Category>()
    var fragmentListener:FragmentListener?=null
    var categoryId:String?=null
    var categoryName:String?=null
    var category:Category? = null
    private var homeCategoryGridAdapter: HomeCategoryGridAdapter? = null
    var categoryPresenter:CategoryPresenter?=null
    var mView:View?=null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if(mView != null){
            return mView
        }
        mView = inflater.inflate(R.layout.fragment_category, container, false)
        categoryPresenter = CategoryPresenter(this)
        var viewType = HomeCategoryGridAdapter.GRID
        if (arguments!!.getBoolean(AppConstant.BundleProperty.SHOULD_CHECK_SUB_CATEGORY_CONFIG,false) && AppConfigHelper.getConfigKey(
                AppConfigHelper.Keys.SHOW_SUB_CATEGORY_AS_LIST,true)){
            viewType = HomeCategoryGridAdapter.LIST
        }
        homeCategoryGridAdapter = HomeCategoryGridAdapter(requireContext(),false, categoryList,viewType){category: Category ->
            val bundle = Bundle()
            bundle.putString("categoryId",category.id)
            bundle.putString("categoryName",category.name)
            bundle.putString("category",category.toJson<Category>())
            bundle.putBoolean(AppConstant.BundleProperty.SHOULD_CHECK_SUB_CATEGORY_CONFIG,true)
            if(category.subCategory.isNotEmpty()){
                fragmentListener?.callNextFragment(bundle, AppConstant.FragmentTag.CATEGORY_FRAGMENT)
            }
            else{
                fragmentListener?.callNextFragment(bundle, AppConstant.FragmentTag.PRODUCT_LIST_FRAGMENT)
            }
        }
        mView?.recycler_view?.layoutManager = if (viewType == HomeCategoryGridAdapter.GRID){
            GridLayoutManager(requireContext(), 4)
        }
        else{
            LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)
        }
        mView?.recycler_view?.adapter = homeCategoryGridAdapter

        categoryId = arguments?.getString("categoryId")
        categoryName = arguments?.getString("categoryName")
        category = arguments?.getString("category").toObject<Category>()
        category?.let { onLoadCategories(it.subCategory)
        }?:run{
            categoryPresenter?.getCategories(categoryId?:Constant.EMPTY)
        }
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? CategoryListActivity)?.supportActionBar?.title = categoryName
    }
    override fun onLoadCategories(list: List<Category>) {
        categoryList.clear()
        categoryList.addAll(list)
        homeCategoryGridAdapter?.notifyDataSetChanged()
    }
    override fun onFailure(appError: AppError) {
        ErrorHandler.handleError(requireContext(),appError)
    }

    override fun showProgressLoader() {
        mView?.progress_circular?.visibility = View.VISIBLE
    }

    override fun hideProgressLoader() {
        mView?.progress_circular?.visibility = View.GONE
    }

    override fun noNetwork() {

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if(requireContext() is FragmentListener){
            fragmentListener = requireContext() as? FragmentListener
        }
    }

    override fun onDetach() {
        super.onDetach()
        fragmentListener = null
    }

}
