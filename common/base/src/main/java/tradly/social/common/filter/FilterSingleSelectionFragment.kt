package tradly.social.common.filter

import android.app.Dialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.google.android.material.shape.ShapeAppearanceModel
import tradly.social.common.adapter.FilterSelectionAdapter
import tradly.social.common.base.*
import tradly.social.common.base.databinding.FilterSingleSelectionBinding
import tradly.social.common.base.getPrimitiveArgumentData
import tradly.social.common.network.RequestID
import tradly.social.common.resources.CommonResourceAttr
import tradly.social.common.resources.CommonResourceColor
import tradly.social.common.resources.CommonResourceString
import tradly.social.common.base.setVisible
import tradly.social.domain.entities.*
import tradly.social.event.explore.common.CommonBaseResourceId


class FilterSingleSelectionFragment : BaseBottomSheetDialogFragment(),GenericAdapter.OnClickItemListener<FilterValue>,CustomOnClickListener.OnCustomClickListener {

    private lateinit var sharedFilterViewModel: SharedFilterViewModel
    private lateinit var filterViewModel: FilterViewModel
    private lateinit var filterSingleSelectionBinding: FilterSingleSelectionBinding
    private lateinit var filterSelectionAdapter: FilterSelectionAdapter
    private lateinit var filter: Filter
    var viewType: Int = 0

    companion object{
        const val TAG = "FilterSingleSelectionFragment"
        private const val ARG_FILTER_TYPE = "filterType"

        fun newInstance(filterType:Int) =
            FilterSingleSelectionFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_FILTER_TYPE,filterType)
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedFilterViewModel = requireActivity().getViewModel { SharedFilterViewModel() }
        filterViewModel = getViewModel { FilterViewModel() }
        filterViewModel.filterType = getPrimitiveArgumentData(ARG_FILTER_TYPE)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        filterSingleSelectionBinding = DataBindingUtil.inflate(inflater,R.layout.fragment_single_selection_filter,container,false)
        filterSingleSelectionBinding.lifecycleOwner = viewLifecycleOwner
        filterSingleSelectionBinding.onClickListener = CustomOnClickListener(this)
        return filterSingleSelectionBinding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setOnShowListener {
            (view?.parent as ViewGroup).background = ColorDrawable(Color.TRANSPARENT)
        }
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeLiveData()
    }

    private fun observeLiveData(){
        sharedFilterViewModel.selectedFilter.observe(viewLifecycleOwner,{ selectedEventFilter->
            this.filter = selectedEventFilter
            initView()
        })
        viewLifecycleOwner.observeEvent(filterViewModel.uiState,this::handleUIState)
        filterViewModel.onFilterEventListLiveData.observe(viewLifecycleOwner,this::onEventList)
        filterViewModel.onFilterProductListLiveData.observe(viewLifecycleOwner,this::onProductList)
        filterViewModel.onFilterStoreListLiveData.observe(viewLifecycleOwner,this::onStoreList)
    }

    private fun initView(){
        filterSingleSelectionBinding.title = this.filter.filterName
        when(this.filter.viewType){
            FilterUtil.ViewType.SINGLE_CHIP_SELECT -> initChipView()
            else-> initAdapter()
        }
    }

    private fun initChipView(){
        val selectedValue:String? = sharedFilterViewModel.getFilterQueryMap()[this.filter.queryKey] as? String
        val strokeColor = ColorStateList.valueOf(ContextCompat.getColor(requireContext(),CommonResourceColor.colorDarkGrey))
        val unSelectedBackgroundColor = ColorStateList.valueOf(ContextCompat.getColor(requireContext(),CommonResourceColor.white))
        val selectedBackgroundColor = ColorStateList.valueOf(ThemeUtil.getResourceDrawable(requireContext(), CommonResourceAttr.colorPrimary))
        filterSingleSelectionBinding.chipGroup.removeAllViews()
        this.filter.filterValue.forEach { filterValue ->
            val chip = Chip(requireContext())
            chip.text = filterValue.filterName
            chip.chipStartPadding = 4f
            chip.chipEndPadding = 4f
            chip.tag = filterValue
            chip.shapeAppearanceModel = ShapeAppearanceModel().withCornerSize(16f).toBuilder().build()
            chip.setOnClickListener{onChipSelected(chip.tag as FilterValue)}
            if (selectedValue != filterValue.filterId){
                chip.chipStrokeWidth = 2f
                chip.chipStrokeColor = strokeColor
                chip.backgroundTintList =  unSelectedBackgroundColor
                chip.setTextColor(ContextCompat.getColor(requireContext(),CommonResourceColor.colorDarkGrey))
            }
            else{
                chip.backgroundTintList = selectedBackgroundColor
            }
            filterSingleSelectionBinding.chipGroup.addView(chip)
        }
    }

    private fun initAdapter(){
        sharedFilterViewModel.getFilterQueryMap()[this.filter.queryKey]?.let {
            val selectedValue = it as String
            this.filter.filterValue.find { i->i.filterId == selectedValue}?.active = true
        }
        filterSelectionAdapter = FilterSelectionAdapter()
        filterSelectionAdapter.items = this.filter.filterValue
        filterSelectionAdapter.setViewType(filter.viewType)
        filterSelectionAdapter.onClickItemListener = this
        filterSingleSelectionBinding.apply {
            rvFilterList.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)
            rvFilterList.adapter = filterSelectionAdapter
            rvFilterList.setVisible()
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

    private fun onChipSelected(filterValue: FilterValue){
        sharedFilterViewModel.setQuery(this.filter.queryKey,filterValue.filterId)
    }

    private fun onDoneClick(){
        if (this.filter.subViewType == FilterUtil.SubViewType.SORT_BY){
            sharedFilterViewModel.setFinalQuery(this.filter.queryKey,this.filter.filterValue.find { it.active }?.filterId)
            sharedFilterViewModel.setOnSortClick()
            dismiss()
        }
        else{
            sharedFilterViewModel.setQuery(this.filter.queryKey,this.filter.filterValue.find { it.active }?.filterId)
            sharedFilterViewModel.setQueryNameMap(this.filter.queryKey,this.filter.filterValue.find { it.active }?.filterName)
            filterViewModel.getFilterResult(sharedFilterViewModel.getFilterQueryMap())
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

    override fun onClick(value: FilterValue, view: View, itemPosition: Int) {
        if (!value.active){
            filterSelectionAdapter.items.indexOfFirst { it.active }.takeIf { it!=-1 }?.let { index->
                filterSelectionAdapter.items[index].active = false
                filterSelectionAdapter.notifyItemChanged(index)
            }
            value.active = true
            filterSelectionAdapter.notifyItemChanged(itemPosition)

        }
        else{
            value.active = false
            filterSelectionAdapter.notifyItemChanged(itemPosition)
        }
    }

    override fun onCustomClick(view: View) {
        when(view.id){
            filterSingleSelectionBinding.btnDone.id-> onDoneClick()
            CommonBaseResourceId.ivBack -> dismiss()
        }
    }

}