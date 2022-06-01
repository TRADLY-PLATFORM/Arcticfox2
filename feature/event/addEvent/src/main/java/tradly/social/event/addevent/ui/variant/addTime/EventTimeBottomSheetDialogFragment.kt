package tradly.social.event.addevent.ui.variant.addTime

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import tradly.social.common.base.CustomOnClickListener
import tradly.social.common.base.ThemeUtil
import tradly.social.common.base.getViewModel
import tradly.social.common.base.getPrimitiveArgumentData
import tradly.social.common.resources.CommonResourceAttr
import tradly.social.common.resources.CommonResourceColor
import tradly.social.event.addevent.R
import tradly.social.event.addevent.databinding.EventTimeSheetBinding
import tradly.social.event.addevent.ui.variant.addTime.viewmodel.SharedEventTimeViewModel
import java.util.*

class EventTimeBottomSheetDialogFragment:BottomSheetDialogFragment(),CustomOnClickListener.OnCustomClickListener {

    private lateinit var eventTimeSheetBinding: EventTimeSheetBinding
    private lateinit var sharedEventTimeViewModel: SharedEventTimeViewModel
    private val fromCalender = Calendar.getInstance()
    private val toCalender = Calendar.getInstance().apply { add(Calendar.HOUR,1) }
    private var fromAMPM = fromCalender[Calendar.AM_PM]
    private var toAMPM = toCalender[Calendar.AM_PM]
    companion object{
        const val TAG = "EventTimeBottomSheetDialogFragment"
        private const val ARG_FROM_MILLIS = "fromMillis"
        private const val ARG_TO_MILLIS = "toMillis"

        fun newInstance(fromMillis:Long,toMillis:Long) = EventTimeBottomSheetDialogFragment().apply {
            arguments = Bundle().apply {
                putLong(ARG_FROM_MILLIS,fromMillis)
                putLong(ARG_TO_MILLIS,toMillis)
            }
        }

    }

    private val pickerFormatter = NumberPicker.Formatter { String.format("%02d", it.toString()) }
    private val colorPrimary by lazy { ThemeUtil.getResourceValue(requireContext(),CommonResourceAttr.colorPrimary) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedEventTimeViewModel = activity!!.getViewModel { SharedEventTimeViewModel() }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        eventTimeSheetBinding = DataBindingUtil.inflate(inflater, R.layout.bottomsheet_event_time_chooser,container,false)
        eventTimeSheetBinding.lifecycleOwner = viewLifecycleOwner
        eventTimeSheetBinding.onClickListener = CustomOnClickListener(this)
        return eventTimeSheetBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialisePicker()
        setDataFromIntent()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setOnShowListener {
            (view?.parent as ViewGroup).background = ColorDrawable(Color.TRANSPARENT)
        }
        return dialog
    }

    private fun initialisePicker(){
        eventTimeSheetBinding.apply {
            val hoursArray = (0..12).map { it.toString() }.toTypedArray()
            val minsArray = (0..60).map { it.toString() }.toTypedArray()
            pickerFromHour.apply {
                minValue = 0
                maxValue = 12
                displayedValues = hoursArray
            }
            pickerFromMinute.apply {
                minValue = 0
                maxValue = 60
                displayedValues = minsArray
            }
            pickerToHour.apply {
                minValue = 0
                maxValue = 12
                displayedValues = hoursArray
            }
            pickerToMinute.apply {
                minValue = 0
                maxValue = 60
                displayedValues = minsArray
            }
            pickerFromHour.setFormatter(pickerFormatter)
            pickerFromMinute.setFormatter(pickerFormatter)
            pickerToHour.setFormatter(pickerFormatter)
            pickerToMinute.setFormatter(pickerFormatter)
        }
    }

    private fun setDataFromIntent(){
        val fromMillis = getPrimitiveArgumentData<Long>(ARG_FROM_MILLIS)
        val toMillis = getPrimitiveArgumentData<Long>(ARG_TO_MILLIS)
        val fromCalender = fromCalender.apply { if (fromMillis!=0L) timeInMillis = fromMillis  }
        val toCalender = toCalender.apply { if (toMillis!=0L) timeInMillis = toMillis  }
        eventTimeSheetBinding.pickerFromHour.value = fromCalender[Calendar.HOUR]
        eventTimeSheetBinding.pickerFromMinute.value = fromCalender[Calendar.MINUTE]
        eventTimeSheetBinding.pickerToHour.value = toCalender[Calendar.HOUR]
        eventTimeSheetBinding.pickerToMinute.value = toCalender[Calendar.MINUTE]
        if (fromCalender[Calendar.AM_PM] == Calendar.AM){
            eventTimeSheetBinding.tvFromAM.setTextColor(ContextCompat.getColor(requireContext(),colorPrimary))
            eventTimeSheetBinding.tvFromPM.setTextColor(ContextCompat.getColor(requireContext(),CommonResourceColor.colorDarkGrey))
        }
        else{
            eventTimeSheetBinding.tvFromAM.setTextColor(ContextCompat.getColor(requireContext(),CommonResourceColor.colorDarkGrey))
            eventTimeSheetBinding.tvFromPM.setTextColor(ContextCompat.getColor(requireContext(),colorPrimary))
        }

        if (toCalender[Calendar.AM_PM] == Calendar.AM){
            eventTimeSheetBinding.tvToAM.setTextColor(ContextCompat.getColor(requireContext(),colorPrimary))
            eventTimeSheetBinding.tvToPM.setTextColor(ContextCompat.getColor(requireContext(),CommonResourceColor.colorDarkGrey))
        }
        else{
            eventTimeSheetBinding.tvToAM.setTextColor(ContextCompat.getColor(requireContext(),CommonResourceColor.colorDarkGrey))
            eventTimeSheetBinding.tvToPM.setTextColor(ContextCompat.getColor(requireContext(),colorPrimary))
        }
    }

    private fun onClickSet(){
        fromCalender[Calendar.HOUR] = eventTimeSheetBinding.pickerFromHour.value
        fromCalender[Calendar.MINUTE] = eventTimeSheetBinding.pickerFromMinute.value
        toCalender[Calendar.HOUR] = eventTimeSheetBinding.pickerToHour.value
        toCalender[Calendar.MINUTE] = eventTimeSheetBinding.pickerToMinute.value
        fromCalender[Calendar.AM_PM] = fromAMPM
        toCalender[Calendar.AM_PM] = toAMPM
        sharedEventTimeViewModel.onTimeSelected(fromCalender.timeInMillis,toCalender.timeInMillis)
        dismiss()
    }

    override fun onCustomClick(view: View) {
        when(view.id){
            R.id.ivFromHourIconUp-> eventTimeSheetBinding.pickerFromHour.apply { value += 1 }
            R.id.ivFromHourIconDown-> eventTimeSheetBinding.pickerFromHour.apply { value -= 1 }
            R.id.ivFromMinuteIconUp -> eventTimeSheetBinding.pickerFromMinute.apply { value+=1 }
            R.id.ivFromMinuteIconDown-> eventTimeSheetBinding.pickerFromMinute.apply { value-=1 }
            R.id.ivToHourIconUp-> eventTimeSheetBinding.pickerToHour.apply { value += 1 }
            R.id.ivToHourIconDown-> eventTimeSheetBinding.pickerToHour.apply { value -= 1 }
            R.id.ivToMinuteIconUp -> eventTimeSheetBinding.pickerToMinute.apply { value+=1 }
            R.id.ivToMinuteIconDown-> eventTimeSheetBinding.pickerToMinute.apply { value-=1 }
            R.id.tvFromAM->{
                eventTimeSheetBinding.tvFromAM.setTextColor(ContextCompat.getColor(requireContext(),colorPrimary))
                eventTimeSheetBinding.tvFromPM.setTextColor(ContextCompat.getColor(requireContext(),CommonResourceColor.colorDarkGrey))
                fromAMPM = Calendar.AM
            }
            R.id.tvFromPM->{
                eventTimeSheetBinding.tvFromAM.setTextColor(ContextCompat.getColor(requireContext(),CommonResourceColor.colorDarkGrey))
                eventTimeSheetBinding.tvFromPM.setTextColor(ContextCompat.getColor(requireContext(),colorPrimary))
                fromAMPM = Calendar.PM
            }
            R.id.tvToAM->{
                eventTimeSheetBinding.tvToAM.setTextColor(ContextCompat.getColor(requireContext(),colorPrimary))
                eventTimeSheetBinding.tvToPM.setTextColor(ContextCompat.getColor(requireContext(),CommonResourceColor.colorDarkGrey))
                toAMPM = Calendar.AM
            }
            R.id.tvToPM->{
                eventTimeSheetBinding.tvToAM.setTextColor(ContextCompat.getColor(requireContext(),CommonResourceColor.colorDarkGrey))
                eventTimeSheetBinding.tvToPM.setTextColor(ContextCompat.getColor(requireContext(),colorPrimary))
                toAMPM = Calendar.PM
            }
            R.id.btnSet -> onClickSet()
        }
    }
}