package tradly.social.common.network.retrofit


import tradly.social.domain.entities.*
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import tradly.social.common.cache.AppCache
import tradly.social.common.network.APIEndPoints
import tradly.social.common.network.Header
import tradly.social.common.network.converters.UserModelConverter
import java.util.regex.Pattern


class RetrofitAuthenticator : Authenticator {
    val CONFIG_PATTERN = Pattern.compile("tenants/(\\d+)/configs")
    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.code == 401) {
            if(response.request.url.toString().contains(APIEndPoints.REFRESH_TOKEN)){
                AppCache.removeUser()
                return null
            }
            else if(CONFIG_PATTERN.matcher(response.request.url.toString()).matches()){
                return null
            }
            val apiService = RetrofitManager.getDefaultApiService()
            when(val result = RetrofitManager.getResponse(apiService.refreshToken())){
                is Result.Success->{
                   if(result.data.status){
                       val user = AppCache.getCacheUser()
                       val authKey = UserModelConverter().mapFrom(result.data.response.user.authKeyEntity)
                       user?.let {
                           val firebaseCustomToken = it.authKeys.firebaseToken
                           it.authKeys = authKey
                           it.authKeys.firebaseToken = firebaseCustomToken
                           AppCache.cacheUser(it)
                       }
                       val requestBuilder =  response.request.newBuilder()
                       requestBuilder.removeHeader(Header.AUTH_KEY)
                       return requestBuilder.addHeader(Header.AUTH_KEY,authKey.authKey).build()
                   }
                    else{
                       AppCache.removeUser()
                   }
                }
                is Result.Error->{
                    AppCache.removeUser()
                    return null
                }
            }
        }
        return null
    }
}