package tradly.social.common.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import tradly.social.common.base.GenericAdapter
import tradly.social.common.base.R
import tradly.social.common.base.BR
import tradly.social.domain.entities.Category

class FilterMultiSelectionAdapter:GenericAdapter<Category>() {

    var onClickItemListener: OnClickItemListener<Category>?=null

    override fun getViewHolder(
        inflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): GenericViewHolder<Category> {
        val binding = DataBindingUtil.inflate<ViewDataBinding>(inflater, R.layout.list_item_filter_multi_select, parent, false)
        return GenericViewHolder(binding)
    }

    override fun onBindData(binding: ViewDataBinding, value: Category, position: Int ,itemViewType:Int) {
        binding.apply {
            setVariable(BR.listItem,value)
            setVariable(BR.adapterPosition,position)
            setVariable(BR.onClickItemListener,onClickItemListener)
            setVariable(BR.showDivider, (position != items.size-1))
        }
    }

}