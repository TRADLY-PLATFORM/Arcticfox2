package tradly.social.search.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.AppBarLayout
import tradly.social.common.base.*
import tradly.social.common.filter.*
import tradly.social.common.base.getString
import tradly.social.common.network.NetworkConstant
import tradly.social.common.network.RequestID
import tradly.social.search.R
import tradly.social.search.databinding.ActivitySearchBinding
import tradly.social.common.resources.CommonResourceDrawable
import tradly.social.common.resources.CommonResourceString
import tradly.social.common.base.setGone
import tradly.social.common.base.setVisible
import tradly.social.domain.entities.Category

class SearchActivity : BaseActivity() {

    private lateinit var binding:ActivitySearchBinding
    private lateinit var sharedSearchViewModel: SharedSearchViewModel
    private lateinit var sharedFilterViewModel:SharedFilterViewModel
    private var showFilterMenu:Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setToolbar(binding.layoutAppToolbar.toolbar,CommonResourceString.search_header_title,CommonResourceDrawable.ic_back)
        sharedSearchViewModel = getViewModel { SharedSearchViewModel() }
        sharedFilterViewModel = getViewModel { SharedFilterViewModel() }
        launchFragment(SearchSuggestionFragment(),false,SearchSuggestionFragment.TAG)
        observeLiveData()
        setListener()
    }

    private fun setListener(){

        binding.searchBarLayout.clSearchBy.setOnClickListener {
            val dialog = SearchByBottomSheetFragment()
            dialog.show(supportFragmentManager,SearchByBottomSheetFragment.TAG)
        }

        binding.clFilter.setOnClickListener { getCategories() }

        binding.clSort.setOnClickListener {
            val eventFilter = sharedFilterViewModel.filterLiveData.value!!.find { it.subViewType == FilterUtil.SubViewType.SORT_BY }
            sharedFilterViewModel.setSelectedEventFilter(eventFilter!!)
            val dialog = FilterSingleSelectionFragment()
            dialog.show(supportFragmentManager,FilterSingleSelectionFragment.TAG)
        }

        binding.searchBarLayout.edSearch.setOnEditorActionListener { v, actionId, event ->
            when (actionId) {
                EditorInfo.IME_ACTION_SEARCH -> {
                    Utils.hideKeyBoard(this)
                    val keyWord = binding.searchBarLayout.edSearch.getString()
                    if (keyWord.isNotEmpty()) {
                        sharedSearchViewModel.setSearchKeyword(keyWord)
                    }
                    true
                }
                else -> false
            }
        }

        supportFragmentManager.addOnBackStackChangedListener {
            val fragment = supportFragmentManager.findFragmentById(R.id.fragContainer)
            if (fragment != null && fragment.isVisible && !fragment.tag.isNullOrEmpty()) {
                when(fragment.tag){
                    SearchSuggestionFragment.TAG->{
                        showFilterMenu = false
                        binding.searchBarLayout.searchBarLayout.setVisible()
                        binding.layoutAppToolbar.toolbar.title = getString(CommonResourceString.search_header_title)
                        binding.cvSortFilter.setGone()
                        sharedFilterViewModel.clearAllQuery()
                        invalidateOptionsMenu()
                    }
                    SearchFragment.TAG ->{
                        showFilterMenu = sharedSearchViewModel.searchByType.value == AppConstant.FilterType.ACCOUNTS
                        enableScrollFlagOnToolbar(true)
                        binding.searchBarLayout.searchBarLayout.setGone()
                        if (sharedSearchViewModel.searchByType.value == AppConstant.FilterType.PRODUCTS){
                            binding.cvSortFilter.setVisible()
                        }
                        binding.layoutAppToolbar.toolbar.title = sharedSearchViewModel.searchKeyword.value
                        invalidateOptionsMenu()
                    }
                    FilterListFragment.TAG->{
                        showFilterMenu = false
                        enableScrollFlagOnToolbar(false)
                        binding.searchBarLayout.searchBarLayout.setGone()
                        binding.cvSortFilter.setGone()
                        binding.layoutAppToolbar.toolbar.title = getString(CommonResourceString.explore_event_filters)
                        invalidateOptionsMenu()
                    }
                    FilterMultiSelectFragment.TAG->{
                        showFilterMenu = false
                        binding.searchBarLayout.searchBarLayout.setGone()
                        binding.cvSortFilter.setGone()
                        invalidateOptionsMenu()
                    }
                }
            }
        }
    }

    private fun getCategories(){
        val type =  when(sharedSearchViewModel.getSearchByType()){
            AppConstant.FilterType.ACCOUNTS-> NetworkConstant.Param.ACCOUNT
            AppConstant.FilterType.PRODUCTS-> NetworkConstant.Param.LISTINGS
            else-> AppConstant.EMPTY
        }
        sharedFilterViewModel.getCategories(type)
    }

    private fun observeLiveData(){
        observeEvent(sharedFilterViewModel.fragmentPopup,{ supportFragmentManager.popBackStack()})
        observeEvent(sharedFilterViewModel.categoryList,this::onCategoryList)
        observeEvent(sharedFilterViewModel.uiState,this::handleUIState)
        observeEvent(sharedFilterViewModel.fragmentNavigation,this::onFragmentNavigation)
        observeEvent(sharedSearchViewModel.fragmentNavigationLiveData,this::onFragmentNavigation)
        sharedSearchViewModel.searchByType.observe(this,this::setSearchByView)
        observeEvent(sharedSearchViewModel.showFilterSortLiveData,{
            if (sharedSearchViewModel.searchByType.value == AppConstant.FilterType.PRODUCTS){
                binding.cvSortFilter.setVisible()
            }
        })
        observeEvent(sharedFilterViewModel.toolbarTitleLiveData,{
            binding.layoutAppToolbar.toolbar.title = it
        })
    }

    private fun onCategoryList(list: List<Category>){
        sharedFilterViewModel.setCategories(list)
        sharedSearchViewModel.setFragmentNavigation(FilterListFragment.TAG)
    }

    private fun setSearchByView(type:Int){
        when(type){
            AppConstant.FilterType.ACCOUNTS-> binding.searchBarLayout.tvSearchBy.text = getString(R.string.search_stores)
            AppConstant.FilterType.PRODUCTS-> binding.searchBarLayout.tvSearchBy.text = getString(R.string.search_products)
        }
    }

    private fun launchFragment(fragment:Fragment,addToBackStack:Boolean,tag:String){
        with(supportFragmentManager.beginTransaction()){
            add(R.id.fragContainer,fragment,tag)
            if (addToBackStack){
               addToBackStack(tag)
            }
            commit()
        }
    }

    private fun onFragmentNavigation(tag: String){
        when(tag){
            SearchFragment.TAG -> launchFragment(SearchFragment(),true,SearchFragment.TAG)
            FilterListFragment.TAG ->{
                launchFragment(FilterListFragment.newInstance(sharedSearchViewModel.getSearchByType()),true,FilterListFragment.TAG)
            }
            FilterMultiSelectFragment.TAG -> launchFragment(FilterMultiSelectFragment.newInstance(sharedSearchViewModel.getSearchByType()),true,FilterMultiSelectFragment.TAG)
        }
    }

    private fun enableScrollFlagOnToolbar(enable:Boolean){
        val params = binding.collapsingToolbar.layoutParams as AppBarLayout.LayoutParams
        if(enable){
            params.scrollFlags = AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL
        }
        else{
            params.scrollFlags = 0
        }

    }

    private fun handleUIState(uiState: UIState<Unit>){
        when(uiState){
            is UIState.Loading->{
                if (uiState.apiId == RequestID.GET_CATEGORIES){
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.search_toolbar_menu, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.findItem(R.id.action_filter)?.isVisible = showFilterMenu
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_filter){
            getCategories()
        }
        return super.onOptionsItemSelected(item)
    }

}