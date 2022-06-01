package tradly.social.event.addevent.ui.variant.addTime

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import tradly.social.common.base.CustomOnClickListener
import tradly.social.common.base.DateTimeHelper
import tradly.social.common.base.getViewModel
import tradly.social.common.base.observeEvent
import tradly.social.common.navigation.Activities
import tradly.social.common.base.setGone
import tradly.social.common.base.setVisible
import tradly.social.event.addevent.BR
import tradly.social.event.addevent.R
import tradly.social.event.addevent.databinding.EventTimeCreateBinding
import tradly.social.event.addevent.ui.variant.addTime.viewmodel.SharedEventTimeViewModel
import tradly.social.event.explore.common.CommonBaseResourceId
import tradly.social.common.base.BaseFragment
import java.util.*

class EventTimeCreateFragment: BaseFragment(),CustomOnClickListener.OnCustomClickListener {

    private lateinit var eventTimeCreateBinding: EventTimeCreateBinding
    private lateinit var sharedEventTimeViewModel:SharedEventTimeViewModel
    private var calender =  Calendar.getInstance()

    companion object{
        const val TAG = "EventTimeCreateFragment"
        private const val ARG_FROM_MILLIS = "fromDay"
        private const val ARG_TO_MILLIS = "toDay"

        fun newInstance(fromDayMillis:Long,toDayMillis:Long) = EventTimeCreateFragment().apply {
            arguments = Bundle().apply {
                putLong(ARG_FROM_MILLIS,fromDayMillis)
                putLong(ARG_TO_MILLIS,toDayMillis)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedEventTimeViewModel = requireActivity().getViewModel { SharedEventTimeViewModel() }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        eventTimeCreateBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_event_time_create,container,false)
        eventTimeCreateBinding.lifecycleOwner = viewLifecycleOwner
        eventTimeCreateBinding.onClickListener = CustomOnClickListener(this)
        return eventTimeCreateBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setDataFromIntent()
        setListener()
        observeLiveData()
    }

    private fun setListener(){
        eventTimeCreateBinding.calenderView.setOnDateChangeListener { view, year, month, dayOfMonth ->
            calender.apply {
                set(Calendar.YEAR,year)
                set(Calendar.MONTH,month)
                set(Calendar.DAY_OF_MONTH,dayOfMonth)
            }
            showTimeSheet()
        }
    }

    private fun setDateWithSelectedTime(startMillis:Long,endMillis:Long){
        val fromCalendar = Calendar.getInstance().apply {
            timeInMillis = startMillis
            set(Calendar.YEAR,calender[Calendar.YEAR])
            set(Calendar.MONTH,calender[Calendar.MONTH])
            set(Calendar.DAY_OF_MONTH,calender[Calendar.DAY_OF_MONTH])
        }
        val toCalender = Calendar.getInstance().apply {
            timeInMillis = endMillis
            set(Calendar.YEAR,calender[Calendar.YEAR])
            set(Calendar.MONTH,calender[Calendar.MONTH])
            set(Calendar.DAY_OF_MONTH,calender[Calendar.DAY_OF_MONTH])
        }
        sharedEventTimeViewModel.setTime(fromCalendar.timeInMillis,toCalender.timeInMillis)
    }

    private fun setDataFromIntent(){
        sharedEventTimeViewModel.onEventTimeLiveData.value?.takeIf { it.first!=0L }?.let { (startMillis,endMillis)->
            eventTimeCreateBinding.clSelectedDateTime.root.setVisible()
            calender.timeInMillis = startMillis
            onTimeSelected(Pair(startMillis, endMillis))
        }
    }

    private fun observeLiveData(){
        viewLifecycleOwner.observeEvent(sharedEventTimeViewModel.onEventTimeSelectionLiveData,{onTimeSelected(it)})
    }

    private fun onTimeSelected(time:Pair<Long,Long>){
        val (startMillis,endMillis) = time
        setDateWithSelectedTime(startMillis, endMillis)
        eventTimeCreateBinding.apply {
            btnSave.isEnabled = true
            setVariable(BR.startMillis,sharedEventTimeViewModel.onEventTimeLiveData.value!!.first)
            setVariable(BR.endMillis,sharedEventTimeViewModel.onEventTimeLiveData.value!!.second)
            setVariable(BR.timeFormat,DateTimeHelper.FORMAT_TIME_AM_PM)
            setVariable(BR.dateFormat,DateTimeHelper.FORMAT_DATE_EEE_D_MMM_YYYY)
            executePendingBindings()
        }
    }

    private fun showTimeSheet(){
        val (startMillis,endMillis) = sharedEventTimeViewModel.onEventTimeLiveData.value!!
        val fragment = EventTimeBottomSheetDialogFragment.newInstance(startMillis,endMillis)
        fragment.show(childFragmentManager,EventTimeBottomSheetDialogFragment.TAG)
    }

    override fun onCustomClick(view: View) {
        when(view.id){
            R.id.btnSave -> onClickSave()
            CommonBaseResourceId.ivDelete->{
                eventTimeCreateBinding.btnSave.isEnabled = false
                eventTimeCreateBinding.clSelectedDateTime.root.setGone()
                sharedEventTimeViewModel.setTime(0,0)
                showTimeSheet()
            }
            CommonBaseResourceId.ivEdit->showTimeSheet()
        }
    }

    private fun onClickSave(){
        val (startMillis,endMillis) = sharedEventTimeViewModel.onEventTimeLiveData.value!!
        val bundle = Bundle().apply {
            putLong(Activities.EventTimeHostActivity.EXTRAS_FROM_MILLIS,startMillis)
            putLong(Activities.EventTimeHostActivity.EXTRAS_TO_MILLIS,endMillis)
        }
        sharedEventTimeViewModel.setFinish(bundle)
    }
}