package tradly.social.event.eventbooking.mybooking

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import tradly.social.common.base.*
import tradly.social.common.cache.AppCache
import tradly.social.common.network.RequestID
import tradly.social.common.base.setLoadMoreListener
import tradly.social.domain.entities.EventBooking
import tradly.social.event.eventbooking.EventBookingDetailFragment
import tradly.social.event.eventbooking.EventBookingViewModel
import tradly.social.event.eventbooking.R
import tradly.social.event.eventbooking.SharedEventBookingViewModel
import tradly.social.event.eventbooking.adapter.BookingListAdapter
import tradly.social.event.eventbooking.databinding.BookingListFragmentBinding
import tradly.social.common.base.BaseFragment

class BookingListFragment : BaseFragment(),GenericAdapter.OnClickItemListener<EventBooking> {

    private lateinit var binding:BookingListFragmentBinding
    private lateinit var bookingViewModel:EventBookingViewModel
    private lateinit var bookingLisAdapter:BookingListAdapter
    private lateinit var sharedBookingViewModel:SharedEventBookingViewModel
    private var isPaginationEnd:Boolean = false
    private val accountId = AppCache.getUserStore()?.id?:AppConstant.EMPTY

    companion object{
        const val TAG = "BookingListFragment"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bookingViewModel = getViewModel { EventBookingViewModel() }
        sharedBookingViewModel = requireActivity().getViewModel { SharedEventBookingViewModel() }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_booking_list,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initAdapter()
        observeLiveData()
        getBookingList(1,false)
    }

    private fun initAdapter(){
        bookingLisAdapter = BookingListAdapter()
        binding.rvBookingList.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)
        binding.rvBookingList.adapter = bookingLisAdapter
        binding.rvBookingList.setLoadMoreListener(loadMoreListener)
    }

    private fun getBookingList(pagination:Int,isLoadMore:Boolean){
        bookingViewModel.getBookingEvents(pagination,accountId,AppConstant.ModuleType.EVENTS)
        bookingViewModel.isLoading = true
    }

    private fun observeLiveData(){
        viewLifecycleOwner.observeEvent(bookingViewModel.uiState,this::handleUIState)
        bookingViewModel.eventBookingListLiveData.observe(viewLifecycleOwner,this::populateEventBookingList)
    }

    private fun populateEventBookingList(triple: Triple<Boolean,Int,List<EventBooking>>){
        val (isLoadMore, pagination, bookingList) = triple
        if (bookingList.isNotEmpty()) {
            if (isLoadMore) {
                bookingLisAdapter.updateList(bookingList)
            } else {
                bookingLisAdapter.items = bookingList.toMutableList()
            }
        } else {
            isPaginationEnd = false
        }
        bookingViewModel.isLoading = false
    }

    private fun handleUIState(uiState: UIState<Unit>){
        when(uiState){
            is UIState.Loading->{
                if (uiState.apiId == RequestID.GET_BOOKING_EVENTS){
                    binding.progressBar.visibility = if (uiState.isLoading) View.VISIBLE else View.GONE
                }
            }
            is UIState.Failure->{
                bookingViewModel.isLoading = false
                ErrorHandler.handleError(uiState.errorData)
            }
        }
    }

    override fun onClick(value: EventBooking, view: View, itemPosition: Int) {
        when(view.id){
            R.id.listItemParent ->{
                sharedBookingViewModel.setEventBooking(value)
                sharedBookingViewModel.setFragmentNavigation(EventBookingDetailFragment.TAG)
            }
        }
    }

    private val loadMoreListener = object : OnScrollMoreListener(){
        override fun onLoadMore() {
            if (!bookingViewModel.isLoading){
                val pagination = bookingViewModel.eventBookingListLiveData.value?.second!!
                bookingViewModel.getBookingEvents(pagination,accountId,AppConstant.ModuleType.EVENTS)
            }
        }
    }
}