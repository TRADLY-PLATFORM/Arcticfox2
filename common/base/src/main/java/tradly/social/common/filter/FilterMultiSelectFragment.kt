package tradly.social.common.filter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import tradly.social.common.adapter.FilterMultiSelectionAdapter
import tradly.social.common.base.*
import tradly.social.common.base.databinding.FilterMultiSelectBinding
import tradly.social.common.base.getPrimitiveArgumentData
import tradly.social.common.network.RequestID
import tradly.social.common.resources.CommonResourceString
import tradly.social.domain.entities.*

class FilterMultiSelectFragment:BaseFragment(),GenericAdapter.OnClickItemListener<Category>,CustomOnClickListener.OnCustomClickListener {

    private lateinit var filterMultiSelectBinding: FilterMultiSelectBinding
    private lateinit var filterViewModel: FilterViewModel
    private lateinit var sharedFilterViewModel: SharedFilterViewModel
    private lateinit var adapter: FilterMultiSelectionAdapter
    private lateinit var filter: Filter

    companion object{
        const val TAG = "FilterMultiSelectFragment"
        private const val ARG_FILTER_TYPE = "filterType"

        fun newInstance(filterType:Int) =
            FilterMultiSelectFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_FILTER_TYPE,filterType)
                }
            }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        filterViewModel = getViewModel { FilterViewModel() }
        sharedFilterViewModel = activity!!.getViewModel { SharedFilterViewModel() }
        filterViewModel.filterType = getPrimitiveArgumentData(ARG_FILTER_TYPE)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        filterMultiSelectBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_filter_multi_select,container,false)
        filterMultiSelectBinding.lifecycleOwner = viewLifecycleOwner
        filterMultiSelectBinding.onClickListener = CustomOnClickListener(this)
        return filterMultiSelectBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeLiveData()
    }

    private fun setToolbarView(){
        filterMultiSelectBinding.clToolbar.toolbar.apply {
            title = filter.filterName
            setNavigationOnClickListener {
                sharedFilterViewModel.setFragmentPopup()
            }
        }
        sharedFilterViewModel.setToolbarTitle(filter.filterName)
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
        setToolbarView()
        initAdapter()
        initSelectedValues()
    }

    private fun initAdapter(){
        adapter = FilterMultiSelectionAdapter()
        adapter.items = this.filter.categoryValues.toMutableList()
        val llManager = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)
        filterMultiSelectBinding.rvFilterList.apply {
            layoutManager = llManager
            adapter = this@FilterMultiSelectFragment.adapter
        }
        adapter.onClickItemListener = this
    }

    private fun initSelectedValues(){
        val selectedValue = (sharedFilterViewModel.getFilterQueryMap()[this.filter.queryKey] as? String)?.split(",")
        adapter.items.forEach { it.active = false }
        selectedValue?.forEach { value->
            adapter.items.find { it.id == value }?.active = true
        }
        adapter.notifyDataSetChanged()
    }

    private fun onDoneClick(){
        val selectedFilterValues = this.filter.categoryValues.filter { it.active }
        if (selectedFilterValues.isNotEmpty()){
            val value = selectedFilterValues.joinToString(",") { it.id }
            sharedFilterViewModel.setQuery(this.filter.queryKey,value)
            sharedFilterViewModel.setQueryNameMap(this.filter.queryKey, selectedFilterValues.joinToString(","){ it.name})
        }
        else{
            sharedFilterViewModel.getFilterQueryMap().remove(this.filter.queryKey)
            sharedFilterViewModel.getFilterQueryNameMap().remove(this.filter.queryKey)
        }
        filterViewModel.getFilterResult(sharedFilterViewModel.getFilterQueryMap())
    }

    private fun onEventList(list: List<Event>){
        sharedFilterViewModel.setFilterEventList(list)
        sharedFilterViewModel.setFragmentPopup()
        sharedFilterViewModel.setRefreshFilterList()
    }

    private fun onStoreList(list:List<Store>){
        sharedFilterViewModel.setFilterStoreList(list)
        sharedFilterViewModel.setFragmentPopup()
        sharedFilterViewModel.setRefreshFilterList()
    }

    private fun onProductList(list:List<Product>){
        sharedFilterViewModel.setFilterProductList(list)
        sharedFilterViewModel.setFragmentPopup()
        sharedFilterViewModel.setRefreshFilterList()
    }

    private fun handleUIState(uiState: UIState<Unit>){
        when(uiState){
            is UIState.Loading->{
                when(uiState.apiId){
                    RequestID.GET_STORES,
                    RequestID.GET_PRODUCTS,
                    RequestID.GET_EVENTS->{
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



    override fun onClick(value: Category, view: View, itemPosition: Int) {
        value.active = !value.active
        adapter.notifyItemChanged(itemPosition)
    }

    override fun onCustomClick(view: View) {
        when(view.id){
            filterMultiSelectBinding.btnDone.id-> onDoneClick()
        }
    }
}