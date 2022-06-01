package tradly.social.event.eventbooking.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import tradly.social.common.base.DateTimeHelper
import tradly.social.common.base.GenericAdapter
import tradly.social.event.eventbooking.R
import tradly.social.domain.entities.EventBooking
import tradly.social.event.eventbooking.BR

class BookingListAdapter:GenericAdapter<EventBooking>() {

    lateinit var onClickItemListener: OnClickItemListener<EventBooking>
    override fun getViewHolder(
        inflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): GenericViewHolder<EventBooking> {
        val binding =  DataBindingUtil.inflate<ViewDataBinding>(inflater, R.layout.list_item_booking, parent, false)
        return GenericViewHolder(binding)
    }

    override fun onBindData(
        binding: ViewDataBinding,
        value: EventBooking,
        position: Int,
        itemViewType: Int
    ) {
        binding.apply {
            setVariable(BR.eventBooking,value)
            setVariable(BR.dateFormat,DateTimeHelper.FORMAT_DATE_EEE_D_MMM_YYYY)
            setVariable(BR.timeFormat,DateTimeHelper.FORMAT_TIME_AM_PM)
            setVariable(BR.onClickItemListListener,object :OnClickItemListListener<EventBooking>{
                override fun onClick(value: EventBooking, view: View) {
                    onClickItemListener.onClick(value,view,position)
                }
            })
        }
    }

}