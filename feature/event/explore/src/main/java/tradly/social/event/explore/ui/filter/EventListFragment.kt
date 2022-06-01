package tradly.social.event.explore.ui.filter

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.michalsvec.singlerowcalendar.calendar.CalendarChangesObserver
import com.michalsvec.singlerowcalendar.calendar.CalendarViewManager
import com.michalsvec.singlerowcalendar.calendar.SingleRowCalendarAdapter
import com.michalsvec.singlerowcalendar.selection.CalendarSelectionManager
import kotlinx.android.synthetic.main.layout_event_list.view.*
import kotlinx.android.synthetic.main.layout_event_sheet.view.*
import kotlinx.android.synthetic.main.layout_explore_toolbar.view.*
import kotlinx.android.synthetic.main.list_item_calendar_row.view.*
import tradly.social.common.*
import tradly.social.common.base.*
import tradly.social.common.filter.FilterListFragment
import tradly.social.common.filter.FilterSingleSelectionFragment
import tradly.social.common.network.NetworkConstant
import tradly.social.common.network.RequestID
import tradly.social.common.resources.CommonResourceColor
import tradly.social.common.resources.CommonResourceString
import tradly.social.domain.entities.Category
import tradly.social.domain.entities.Event
import tradly.social.event.explore.R
import tradly.social.event.explore.adapter.EventListAdapter
import tradly.social.common.filter.FilterUtil
import tradly.social.event.explore.databinding.EventListBinding
import tradly.social.event.explore.ui.filter.viewmodel.EventExploreViewModel
import tradly.social.common.filter.SharedFilterViewModel
import tradly.social.common.base.BaseFragment
import tradly.social.ui.product.addProduct.productDetail.ProductDetailActivity
import java.util.*


class EventListFragment : BaseFragment(), GenericAdapter.OnClickItemListener<Event>, CustomOnClickListener.OnCustomClickListener {

    private lateinit var eventExploreViewModel: EventExploreViewModel
    private lateinit var sharedExploreViewModel:EventExploreViewModel
    private lateinit var sharedFilterViewModel: SharedFilterViewModel
    private lateinit var eventListBinding: EventListBinding
    private lateinit var eventListAdapter: EventListAdapter
    private lateinit var layoutManager: LinearLayoutManager
    private var isManualSelectionEnabled = false
    private var recentSearch:String = AppConstant.EMPTY
    val whiteBackground by lazy { ColorStateList.valueOf(ContextCompat.getColor(requireContext(),CommonResourceColor.white)) }
    val selectedBackground  by lazy{ ColorStateList.valueOf(ContextCompat.getColor(requireContext(),CommonResourceColor.colorPrimary)) }
    val strokeColor by lazy { ColorStateList.valueOf(ContextCompat.getColor(requireContext(),CommonResourceColor.colorDarkGrey))  }

    companion object{
        const val TAG = "EventListFragment"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        eventExploreViewModel = getViewModel { EventExploreViewModel() }
        sharedExploreViewModel = activity!!.getViewModel { EventExploreViewModel() }
        sharedFilterViewModel = activity!!.getViewModel { SharedFilterViewModel() }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        eventListBinding = DataBindingUtil.inflate(inflater, R.layout.layout_event_list,container,false)
        eventListBinding.lifecycleOwner = viewLifecycleOwner
        eventListBinding.onClickListener = CustomOnClickListener(this)
        return  eventListBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setToolbarView()
        initAdapter()
        initHorZCalender()
        observeLiveData()
        setListener()
        if (sharedExploreViewModel.eventListLiveData.value==null){
            getEvents(Calendar.getInstance(),false,1)
        }
        else{
            eventExploreViewModel.setEvents(sharedExploreViewModel.getEventsLiveData())
        }
    }

    private fun setListener(){
        eventListBinding.clToolbar.edSearch.setOnEditorActionListener { v, actionId, event ->
            when (actionId) {
                EditorInfo.IME_ACTION_SEARCH -> {
                    Utils.hideKeyBoard(requireActivity())
                    val newSearchKeyword = eventListBinding.clToolbar.edSearch.getString()
                    if (newSearchKeyword.isNotEmpty() && recentSearch != newSearchKeyword) {
                        recentSearch = newSearchKeyword
                        sharedFilterViewModel.setFinalQuery(NetworkConstant.QueryParam.SEARCH_KEY,newSearchKeyword)
                        resetList()
                        getEvents(false,1)
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun getEvents(calendar: Calendar,isLoadMore: Boolean,pagination:Int){
        val startDate = DateTimeHelper.getDate(calendar = calendar,includeTime = false,format =  DateTimeHelper.FORMAT_DATE_YYYY_MM_DD_HH_MM_SS_Z)
        val endDate = DateTimeHelper.getDate(calendar = calendar,includeTime = false, format = DateTimeHelper.FORMAT_DATE_YYYY_MM_DD_HH_MM_SS_Z,nextOffset = 1)
        sharedFilterViewModel.getFinalFilterQueryMap().apply {
            this[NetworkConstant.QueryParam.START_AT] = startDate//"2021-06-01T00:00:00Z"
            this[NetworkConstant.QueryParam.END_AT] =  endDate //"2021-07-01T00:00:00Z"
            this[NetworkConstant.QueryParam.PAGE] = pagination
        }
        getEvents(isLoadMore,pagination)
    }

    private fun getEvents(isLoadMore:Boolean,pagination: Int){
        if (PermissionHelper.checkPermission(requireContext(), PermissionHelper.Permission.LOCATION)){
            LocationHelper.getInstance().getMyLocation {
                sharedFilterViewModel.getFinalFilterQueryMap()[NetworkConstant.QueryParam.LATITUDE] = it.latitude
                sharedFilterViewModel.getFinalFilterQueryMap()[NetworkConstant.QueryParam.LONGITUDE] = it.longitude
                eventExploreViewModel.setLoading(true)
                eventExploreViewModel.getEvents(isLoadMore,pagination,sharedFilterViewModel.getFinalFilterQueryMap())
            }
        }
        else{
            eventExploreViewModel.setLoading(true)
            eventExploreViewModel.getEvents(isLoadMore,pagination,sharedFilterViewModel.getFinalFilterQueryMap())
        }
    }

    private fun setToolbarView(){
        eventListBinding.clToolbar.root.setVisible()
        eventListBinding.clToolbar.toolbarTitle.setGone()
        eventListBinding.clToolbar.tvSearchHint.setGone()
        eventListBinding.clToolbar.edSearch.setVisible()
        eventListBinding.clToolbar.searchCard.setVisible()
        eventListBinding.clToolbar.root.setBackgroundColor(ContextCompat.getColor(requireContext(),CommonResourceColor.colorPrimary))
        eventListBinding.clToolbar.ivBack.setColorFilter(ContextCompat.getColor(requireContext(),CommonResourceColor.white ), android.graphics.PorterDuff.Mode.SRC_IN)

    }

    private fun onSortClick(){
        resetList()
        eventExploreViewModel.getEvents(false,1,sharedFilterViewModel.getFinalFilterQueryMap())
    }

    private fun onFilterApplyClick(){
        if (sharedFilterViewModel.isFilterApplied()){
            eventExploreViewModel.setEvents(sharedExploreViewModel.getEventsLiveData())
        }
        else{
            eventExploreViewModel.getEvents(false,1,sharedFilterViewModel.getFinalFilterQueryMap())
        }
    }

    private fun initHorZCalender(){
       eventListBinding.eventDates.apply {
           calendarViewManager = calenderViewManager
           calendarChangesObserver = calenderListener
           futureDaysCount = 30
           includeCurrentDate = true
           calendarSelectionManager = object : CalendarSelectionManager {
               override fun canBeItemSelected(position: Int, date: Date) = true
           }
           init()
           select(eventExploreViewModel.getSelectedCalenderPosition())
       }
    }


    @SuppressLint("ClickableViewAccessibility")
    private fun initAdapter(){
        layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)
        eventListBinding.rvEventList.layoutManager = layoutManager
        eventListAdapter = EventListAdapter()
        eventListBinding.rvEventList.adapter = eventListAdapter
        eventListAdapter.onClickItemListener = this
        eventListBinding.rvEventList.setLoadMoreListener(scrollMoreListener)
    }

    val scrollMoreListener = object : OnScrollMoreListener(){
        override fun onLoadMore() {
            if (!eventExploreViewModel.isLoading()){
                getEvents(true,eventExploreViewModel.getPagination())
            }
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            if (dy > 0) {
                eventListBinding.fab.shrink()
            } else if (dy < 0) {
                eventListBinding.fab.extend()
            }
        }
    }

    private fun observeLiveData(){
        eventExploreViewModel.eventListLiveData.observe(viewLifecycleOwner,this::onEventList)
        with(viewLifecycleOwner){
            observeEvent(sharedFilterViewModel.onFilterApplyLiveData,{onFilterApplyClick()})
            observeEvent(sharedFilterViewModel.onSortClickLiveData,{onSortClick()})
            observeEvent(sharedFilterViewModel.uiState,this@EventListFragment::handleUIState)
            observeEvent(eventExploreViewModel.uiState,this@EventListFragment::handleUIState)
            observeEvent(sharedFilterViewModel.categoryList,this@EventListFragment::onCategoryList)
        }
    }



    private fun onEventList(data:Triple<Boolean,Int,List<Event>>){
        val (isLoadMore,pagination,eventList) = data
        if (!isLoadMore){
            eventListAdapter.items = eventList.toMutableList()
            eventListBinding.rvEventList.setVisible()
            eventListBinding.eventDates.setVisible()
            eventListBinding.sortFilterCard.setVisible()
            eventListBinding.viewHorZDiv.setVisible()
            eventListBinding.fab.setVisible()
            eventListBinding.root.progressBar.setInVisible(false)
            if (eventList.isEmpty()){
                eventListBinding.tvEmptyStateMsg.visibility = if (eventList.isEmpty()) View.VISIBLE else View.GONE
            }
        }
        else{
            eventListAdapter.updateList(eventList)

        }
        eventExploreViewModel.setLoading(false)
    }

    private fun onCategoryList(categoryList:List<Category>){
        sharedFilterViewModel.setCategories(categoryList)
        sharedExploreViewModel.setFragmentNavigation(FilterListFragment.TAG)
    }


    private fun resetList(){
        eventListAdapter.items.clear()
        eventListAdapter.notifyDataSetChanged()
        eventListBinding.sortFilterCard.setGone()
        eventListBinding.eventDates.setGone()
        eventListBinding.viewHorZDiv.setGone()
    }


    override fun onClick(value: Event, view: View, itemPosition: Int) {
        when(view.id){
            R.id.fabLike->{
                value.isLiked = !value.isLiked
                eventExploreViewModel.likeEvent(value.id,value.isLiked)
                eventListAdapter.notifyItemChanged(itemPosition)
            }
            R.id.listItemParent -> startActivityResult(ProductDetailActivity.getIntent(requireContext(),value.id),ActivityRequestCode.ProductDetailActivity)
        }
    }

    override fun onCustomClick(view: View) {
        when(view.id){
            eventListBinding.clFilter.id ->{
                if(sharedFilterViewModel.categoryList.value == null){
                    sharedFilterViewModel.getCategories(AppConstant.CategoryType.LISTINGS)
                }
                else{
                    sharedExploreViewModel.setFragmentNavigation(FilterListFragment.TAG)
                }
            }
            eventListBinding.clSort.id ->{
                val eventFilter = sharedFilterViewModel.filterLiveData.value!!.find { it.subViewType == FilterUtil.SubViewType.SORT_BY }
                sharedFilterViewModel.setSelectedEventFilter(eventFilter!!)
                val sortDialog = FilterSingleSelectionFragment()
                sortDialog.show(childFragmentManager,null)
            }
            eventListBinding.fab.id->{
                if (eventExploreViewModel.eventListLiveData.value!=null){
                    sharedExploreViewModel.setEvents(eventExploreViewModel.getEventsLiveData())
                    sharedExploreViewModel.setRefreshData()
                }
                sharedExploreViewModel.setFragmentPopup()
            }
            R.id.ivBack-> sharedExploreViewModel.setFragmentPopup()
        }
    }

    private fun handleUIState(uiState: UIState<Unit>){
        when(uiState){
            is UIState.Loading->{
                when(uiState.apiId){
                    RequestID.GET_EVENTS-> {
                        eventListBinding.tvEmptyStateMsg.setGone()
                        eventListBinding.root.progressBar.setInVisible(uiState.isLoading)
                    }
                    RequestID.GET_CATEGORIES ->{
                        if (uiState.isLoading){
                            showLoader(CommonResourceString.please_wait)
                        }
                        else{
                            hideLoader()
                        }
                    }
                }

            }
            is UIState.Failure->{
                eventExploreViewModel.setLoading(false)
                ErrorHandler.handleError(uiState.errorData)
            }
        }
    }

    private val calenderListener = object : CalendarChangesObserver {
        override fun whenSelectionChanged(isSelected: Boolean, position: Int, date: Date) {
            super.whenSelectionChanged(isSelected, position, date)
            if (isSelected){
                if (isManualSelectionEnabled){
                    eventExploreViewModel.setSelectedCalenderPosition(position)
                    resetList()
                    getEvents(Calendar.getInstance().apply { time = date },false,1 )
                }
                isManualSelectionEnabled = true
            }
        }
    }

    private val calenderViewManager = object : CalendarViewManager {
        override fun bindDataToCalendarView(
            holder: SingleRowCalendarAdapter.CalendarViewHolder,
            date: Date,
            position: Int,
            isSelected: Boolean
        ) {
            if(position == 0){
                holder.itemView.tvDate.text = getString(CommonResourceString.explore_event_today)
                holder.itemView.tvDate.chipBackgroundColor = if (isSelected) selectedBackground else whiteBackground
                holder.itemView.tvDate.chipStrokeColor = strokeColor
                holder.itemView.tvDate.chipStrokeWidth = if (isSelected)0f else 1f
                holder.itemView.tvDate.setTextColor(ContextCompat.getColor(requireContext(),if (isSelected)CommonResourceColor.white else CommonResourceColor.colorDarkGrey))
            }
            else{
                if(isSelected){
                    holder.itemView.tvDate.apply{
                        text = DateTimeHelper.getDateString(date,DateTimeHelper.FORMAT_EE_DD)
                        setTextColor(ContextCompat.getColor(requireContext(),CommonResourceColor.colorTextBlack))
                    }
                }
                else{
                    holder.itemView.tvDate.apply{
                        text = DateTimeHelper.getDateString(date,DateTimeHelper.FORMAT_EE_DD)
                        setTextColor(ContextCompat.getColor(requireContext(),CommonResourceColor.colorDarkGrey))
                    }
                }
                holder.itemView.tvDate.chipStrokeColor = strokeColor
                holder.itemView.tvDate.chipBackgroundColor = if (isSelected) selectedBackground else whiteBackground
                holder.itemView.tvDate.chipStrokeWidth = if (isSelected)0f else 1f
                holder.itemView.tvDate.setTextColor(ContextCompat.getColor(requireContext(),if (isSelected)CommonResourceColor.white else CommonResourceColor.colorDarkGrey))
            }
        }

        override fun setCalendarViewResourceId(
            position: Int,
            date: Date,
            isSelected: Boolean
        ): Int {
           return R.layout.list_item_calendar_row
        }
    }

}