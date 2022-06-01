package tradly.social.common.filter

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import tradly.social.common.base.*
import tradly.social.common.base.databinding.EventFilterRangeBinding
import tradly.social.common.base.getPrimitiveArgumentData
import tradly.social.common.base.isNotNullOrEmpty
import tradly.social.common.network.NetworkConstant
import tradly.social.common.network.RequestID
import tradly.social.common.resources.CommonResourceString
import tradly.social.common.base.setVisible
import tradly.social.domain.entities.Event
import tradly.social.domain.entities.Filter
import tradly.social.domain.entities.Product
import tradly.social.domain.entities.Store
import java.util.*

class FilterRangeFragment : BaseBottomSheetDialogFragment(),CustomOnClickListener.OnCustomClickListener {

    companion object{
        const val TAG = "FilterTimeRangeFragment"
        private const val ARG_FILTER_TYPE = "filterType"
        fun newInstance(filterType:Int) =
            FilterRangeFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_FILTER_TYPE,filterType)
                }
            }
    }

    private lateinit var eventFilterRangeBinding: EventFilterRangeBinding
    private lateinit var sharedFilterViewModel: SharedFilterViewModel
    private lateinit var filterViewModel: FilterViewModel
    private lateinit var filter:Filter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedFilterViewModel = requireActivity().getViewModel { SharedFilterViewModel() }
        filterViewModel = getViewModel { FilterViewModel() }
        filterViewModel.filterType = getPrimitiveArgumentData(ARG_FILTER_TYPE)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        eventFilterRangeBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_event_filter_range, container, false)
        eventFilterRangeBinding.lifecycleOwner = viewLifecycleOwner
        eventFilterRangeBinding.onClickListener = CustomOnClickListener(this)
        return eventFilterRangeBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeLiveData()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setOnShowListener {
            (view?.parent as ViewGroup).background = ColorDrawable(Color.TRANSPARENT)
        }
        return dialog
    }

    private fun observeLiveData(){
        sharedFilterViewModel.selectedFilter.observe(viewLifecycleOwner,{ selectedEventFilter->
            this.filter = selectedEventFilter
            initViews()
        })
        viewLifecycleOwner.observeEvent(filterViewModel.uiState,this::handleUIState)
        filterViewModel.onFilterEventListLiveData.observe(viewLifecycleOwner,this::onEventList)
        filterViewModel.onFilterProductListLiveData.observe(viewLifecycleOwner,this::onProductList)
        filterViewModel.onFilterStoreListLiveData.observe(viewLifecycleOwner,this::onStoreList)
    }

    private fun initViews(){
        eventFilterRangeBinding.title = this.filter.filterName
        initValue()
    }

    private fun initValue(){
        when(filter.viewType){
            FilterUtil.ViewType.RANGE_SLIDER-> setRangeSlider()
            FilterUtil.ViewType.SLIDER -> setSlider()
        }
    }

    private fun setRangeSlider(){
        eventFilterRangeBinding.rangeSlider.apply {
            setVisible()
            valueFrom = filter.minValue.toFloat()
            valueTo = filter.maxValue.toFloat()
            eventFilterRangeBinding.rangeSlider.setLabelFormatter { value->
                DateTimeHelper.getTimeFromMinutes(value.toInt())
            }
            addOnChangeListener { slider, value, fromUser ->
                eventFilterRangeBinding.tvStartRange.text = DateTimeHelper.getTimeFromMinutes(slider.values[0].toInt())
                eventFilterRangeBinding.tvEndRange.text = DateTimeHelper.getTimeFromMinutes(slider.values[1].toInt())
                eventFilterRangeBinding.tvSelectedTime.text = getString(CommonResourceString.placeholder_two_with_hyphen,values[0].toInt().toString(),values[1].toInt().toString())
            }
            val startTime = sharedFilterViewModel.getFilterQueryMap()[filter.queryKey] as? String
            val endTime = sharedFilterViewModel.getFilterQueryMap()[NetworkConstant.QueryParam.CREATED_TO] as? String
            values = if (startTime.isNotNullOrEmpty()){
                val startMillis = DateTimeHelper.getMillisFromDateString(startTime!!,DateTimeHelper.FORMAT_DATE_YYYY_MM_DD_HH_MM_SS_Z)
                val endMillis= DateTimeHelper.getMillisFromDateString(endTime!!,DateTimeHelper.FORMAT_DATE_YYYY_MM_DD_HH_MM_SS_Z)
                val startMinutes = Calendar.getInstance().apply { timeInMillis = startMillis }.let { it[Calendar.HOUR_OF_DAY]*60 + it[Calendar.MINUTE] }
                val endMinutes = Calendar.getInstance().apply { timeInMillis = endMillis }.let { it[Calendar.HOUR_OF_DAY]*60 + it[Calendar.MINUTE] }
                listOf(startMinutes.toFloat(),endMinutes.toFloat())
            } else{
                listOf(filter.minValue.toFloat(), filter.maxValue.toFloat())
            }
            eventFilterRangeBinding.tvSelectedTime.text = getString(CommonResourceString.placeholder_two_with_hyphen,values[0].toInt().toString(),values[1].toInt().toString())
        }
    }

    private fun setSlider(){
        eventFilterRangeBinding.slider.apply {
            setVisible()
            valueFrom = filter.minValue.toFloat()
            valueTo = filter.maxValue.toFloat()
            eventFilterRangeBinding.tvStartRange.text = getString(CommonResourceString.two_data,filter.minValue.toString(),filter.unit)
            if (filter.unit.isNotEmpty()){
                setLabelFormatter { value->
                    getString(CommonResourceString.two_data,value.toInt().toString(),filter.unit)
                }
            }
            addOnChangeListener { slider, value, fromUser ->
               eventFilterRangeBinding.tvEndRange.text = getString(CommonResourceString.two_data,slider.value.toInt().toString(),filter.unit)
            }
            val selectedValue = sharedFilterViewModel.getFilterQueryMap()[filter.queryKey] as? Int
            value = selectedValue?.toFloat() ?: filter.minValue.toFloat()
        }
    }


    private fun onEventList(list: List<Event>){
        sharedFilterViewModel.setFilterEventList(list)
        sharedFilterViewModel.setRefreshFilterList()
        dismiss()
    }

    private fun onStoreList(list:List<Store>){
        sharedFilterViewModel.setFilterStoreList(list)
        sharedFilterViewModel.setRefreshFilterList()
        dismiss()
    }

    private fun onProductList(list:List<Product>){
        sharedFilterViewModel.setFilterProductList(list)
        sharedFilterViewModel.setRefreshFilterList()
        dismiss()
    }

    override fun onCustomClick(view: View) {
        when(view.id){
            R.id.btnDone ->{
                when(this.filter.viewType){
                    FilterUtil.ViewType.RANGE_SLIDER->{
                        eventFilterRangeBinding.rangeSlider.values.let {
                            val currentSelectedDate = sharedFilterViewModel.getFilterQueryMap()[NetworkConstant.QueryParam.START_AT] as String
                            val (startHour,startMin) = DateTimeHelper.getMinuteHourPairFromMinutes(it[0].toInt())
                            val (endHour,endMin) = DateTimeHelper.getMinuteHourPairFromMinutes(it[1].toInt())
                            val time = DateTimeHelper.getMillisFromDateString(currentSelectedDate,DateTimeHelper.FORMAT_DATE_YYYY_MM_DD_HH_MM_SS_Z)
                            val startCalender = Calendar.getInstance().apply { timeInMillis = time }
                            startCalender[Calendar.HOUR_OF_DAY] = startHour
                            startCalender[Calendar.MINUTE] = startMin
                            val endCalender = Calendar.getInstance().apply { timeInMillis = time }
                            endCalender[Calendar.HOUR_OF_DAY] = endHour
                            endCalender[Calendar.MINUTE] = endMin
                            sharedFilterViewModel.setQuery(this.filter.queryKey,DateTimeHelper.getDateFromTimeMillis(startCalender.timeInMillis,DateTimeHelper.FORMAT_DATE_YYYY_MM_DD_HH_MM_SS_Z))
                            sharedFilterViewModel.setQuery(NetworkConstant.QueryParam.CREATED_TO,DateTimeHelper.getDateFromTimeMillis(endCalender.timeInMillis,DateTimeHelper.FORMAT_DATE_YYYY_MM_DD_HH_MM_SS_Z))
                            sharedFilterViewModel.setQueryNameMap(this.filter.queryKey,DateTimeHelper.getDateFromTimeMillis(startCalender.timeInMillis,DateTimeHelper.FORMAT_TIME_AM_PM))
                            sharedFilterViewModel.setQueryNameMap(NetworkConstant.QueryParam.CREATED_TO,DateTimeHelper.getDateFromTimeMillis(endCalender.timeInMillis,DateTimeHelper.FORMAT_TIME_AM_PM))
                        }
                    }
                    FilterUtil.ViewType.SLIDER->{
                        val value = eventFilterRangeBinding.slider.value.toInt()
                        sharedFilterViewModel.setQuery(this.filter.queryKey,value)
                        sharedFilterViewModel.setQueryNameMap(this.filter.queryKey,"$value${this.filter.unit}")
                        if (getPrimitiveArgumentData<Int>(ARG_FILTER_TYPE) != AppConstant.FilterType.EVENTS){
                            if ( PermissionHelper.checkPermission(requireContext(),PermissionHelper.Permission.LOCATION)){
                                LocationHelper.getInstance().getMyLocation {
                                    sharedFilterViewModel.getFilterQueryMap()[NetworkConstant.QueryParam.LATITUDE] = it.latitude
                                    sharedFilterViewModel.getFilterQueryMap()[NetworkConstant.QueryParam.LONGITUDE] = it.longitude
                                    filterViewModel.getFilterResult(sharedFilterViewModel.getFilterQueryMap())
                                }
                            }
                            else{
                                filterViewModel.getFilterResult(sharedFilterViewModel.getFilterQueryMap())
                            }
                        }
                        else{
                            filterViewModel.getFilterResult(sharedFilterViewModel.getFilterQueryMap())
                        }
                    }
                }
            }
            R.id.ivBack-> dismiss()
        }
    }

    private fun handleUIState(uiState: UIState<Unit>){
        when(uiState){
            is UIState.Loading->{
                when(uiState.apiId){
                    RequestID.GET_EVENTS,
                    RequestID.GET_PRODUCTS,
                    RequestID.GET_STORES->{
                        if (uiState.isLoading){
                            showLoader(CommonResourceString.please_wait)
                        }
                        else{
                            hideLoader()
                        }
                    }
                }
            }
            is UIState.Failure-> ErrorHandler.handleError(uiState.errorData)
        }
    }

}