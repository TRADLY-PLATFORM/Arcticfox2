package tradly.social.event.explore.ui

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.collection.LruCache
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.layout_explore_toolbar.view.*
import tradly.social.common.*
import tradly.social.common.base.*
import tradly.social.common.network.NetworkConstant
import tradly.social.common.network.RequestID
import tradly.social.common.resources.CommonResourceColor
import tradly.social.common.resources.CommonResourceDrawable
import tradly.social.common.resources.CommonResourceString
import tradly.social.domain.entities.Event
import tradly.social.event.explore.R
import tradly.social.event.explore.adapter.EventListAdapter
import tradly.social.event.explore.common.*
import tradly.social.event.explore.databinding.EventExploreFragmentBinding
import tradly.social.event.explore.ui.filter.*
import tradly.social.event.explore.ui.filter.viewmodel.EventExploreViewModel
import tradly.social.common.filter.SharedFilterViewModel
import tradly.social.common.base.BaseFragment
import tradly.social.ui.product.addProduct.productDetail.ProductDetailActivity
import java.util.*
import kotlin.collections.HashMap

class EventExploreFragment : BaseFragment(), OnMapReadyCallback,
    GenericAdapter.OnClickItemListener<Event>, CustomOnClickListener.OnCustomClickListener,GoogleMap.OnInfoWindowClickListener,GoogleMap.OnMarkerClickListener {

    private var googleMap:GoogleMap?=null
    private lateinit var eventExploreViewModel: EventExploreViewModel
    private lateinit var sharedEventExploreViewModel: EventExploreViewModel
    private lateinit var sharedFilterViewModel: SharedFilterViewModel
    private lateinit var markerBitmapCache:LruCache<Int,Bitmap>
    private var markersCache:HashMap<String,Marker?> = hashMapOf()
    private lateinit var eventExploreFragmentBinding:EventExploreFragmentBinding
    private lateinit var adapter:EventListAdapter
    private var currentlySelectedMarker:Marker?=null

    companion object{
        const val TAG = "EventExploreFragment"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        eventExploreViewModel = getViewModel { EventExploreViewModel() }
        sharedEventExploreViewModel = activity!!.getViewModel { EventExploreViewModel() }
        sharedFilterViewModel = activity!!.getViewModel { SharedFilterViewModel() }
        initMarkerCache()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        eventExploreFragmentBinding = DataBindingUtil.inflate(inflater,R.layout.fragment_event_explore,container,false)
        eventExploreFragmentBinding.lifecycleOwner = viewLifecycleOwner
        eventExploreFragmentBinding.onClickListener = CustomOnClickListener(this)
        return eventExploreFragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setToolbarView()
        eventExploreFragmentBinding.mapView.onCreate(savedInstanceState)
        eventExploreFragmentBinding.mapView.getMapAsync(this)
        initAdapter()
        observeLiveData()
        if (eventExploreViewModel.eventListLiveData.value == null){
            getEvents(Calendar.getInstance(),false,1)
        }
        if (sharedFilterViewModel.filterLiveData.value == null){
            sharedFilterViewModel.getFilters()
        }
    }

    private fun setToolbarView(){
        eventExploreFragmentBinding.clToolbar.currentLocationGroup.setVisible()
        eventExploreFragmentBinding.clToolbar.root.setVisible()
        eventExploreFragmentBinding.clToolbar.toolbarTitle.setGone()
        eventExploreFragmentBinding.clToolbar.tvSearchHint.setVisible()
        eventExploreFragmentBinding.clToolbar.edSearch.setGone()
        eventExploreFragmentBinding.clToolbar.searchCard.setVisible()
        eventExploreFragmentBinding.clToolbar.root.setBackgroundColor(ContextCompat.getColor(requireContext(),CommonResourceColor.colorTransparent))
        eventExploreFragmentBinding.clToolbar.ivBack.setColorFilter(ContextCompat.getColor(requireContext(),CommonResourceColor.colorDarkGrey ), android.graphics.PorterDuff.Mode.SRC_IN)

    }

    private fun getEvents(calendar: Calendar, isLoadMore: Boolean, pagination:Int){
        val startDate = DateTimeHelper.getDate(calendar = calendar,includeTime = false,format =  DateTimeHelper.FORMAT_DATE_YYYY_MM_DD_HH_MM_SS_Z)
        val endDate = DateTimeHelper.getDate(calendar = calendar,includeTime = false, format = DateTimeHelper.FORMAT_DATE_YYYY_MM_DD_HH_MM_SS_Z,nextOffset = 1)
        sharedFilterViewModel.getFinalFilterQueryMap().apply {
            this[NetworkConstant.QueryParam.START_AT] = startDate
            this[NetworkConstant.QueryParam.END_AT] = endDate
            this[NetworkConstant.QueryParam.PAGE] = pagination
        }
        getEvents(isLoadMore,pagination)
    }

    private fun getEvents(isLoadMore:Boolean,pagination: Int){
        if (PermissionHelper.checkPermission(requireContext(), PermissionHelper.Permission.LOCATION)){
            LocationHelper.getInstance().getMyLocation {
                sharedFilterViewModel.getFinalFilterQueryMap()[NetworkConstant.QueryParam.LATITUDE] = it.latitude
                sharedFilterViewModel.getFinalFilterQueryMap()[NetworkConstant.QueryParam.LONGITUDE] = it.longitude
                eventExploreViewModel.getEvents(isLoadMore,pagination,sharedFilterViewModel.getFinalFilterQueryMap())
            }
        }
        else{
            eventExploreViewModel.getEvents(isLoadMore,pagination,sharedFilterViewModel.getFinalFilterQueryMap())
        }
    }

    private fun initMarkerCache(){
        markerBitmapCache = LruCache<Int,Bitmap>(2)
        markerBitmapCache.put(CommonResourceDrawable.ic_pin_selected_marker,CommonResourceDrawable.ic_pin_selected_marker.toBitmap(requireContext()))
        markerBitmapCache.put(R.drawable.ic_marker_pin,R.drawable.ic_marker_pin.toBitmap(requireContext()))
    }


    private fun observeLiveData(){
        viewLifecycleOwner.observeEvent(eventExploreViewModel.fragmentPopup,{childFragmentManager.popBackStack()})
        eventExploreViewModel.eventListLiveData.observe(viewLifecycleOwner,this::onEventResponse)
        viewLifecycleOwner.observeEvent(sharedEventExploreViewModel.refreshData,{onRefreshData()})
        viewLifecycleOwner.observeEvent(eventExploreViewModel.showCurrentLocation,{showMyLocation()})
        viewLifecycleOwner.observeEvent(eventExploreViewModel.uiState,this::handleUIState)
    }


    private fun onEventResponse(data:Triple<Boolean,Int,List<Event>>){
        val (isLoadMore,pagination,list) = data
        populateMarkers(data.third,data.first)
        if (isLoadMore) {
            adapter.updateList(list)
        } else {
            adapter.items = list.toMutableList()
            eventExploreFragmentBinding.apply {
                fab.show()
                rvEventList.setVisible()
            }
            if (adapter.items.isEmpty()){
                showSnackbar(eventExploreFragmentBinding.root,CommonResourceString.event_explore_no_events_found,CommonResourceString.event_explore_no_events_found_dismiss)
            }
        }
        sharedEventExploreViewModel.setEvents(Triple(isLoadMore,pagination,adapter.items))
    }

    private fun initAdapter(){
        eventExploreFragmentBinding.rvEventList.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.HORIZONTAL,false)
        adapter = EventListAdapter().apply {
            setViewType(EventListAdapter.ViewType.HORZ_LIST)
            onClickItemListener = this@EventExploreFragment
        }
        eventExploreFragmentBinding.rvEventList.adapter = adapter
        eventExploreFragmentBinding.rvEventList.addOnScrollListener(scrollListener)
    }

    private fun onRefreshData(){
        eventExploreViewModel.setEvents(sharedEventExploreViewModel.getEventsLiveData())
    }

    private fun populateMarkers(list: List<Event>,isLoadMore: Boolean){
        this.googleMap?.let { googleMap ->
            if (!isLoadMore){
                markersCache.clear()
                googleMap.clear()
                currentlySelectedMarker = null
            }
            list.filter { it.location.latitude!=0.0 }.forEachIndexed { index, event ->
                val snippet = getString(CommonResourceString.placeholder_two_with_hyphen,DateTimeHelper.getDateFromTimeMillis(event.startAt,DateTimeHelper.FORMAT_TIME_AM_PM),DateTimeHelper.getDateFromTimeMillis(event.endAt,DateTimeHelper.FORMAT_TIME_AM_PM))
                val marker = googleMap.addMarker(event.location, markerBitmapCache[R.drawable.ic_marker_pin], Pair(event.title,snippet),event)
                markersCache[event.id] = marker
            }
        }
    }


    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        googleMap.uiSettings.isMyLocationButtonEnabled = false
        googleMap.uiSettings.isScrollGesturesEnabled = true
        googleMap.uiSettings.isZoomGesturesEnabled = true
        googleMap.uiSettings.isZoomControlsEnabled = false
        googleMap.uiSettings.isMapToolbarEnabled = false
        googleMap.isIndoorEnabled = false
        googleMap.uiSettings.isCompassEnabled = false
        if (PermissionHelper.checkPermission(requireContext(), PermissionHelper.Permission.LOCATION)){
            googleMap.isMyLocationEnabled = true
        }
        showMyLocation()
        setMarkerListener()
    }

    private fun setMarkerListener(){
        this.googleMap?.let {
            it.setOnInfoWindowClickListener(this)
            it.setOnMarkerClickListener(this)
        }
    }

    override fun onInfoWindowClick(marker: Marker) {
        (marker.tag as? Event)?.let { event ->
            startActivityResult(ProductDetailActivity.getIntent(requireContext(),event.id),ActivityRequestCode.ProductDetailActivity)
        }
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        (marker.tag as? Event)?.let { event ->
            val index = adapter.items.indexOfFirst { it.id == event.id }
            if (index!=-1){
                eventExploreFragmentBinding.rvEventList.scrollToPosition(index)
            }
            (currentlySelectedMarker?.tag as? Event)?.let { currentlySelectedEvent->
                if (currentlySelectedEvent.id == (marker.tag as? Event)?.id){
                    return false
                }
            }
            marker.setIcon(BitmapDescriptorFactory.fromBitmap(markerBitmapCache[CommonResourceDrawable.ic_pin_selected_marker]!!))
            currentlySelectedMarker?.setIcon(BitmapDescriptorFactory.fromBitmap(markerBitmapCache[R.drawable.ic_marker_pin]!!))
            currentlySelectedMarker = marker
        }
        return false
    }



    private val scrollListener = object :RecyclerView.OnScrollListener(){
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (newState == RecyclerView.SCROLL_STATE_IDLE){
                (eventExploreFragmentBinding.rvEventList.layoutManager as LinearLayoutManager).let {
                    val position = it.findFirstCompletelyVisibleItemPosition()
                    adapter.items.getOrNull(position)?.let {  event->
                        currentlySelectedMarker?.setIcon(BitmapDescriptorFactory.fromBitmap(markerBitmapCache[R.drawable.ic_marker_pin]!!))
                        val marker = markersCache[event.id]
                        marker?.let {
                            currentlySelectedMarker = it
                            marker.setIcon(BitmapDescriptorFactory.fromBitmap(markerBitmapCache[CommonResourceDrawable.ic_pin_selected_marker]!!))
                            googleMap?.let {
                               val zoom =  it.cameraPosition.zoom
                               it.animateCamera(CameraUpdateFactory.zoomTo(LocationHelper.LocationConstant.DEFAULT_ZOOM))
                            }
                        }
                    }
                }
            }
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
        }
    }

    private fun showMyLocation(){
        if (PermissionHelper.checkPermission(requireContext(), PermissionHelper.Permission.LOCATION)){
            LocationHelper.getInstance().checkSettings { success, exception ->
                if (success){
                    LocationHelper.getInstance().getMyLocation {
                        moveCamera(LatLng(it.latitude,it.longitude))
                    }
                }
                else{
                    exception!!.startResolutionForResult(requireActivity(), LocationHelper.LocationConstant.REQUEST_CHECK_SETTINGS)
                }
            }
        }
        else{
            PermissionHelper.requestPermission(this,
                PermissionHelper.Permission.LOCATION,
                PermissionHelper.RequestCode.LOCATION)
        }
    }

    private fun moveCamera(latLng: LatLng){
        this.googleMap?.let { googleMap ->
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, LocationHelper.LocationConstant.DEFAULT_ZOOM)
            googleMap.moveCamera(cameraUpdate)
            googleMap.animateCamera(cameraUpdate)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == LocationHelper.LocationConstant.REQUEST_CHECK_SETTINGS){
            if (PermissionHelper.checkPermission(requireContext(), PermissionHelper.Permission.LOCATION)){
                showMyLocation()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == PermissionHelper.RequestCode.LOCATION){
            if (grantResults.size>1 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                googleMap?.isMyLocationEnabled = true
                showMyLocation()
            }
        }
    }

    override fun onClick(value: Event, view: View, itemPosition: Int) {
        when(view.id){
            R.id.listItemParent -> startActivityResult(ProductDetailActivity.getIntent(requireContext(),value.id),ActivityRequestCode.ProductDetailActivity)
            R.id.fabLike -> {
                value.isLiked = !value.isLiked
                eventExploreViewModel.likeEvent(value.id,value.isLiked)
                adapter.notifyItemChanged(itemPosition)
            }
        }
    }

    override fun onCustomClick(view: View) {
        when(view.id){
            eventExploreFragmentBinding.fab.id,
            eventExploreFragmentBinding.clToolbar.tvSearchHint.id->{
                sharedEventExploreViewModel.setFragmentNavigation(EventListFragment.TAG)
            }
            eventExploreFragmentBinding.clToolbar.ivCurrentLocation.id -> showMyLocation()
            R.id.ivBack -> requireActivity().finish()
        }
    }

    private fun handleUIState(uiState: UIState<Unit>){
        when(uiState){
            is UIState.Loading->{
               when(uiState.apiId){
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
        }
    }

    private fun showSnackbar(view: View,@StringRes label:Int,actionLabel:Int = 0,action:(()->Unit)?=null){
        Snackbar.make(view,label,Snackbar.LENGTH_SHORT).apply {
            if (actionLabel!=0){
                setAction(actionLabel){
                    action?.let { it() }
                }
            }
            show()
        }
    }

    override fun onResume() {
        super.onResume()
        eventExploreFragmentBinding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        eventExploreFragmentBinding.mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        eventExploreFragmentBinding.mapView.onDestroy()
        markerBitmapCache.evictAll()
    }
}