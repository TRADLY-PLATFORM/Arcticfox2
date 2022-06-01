package tradly.social.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import tradly.social.R
import tradly.social.adapter.HomeCategoryGridAdapter
import tradly.social.domain.entities.AppError
import tradly.social.domain.entities.Category
import tradly.social.domain.entities.Constant
import tradly.social.ui.category.CategoryPresenter
import kotlinx.android.synthetic.main.layout_category_search.view.*
import tradly.social.common.base.ThemeUtil


class CategoryFullScreenDialog : DialogFragment(), CategoryPresenter.View {

    private var categoryList = mutableListOf<Category>()
    private var categoryGridAdapter: HomeCategoryGridAdapter? = null
    private var categoryPresenter:CategoryPresenter?=null
    var mView:View?=null
    var dialogListener:DialogListener?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, ThemeUtil.getSelectedTheme())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mView = inflater.inflate(R.layout.layout_category_search, container, false)
        mView?.toolbar?.setNavigationOnClickListener {
            dialog?.dismiss()
        }
        categoryPresenter = CategoryPresenter(this)
        categoryGridAdapter = HomeCategoryGridAdapter(requireContext(),true, categoryList) {
            dialogListener?.onClick(it.id)
            dialog?.dismiss()

        }
        mView?.recycler_view?.layoutManager = GridLayoutManager(requireContext(), 4)
        mView?.recycler_view?.adapter = categoryGridAdapter
        mView?.toolbar?.title = getString(R.string.category_header_title)
        categoryPresenter?.getCategories()
        return mView
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            dialog.window?.setLayout(width, height)
        }
    }


    override fun onFailure(appError: AppError) {

    }

    override fun onLoadCategories(list: List<Category>) {
        categoryList.clear()
        categoryList.addAll(list)
        arguments?.let {
            categoryList.find { it.id == arguments?.getString("id",Constant.EMPTY) }?.isSelected  = true
        }
        categoryGridAdapter?.notifyDataSetChanged()
    }

    override fun showProgressLoader() {
        mView?.progress_circular?.visibility = View.VISIBLE
    }

    override fun hideProgressLoader() {
        mView?.progress_circular?.visibility = View.GONE
    }

    override fun noNetwork() {

    }

    fun setListener(listener: DialogListener){
        this.dialogListener = listener
    }

    override fun onDestroy() {
        super.onDestroy()
        categoryPresenter?.onDestroy()
    }
}