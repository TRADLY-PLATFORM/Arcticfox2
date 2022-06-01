package tradly.social.common.network.retrofit

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import tradly.social.common.cache.AppCache
import tradly.social.common.cache.CurrencyCache
import tradly.social.common.network.APIEndPoints
import tradly.social.common.network.BuildConfig
import tradly.social.common.network.Header
import tradly.social.common.network.base.AppConstant
import tradly.social.common.util.common.LocaleHelper
import java.util.regex.Pattern

class RetrofitInterceptor : Interceptor{

    val CONFIG_PATTERN: Pattern = Pattern.compile("tenants/(\\d+)/configs")

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var requestBuilder = request.newBuilder()
        if(request.url.toString().startsWith(BuildConfig.BASE_URL)) {
            requestBuilder = addHeadersForAPI(requestBuilder, request.url.toString())
        }
        return chain.proceed(requestBuilder.build())
    }

    private fun addHeadersForAPI(requestBuilder: Request.Builder,url:String):Request.Builder{
        if(!CONFIG_PATTERN.matcher(url).matches()){
          requestBuilder.addHeader(
              Header.AUTHORIZATION,
              Header.BEARER.plus(getTenantKey()))
          if(url.contains(APIEndPoints.REFRESH_TOKEN)){
              requestBuilder.addHeader(Header.REFRESH_KEY,getRefreshKey())
          }
          else if(getAuthKey().isNotEmpty()){
              requestBuilder.addHeader(Header.AUTH_KEY,getAuthKey())
          }
        }

        CurrencyCache.getDefaultCurrency()?.let {
            requestBuilder.addHeader(Header.CURRENCY,it.code)
        }

        requestBuilder.addHeader(
            Header.CONTENT_TYPE,
            Header.CONTENT_TYPE_JSON
        )
        requestBuilder.addHeader(
            Header.TRADLY_USER_AGENT,
            Header.TRADLY_AGENT_VALUE
        )
        requestBuilder.addHeader(Header.LANGUAGE, LocaleHelper.getCurrentAppLanguage())
        requestBuilder.addHeader(
            Header.API_ID,
            Header.API_ID_VALUE
        )
        return requestBuilder
    }

    private fun getTenantKey() = AppCache.getCacheAppConfig()?.key?.appKey?: AppConstant.EMPTY

    private fun getAuthKey() = AppCache.getCacheUser()?.authKeys?.authKey?: AppConstant.EMPTY

    private fun getRefreshKey() =  AppCache.getCacheUser()?.authKeys?.refreshKey?: AppConstant.EMPTY
}