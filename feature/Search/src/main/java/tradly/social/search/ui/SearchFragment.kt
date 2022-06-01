package tradly.social.search.ui

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import tradly.social.adapter.ListingAdapter
import tradly.social.common.base.SpaceGrid
import tradly.social.common.base.*
import tradly.social.common.cache.AppCache
import tradly.social.common.filter.SharedFilterViewModel
import tradly.social.common.network.NetworkConstant
import tradly.social.common.network.RequestID
import tradly.social.common.base.setGone
import tradly.social.common.base.setVisible
import tradly.social.domain.entities.Product
import tradly.social.domain.entities.Store
import tradly.social.search.R
import tradly.social.search.databinding.SearchFragmentBinding
import tradly.social.ui.login.AuthenticationActivity

class SearchFragment : Fragment() {

    lateinit var binding: SearchFragmentBinding
    lateinit var sharedSearchViewModel: SharedSearchViewModel
    lateinit var searchViewModel: SearchViewModel
    lateinit var sharedFilterViewModel: SharedFilterViewModel
    lateinit var storeListAdapter:ListingAdapter
    lateinit var productListAdapter:ListingAdapter
    private val storeList:MutableList<Store> = mutableListOf()
    private val productList:MutableList<Product> = mutableListOf()
    private var isPaginationEnd:Boolean = false
    private var isLoading = false

    companion object{
        const val TAG = "SearchFragment"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        sharedSearchViewModel = requireActivity().getViewModel { SharedSearchViewModel() }
        searchViewModel = getViewModel { SearchViewModel() }
        sharedFilterViewModel = requireActivity().getViewModel { SharedFilterViewModel() }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_search,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().invalidateOptionsMenu()
        observeLiveData()
        initQueryParams()
        initAdapter()
        doSearch(false,1)
        sharedFilterViewModel.getFilters()
    }

    private fun initQueryParams(){
        sharedFilterViewModel.getFinalFilterQueryMap().apply {
            this[NetworkConstant.QueryParam.PAGE] = 1
            this[NetworkConstant.QueryParam.SEARCH_KEY] = sharedSearchViewModel.searchKeyword.value
            this[NetworkConstant.QueryParam.SORT] = AppConstant.SORT.RECENT
        }
    }

    private fun initAdapter(){
        storeListAdapter = ListingAdapter(requireContext(),storeList,AppConstant.ListingType.STORE_LIST,binding.rvList){ position, obj ->
            if (AppCache.getCacheUser()!=null){
                val store = obj as Store
                if (store.followed){
                    searchViewModel.followStore(store.id)
                }
                else{
                    searchViewModel.unFollowStore(store.id)
                }
            }
            else{
                startActivityForResult(Intent(requireContext(),AuthenticationActivity::class.java),AppConstant.ActivityResultCode.LOGIN_FROM_SEARCH)
            }
        }
        productListAdapter =  ListingAdapter(requireContext(),productList,AppConstant.ListingType.PRODUCT_LIST,binding.rvList){ position, obj ->

        }
        productListAdapter.setOnLoadMoreListener(onLoadMoreListener)
        storeListAdapter.setOnLoadMoreListener(onLoadMoreListener)
        binding.rvList.addItemDecoration(SpaceGrid(2, 15, true))
        binding.rvList.layoutManager = GridLayoutManager(requireContext(),2,GridLayoutManager.VERTICAL,false)
        when(sharedSearchViewModel.getSearchByType()){
            AppConstant.FilterType.ACCOUNTS->binding.rvList.adapter = storeListAdapter
            AppConstant.FilterType.PRODUCTS -> binding.rvList.adapter = productListAdapter
        }
    }

    private val onLoadMoreListener = object :OnLoadMoreListener{
        override fun onLoadMore() {
            if (!isLoading && !isPaginationEnd){
               val pagination = when(sharedSearchViewModel.getSearchByType()){
                    AppConstant.FilterType.PRODUCTS-> searchViewModel.productListLiveData.value?.second!!
                    AppConstant.FilterType.ACCOUNTS-> searchViewModel.storeListLiveData.value?.second!!
                    else -> 0
                }
                doSearch(true,pagination)
            }
        }
    }

    private fun observeLiveData(){
        observeEvent(sharedFilterViewModel.onSortClickLiveData,{
            resetList()
            searchViewModel.getSearchList(sharedSearchViewModel.getSearchByType(),false,1,sharedFilterViewModel.getFinalFilterQueryMap())
        })
        viewLifecycleOwner.observeEvent(sharedFilterViewModel.onFilterApplyLiveData,{ onRefreshSearchList() })
        searchViewModel.productListLiveData.observe(viewLifecycleOwner,this::onProductList)
        searchViewModel.storeListLiveData.observe(viewLifecycleOwner,this::onStoreList)
        viewLifecycleOwner.observeEvent(searchViewModel.uiState,this::handleUIState)
    }

    private fun resetList(){
        when(sharedSearchViewModel.getSearchByType()){
            AppConstant.FilterType.PRODUCTS->{
                productList.clear()
                productListAdapter.notifyDataSetChanged()
            }
            AppConstant.FilterType.ACCOUNTS->{
                storeList.clear()
                storeListAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun onStoreList(triple: Triple<Boolean,Int,List<Store>>){
        val (isLoadMore,pagination,list) = triple
        if (isLoadMore){
            if (list.isNotEmpty()){
                val index = storeList.size
                storeList.addAll(list)
                storeListAdapter.notifyItemRangeInserted(index,list.size)
            }
            else{
               isPaginationEnd = false
            }
        }
        else{

            if (list.isNotEmpty()){
                binding.rvList.setVisible()
                storeList.addAll(list)
            }
            else{
                isPaginationEnd = false
                storeList.clear()
                binding.rvList.setGone()
                binding.emptyStateGroup.setVisible()
            }
            storeListAdapter.notifyDataSetChanged()
            sharedSearchViewModel.showFilterSort()
        }
        isLoading = false
    }

    private fun onProductList(triple: Triple<Boolean,Int,List<Product>>){
        val (isLoadMore,pagination,list) = triple
        if (isLoadMore){
            if (list.isNotEmpty()){
                val index = storeList.size
                productList.addAll(list)
                productListAdapter.notifyItemRangeInserted(index,list.size)
            }
            else{
                isPaginationEnd = false
            }
        }
        else{
            if (list.isNotEmpty()){
                binding.rvList.setVisible()
                productList.addAll(list)
            }
            else{
                isPaginationEnd = false
                productList.clear()
                binding.rvList.setGone()
                binding.emptyStateGroup.setVisible()
            }
            productListAdapter.notifyDataSetChanged()
            sharedSearchViewModel.showFilterSort()
        }
        isLoading = false
    }

    private fun doSearch(isLoadMore:Boolean,pagination:Int){
        val queryMap = sharedFilterViewModel.getFinalFilterQueryMap().apply {
            this[NetworkConstant.QueryParam.PAGE] = pagination
        }
        searchViewModel.getSearchList(sharedSearchViewModel.getSearchByType(),isLoadMore,pagination,queryMap)
    }

    private fun handleUIState(uiState: UIState<Unit>){
        when(uiState){
            is UIState.Loading->{
                if (uiState.apiId == RequestID.GET_STORES || uiState.apiId == RequestID.GET_PRODUCTS){
                    binding.emptyStateGroup.setGone()
                    binding.progressBar.visibility = if (uiState.isLoading) View.VISIBLE else View.GONE
                }
            }
            is UIState.Failure->{
                isLoading = false
                ErrorHandler.handleError(uiState.errorData)
            }
        }
    }

    private fun onRefreshSearchList(){
        if (sharedFilterViewModel.isFilterApplied()){
            when(sharedSearchViewModel.searchByType.value){
                AppConstant.FilterType.ACCOUNTS-> searchViewModel.setStoreList(sharedFilterViewModel.getFilterFinalStoreList())
                AppConstant.FilterType.PRODUCTS-> searchViewModel.setProductList(sharedFilterViewModel.getFilterFinalProductList())
            }
        }
        else{
            resetList()
            doSearch(false ,1)
        }
    }
}