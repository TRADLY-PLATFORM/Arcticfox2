package tradly.social.event.explore.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import tradly.social.common.base.DateTimeHelper
import tradly.social.common.base.GenericAdapter
import tradly.social.domain.entities.Event
import tradly.social.event.explore.BR
import tradly.social.event.explore.R

class EventListAdapter : GenericAdapter<Event>(){

    var onClickItemListener: OnClickItemListener<Event>?=null
    private var viewType:Int = ViewType.VERTICAL_LIST

    object ViewType{
        const val VERTICAL_LIST = 1
        const val HORZ_LIST = 2
    }

    override fun getViewHolder(
        inflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): GenericViewHolder<Event> {
        val binding = if (viewType == ViewType.HORZ_LIST){
            DataBindingUtil.inflate<ViewDataBinding>(inflater, R.layout.list_item_horz_event, parent, false)
        }
        else{
            DataBindingUtil.inflate<ViewDataBinding>(inflater, R.layout.list_item_event, parent, false)
        }
        return GenericViewHolder(binding)
    }

    override fun onBindData(binding: ViewDataBinding, value: Event, position: Int ,itemViewType:Int) {
        binding.apply {
            setVariable(BR.listItem,value)
            setVariable(BR.dateFormat,DateTimeHelper.FORMAT_TIME_AM_PM)
            setVariable(BR.onClickItemListListener,object : OnClickItemListListener<Event>{
                override fun onClick(value: Event, view: View) {
                    onClickItemListener?.onClick(value, view, position)
                }
            })
        }
    }

    fun setViewType(viewType: Int){
        this.viewType = viewType
    }

    override fun getItemViewType(position: Int): Int {
        return viewType
    }
}