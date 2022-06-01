package tradly.social.common.network.base

import android.os.Looper
import retrofit2.Call
import tradly.social.common.network.retrofit.RetrofitManager
import tradly.social.domain.entities.Result

abstract class BaseService {

    protected fun<T> getRetrofitService(cls:Class<T>) = RetrofitManager.getApiService(cls)

    protected fun getDefaultApiService() = RetrofitManager.getDefaultApiService()

    protected fun <T:Any> apiCall(call: Call<T>) = RetrofitManager.getResponse(call)

    protected fun <T:Any> getImageUploadResponse(call: Call<T>) = RetrofitManager.getImageUploadResponse(call)

    protected fun <T : Any> apiCall(call: Call<T>, executeSync:Boolean = false):Result<T> {
        if(isOnBackgroundThread() || executeSync){
            return RetrofitManager.getResponse(call)
        }
        else{
            throw IllegalArgumentException("You must call this method on a background thread")
        }
    }

    private fun isOnBackgroundThread() = Looper.myLooper() != Looper.getMainLooper()
}