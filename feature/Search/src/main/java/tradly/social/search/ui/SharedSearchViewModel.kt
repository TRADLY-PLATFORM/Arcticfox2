package tradly.social.search.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import tradly.social.common.base.AppConstant
import tradly.social.common.base.BaseViewModel
import tradly.social.common.base.SingleEvent

class SharedSearchViewModel:BaseViewModel() {

    private val _fragmentNavigationLiveData by lazy { MutableLiveData<SingleEvent<String>>() }
    val fragmentNavigationLiveData:LiveData<SingleEvent<String>> = _fragmentNavigationLiveData
    private val _searchKeyWord by lazy { MutableLiveData<String>() }
    val searchKeyword:LiveData<String> = _searchKeyWord

    private val _searchByType by lazy { MutableLiveData(AppConstant.FilterType.PRODUCTS) }
    val searchByType get() = _searchByType

    private val _showFilterSortLiveData by lazy { MutableLiveData<SingleEvent<Unit>>() }
    val showFilterSortLiveData get() = _showFilterSortLiveData

    fun setSearchByType(type:Int){
        this._searchByType.value = type
    }

    fun showFilterSort(){
        _showFilterSortLiveData.value = SingleEvent(Unit)
    }

    fun getSearchByType() = this._searchByType.value!!

    fun setFragmentNavigation(tag:String){
        _fragmentNavigationLiveData.value = SingleEvent(tag)
    }

    fun setSearchKeyword(searchKey:String){
        _searchKeyWord.value = searchKey
    }
}