package tradly.social.event.eventbooking

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import tradly.social.common.base.DateTimeHelper
import tradly.social.common.base.getViewModel
import tradly.social.domain.entities.EventBooking
import tradly.social.event.eventbooking.databinding.EventBookingDetailsFragmentBinding
import tradly.social.common.base.BaseFragment


class EventBookingDetailFragment : BaseFragment() {

    private lateinit var eventBookingDetailFragmentBinding: EventBookingDetailsFragmentBinding
    private lateinit var eventBookingViewModel: EventBookingViewModel

    companion object{
        const val TAG = "EventBookingDetailFragment"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        eventBookingViewModel = getViewModel { EventBookingViewModel() }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        eventBookingDetailFragmentBinding = DataBindingUtil.inflate(inflater,R.layout.fragment_boooking_details,container, false)
        return eventBookingDetailFragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeLiveData()
    }

    private fun observeLiveData(){
        eventBookingViewModel.eventBookingDetail.observe(viewLifecycleOwner,this::onPopulateEventBooking)
    }

    private fun onPopulateEventBooking(eventBooking:EventBooking){
        eventBookingDetailFragmentBinding.apply {
            setVariable(BR.dateFormat,DateTimeHelper.FORMAT_DATE_EEE_D_MMM_YYYY)
            setVariable(BR.timeFormat,DateTimeHelper.FORMAT_TIME_AM_PM)
            setVariable(BR.event,eventBooking.event)
            executePendingBindings()
        }
    }

}