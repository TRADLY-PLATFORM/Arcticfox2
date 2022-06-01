package tradly.social.common.base

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo

class NetworkUtil {
    companion object {
        fun isConnectingToInternet(showNoNetworkMsg:Boolean = false): Boolean {
            val cm = AppController.appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
            val isConnected: Boolean = activeNetwork?.isConnectedOrConnecting == true
            if(showNoNetworkMsg && !isConnected){
                ActivityHelper.getCurrentActivityInstance()?.let {
                    it.showToast(it.getString(R.string.no_internet))
                }
            }
            return isConnected
        }
    }
}