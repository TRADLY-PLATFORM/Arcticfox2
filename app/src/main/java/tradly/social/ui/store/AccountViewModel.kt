package tradly.social.ui.store

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import tradly.social.common.base.BaseViewModel
import tradly.social.common.base.SingleEvent
import tradly.social.common.base.UIState
import tradly.social.common.network.RequestID
import tradly.social.common.network.feature.common.datasource.StoreDataSourceImpl
import tradly.social.domain.entities.AppError
import tradly.social.domain.entities.User
import tradly.social.domain.repository.StoreRepository
import tradly.social.domain.usecases.GetAccountFollowers

class AccountViewModel:BaseViewModel() {

    var page:Int = 1
    var isPaginationEnd:Boolean = false
    var isLoading:Boolean = false
    private val getAccountFollowers by lazy { GetAccountFollowers(StoreRepository(StoreDataSourceImpl())) }
    private val _accountFollowersLiveData:MutableLiveData<List<User>> by lazy { MutableLiveData() }
    val accountFollowersLiveData:LiveData<List<User>> = _accountFollowersLiveData

    fun getFollowers(accountId:String,isLoadMore:Boolean){
        isLoading = true
        getApiResult(viewModelScope,{getAccountFollowers.execute(accountId, page)},{ usersList->
            _accountFollowersLiveData.value = usersList
            this.page = if (usersList.isNotEmpty()){
                isPaginationEnd = false
                page+1
            }
            else{
                page
            }
        },!isLoadMore,RequestID.GET_ACCOUNT_FOLLOWERS)
    }

    override fun showAPIProgress(show: Boolean, apiId: Int) {
        _uiState.value = SingleEvent(UIState.Loading(show,apiId))
    }

    override fun onFailure(apiError: AppError) {
        showAPIProgress(false,apiError.apiId)
        _uiState.value = SingleEvent(UIState.Failure(apiError))
        isLoading = false
    }
}