package tradly.social.common.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import tradly.social.common.base.GenericAdapter
import tradly.social.common.base.R
import tradly.social.common.base.BR
import tradly.social.domain.entities.FilterValue
import tradly.social.common.filter.FilterUtil
import java.lang.IllegalArgumentException

class FilterSelectionAdapter:GenericAdapter<FilterValue>() {

    var onClickItemListener: OnClickItemListener<FilterValue>?=null
    private var viewType:Int = -1

    fun setViewType(viewType: Int){
        this.viewType = viewType
    }

    override fun getViewHolder(
        inflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): GenericViewHolder<FilterValue> {
        val binding = if (viewType == FilterUtil.ViewType.SINGLE_SELECT){
            DataBindingUtil.inflate<ViewDataBinding>(inflater, R.layout.list_item_filter_select, parent, false)
        }
        else{
            DataBindingUtil.inflate<ViewDataBinding>(inflater, R.layout.list_item_filter_rating, parent, false)
        }
        return GenericViewHolder(binding)
    }

    override fun onBindData(binding: ViewDataBinding, value: FilterValue, position: Int, itemViewType:Int) {
        if(itemViewType == FilterUtil.ViewType.SINGLE_SELECT){
            binding.apply {
                setVariable(BR.listItem,value)
                setVariable(BR.adapterPosition,position)
                setVariable(BR.onClickItemListener,onClickItemListener)
                setVariable(BR.showDivider, (position != items.size-1))
            }
        }
        else{
            binding.apply {
                setVariable(BR.listItem,value)
                setVariable(BR.starCount,value.filterName.toFloat())
                setVariable(BR.adapterPosition,position)
                setVariable(BR.onClickItemListener,onClickItemListener)
                setVariable(BR.showDivider, (position != items.size-1))
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (viewType != -1)  viewType else throw IllegalArgumentException("Unknown ViewType:: $viewType")
    }
}