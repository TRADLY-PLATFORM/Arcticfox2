package tradly.social.common.base

import androidx.lifecycle.ViewModel

import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.*
import tradly.social.common.network.APIRequest
import tradly.social.common.network.NetworkError
import tradly.social.data.model.CoroutinesManager
import tradly.social.domain.entities.*


open class BaseViewModel:ViewModel() {

    protected var _uiState = MutableLiveData<SingleEvent<UIState<Unit>>>()
    val uiState get() = _uiState

    protected fun <T:Any> getApiResult(scope: CoroutineScope, work:suspend (() -> Result<T>), callback: ((T) -> Unit)? = null, showProgress:Boolean = false,@APIRequest.ID apiId:Int){
        if(NetworkUtil.isConnectingToInternet()){
            if(showProgress){
                showAPIProgress(true,apiId)
            }
            CoroutinesManager.ioThenMain(scope,work,{ result ->
                if(showProgress){
                    showAPIProgress(false,apiId)
                }
                when(result){
                    is Result.Success->callback?.let { it(result.data) }
                    is Result.Error-> onFailure(result.exception.also { it.apiId = apiId})
                }
            })
        }
        else{
            onFailure(AppError(errorType = NetworkError.NO_NETWORK,apiId = apiId))
        }
    }

    protected fun <T:Any> getApiResults(scope: CoroutineScope, vararg work:suspend (() -> Result<T>), callback: ((List<Result<Any>>) -> Unit)? = null, showProgress:Boolean = false, apiId:Int=0){
        if(NetworkUtil.isConnectingToInternet()){
            scope.launch {
                if(showProgress){
                    showAPIProgress(true,apiId)
                }
                val deferredList:MutableList<Deferred<Result<Any>>> = mutableListOf()
                work.forEach {
                    deferredList.add(async(Dispatchers.IO){ return@async it() })
                }
                callback?.let { it(deferredList.awaitAll()) }
            }
        }
        else{
            onFailure(AppError(errorType = NetworkError.NO_NETWORK,apiId = apiId))
        }
    }

    open fun showAPIProgress(show: Boolean,apiId: Int){}

    open fun onFailure(apiError: AppError){}

}