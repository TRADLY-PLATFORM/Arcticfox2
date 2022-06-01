package tradly.social.common.filter

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import tradly.social.common.adapter.FilterListAdapter
import tradly.social.common.base.*
import tradly.social.common.base.databinding.EventFilterListBinding
import tradly.social.common.network.NetworkConstant
import tradly.social.common.resources.CommonResourceString
import tradly.social.domain.entities.Filter


class FilterListFragment : BaseFragment(),GenericAdapter.OnClickItemListener<Filter>,CustomOnClickListener.OnCustomClickListener{

    companion object{
        const val TAG = "EventFilterListFragment"
        private const val ARG_FILTER_TYPE = "filterType"

        fun newInstance(filterType:Int) =
            FilterListFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_FILTER_TYPE,filterType)
                }
            }
    }

    private lateinit var sharedFilterViewModel: SharedFilterViewModel
    lateinit var filterListAdapter: FilterListAdapter
    lateinit var eventFilterBinding: EventFilterListBinding
    var filterList:MutableList<Filter> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedFilterViewModel = requireActivity().getViewModel { SharedFilterViewModel() }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        eventFilterBinding = DataBindingUtil.inflate(inflater,R.layout.fragment_event_filter_list,container,false)
        eventFilterBinding.lifecycleOwner = viewLifecycleOwner
        eventFilterBinding.onClickListener = CustomOnClickListener(this)
        return eventFilterBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setToolbar()
        initFilterData()
        initAdapter()
        observeLiveData()
    }

    private fun initFilterData(){
        sharedFilterViewModel.apply {
            getFilterQueryMap().putAll(getFinalFilterQueryMap())
            getFilterQueryNameMap().putAll(getFinalFilterQueryNameMap())
        }
    }

    private fun setToolbar(){
        eventFilterBinding.clToolbar.toolbar.apply {
            title = getString(CommonResourceString.explore_event_filters)
            setNavigationOnClickListener {
                sharedFilterViewModel.setFragmentPopup()
            }
        }
        sharedFilterViewModel.setToolbarTitle(getString(CommonResourceString.explore_event_filters))
    }

    private fun observeLiveData(){
        sharedFilterViewModel.filterLiveData.observe(viewLifecycleOwner,this::onPopulateFilters)
        sharedFilterViewModel.onRefreshFilterList.observe(viewLifecycleOwner,{updateFilterListSelectedValue()})
    }


    private fun initAdapter(){
        eventFilterBinding.rvFilterList.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)
        filterListAdapter = FilterListAdapter()
        filterListAdapter.items = filterList
        filterListAdapter.onClickItemListener = this
        eventFilterBinding.rvFilterList.adapter = filterListAdapter
    }

    private fun updateFilterListSelectedValue(){
        val filterMap = sharedFilterViewModel.getFilterQueryNameMap()
        filterListAdapter.items.forEach {
            if (it.queryKey == NetworkConstant.QueryParam.CREATED_FROM){
                val startTime = filterMap[NetworkConstant.QueryParam.CREATED_FROM] as? String
                val endTime = filterMap[NetworkConstant.QueryParam.CREATED_TO] as? String
                if (startTime.isNotNullOrEmpty() && endTime.isNotNullOrEmpty()){
                    it.selectedValue = "$startTime - $endTime"
                }
            }
            else if (it.queryKey == NetworkConstant.QueryParam.MAX_DISTANCE){
                filterMap[it.queryKey]?.let { value ->
                    it.selectedValue = value
                }

            }
            else{
                val value = filterMap[it.queryKey].getOrDefault<String>()
                it.selectedValue = value
            }
            filterMap[it.queryKey]
        }
        filterListAdapter.notifyDataSetChanged()
    }

    private fun onPopulateFilters(list: List<Filter>){
        filterList.clear()
        filterList.addAll(list.filterNot { it.subViewType == FilterUtil.SubViewType.SORT_BY })
        when(getPrimitiveArgumentData<Int>(ARG_FILTER_TYPE)){
            AppConstant.FilterType.ACCOUNTS,
            AppConstant.FilterType.PRODUCTS->{
                filterList.removeAll { it.viewType == FilterUtil.ViewType.RANGE_SLIDER }
            }
        }
        filterListAdapter.notifyDataSetChanged()
        updateFilterListSelectedValue()
    }


    override fun onClick(value: Filter, view: View, itemPosition: Int) {
        sharedFilterViewModel.setSelectedEventFilter(value)
        when(value.viewType){
            FilterUtil.ViewType.RATINGS,
            FilterUtil.ViewType.SINGLE_SELECT->{
                val dialog = FilterSingleSelectionFragment.newInstance(getPrimitiveArgumentData(ARG_FILTER_TYPE))
                dialog.show(childFragmentManager, FilterSingleSelectionFragment.TAG)
            }
            FilterUtil.ViewType.MULTI_SELECT -> sharedFilterViewModel.setFragmentNavigation(
                FilterMultiSelectFragment.TAG
            )
            FilterUtil.ViewType.SLIDER,
            FilterUtil.ViewType.RANGE_SLIDER->{
               if (getPrimitiveArgumentData<Int>(ARG_FILTER_TYPE) == AppConstant.FilterType.EVENTS){
                   openRangeSheet()
               }
               else{
                   checkLocationPermission()
               }
            }
        }
    }

    private fun openRangeSheet(){
        val dialog = FilterRangeFragment.newInstance(getPrimitiveArgumentData(ARG_FILTER_TYPE))
        dialog.show(childFragmentManager, FilterRangeFragment.TAG)
    }

    private fun checkLocationPermission(){
       if ( PermissionHelper.checkPermission(requireContext(),PermissionHelper.Permission.LOCATION)){
          openRangeSheet()
       }
       else{
           PermissionHelper.requestPermission(this,PermissionHelper.Permission.LOCATION,PermissionHelper.RESULT_CODE_LOCATION)
       }
    }

    override fun onCustomClick(view: View) {
        when(view.id){
            eventFilterBinding.btnApply.id-> onClickApply()
            eventFilterBinding.btnClearFilter.id-> {
                sharedFilterViewModel.clearQueryParam()
                sharedFilterViewModel.getFilterQueryNameMap().clear()
                filterListAdapter.items.forEach { it.selectedValue = AppConstant.EMPTY }
                filterListAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun onClickApply(){
        sharedFilterViewModel.apply {
            getFinalFilterQueryMap().apply {
                clear()
                putAll(getFilterQueryMap())
            }
            getFinalFilterQueryNameMap().apply {
                clear()
                putAll(getFilterQueryNameMap())
            }
        }
        sharedFilterViewModel.setFinalList()
        sharedFilterViewModel.setFragmentPopup()
        sharedFilterViewModel.setFilterApply()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PermissionHelper.RESULT_CODE_LOCATION) {
            if (grantResults.count() > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                openRangeSheet()
            } else {
                requireContext().showToast(CommonResourceString.app_need_location_permission)
            }
        }
    }
}