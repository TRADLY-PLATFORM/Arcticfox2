package tradly.social.common.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import tradly.social.common.base.BR
import tradly.social.common.base.GenericAdapter
import tradly.social.common.base.R
import tradly.social.domain.entities.Filter
import tradly.social.common.filter.FilterUtil

class FilterListAdapter:GenericAdapter<Filter>() {

    var onClickItemListener: OnClickItemListener<Filter>?=null


    override fun getViewHolder(
        inflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): GenericViewHolder<Filter> {
        val binding = DataBindingUtil.inflate<ViewDataBinding>(inflater, R.layout.list_item_event_filter, parent, false)
        return GenericViewHolder(binding)
    }

    override fun onBindData(binding: ViewDataBinding, value: Filter, position: Int, itemViewType:Int) {
        binding.apply {
            setVariable(BR.listItem,value)
            setVariable(BR.adapterPosition,position)
            setVariable(BR.onClickItemListener,onClickItemListener)
            setVariable(BR.showDivider, (position != items.size-1))
            if (value.viewType == FilterUtil.ViewType.RATINGS){
                setVariable(BR.showRatingBar,value.selectedValue.isNotEmpty())
                setVariable(BR.showFilterValue,false)
            }
            else{
                setVariable(BR.showFilterValue,true)
                setVariable(BR.showRatingBar,false)
            }
        }
    }

}