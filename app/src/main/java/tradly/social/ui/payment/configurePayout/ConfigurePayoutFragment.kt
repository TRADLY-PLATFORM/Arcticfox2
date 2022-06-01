package tradly.social.ui.payment.configurePayout

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.fragment_configure_payout.*
import tradly.social.BuildConfig
import tradly.social.R
import tradly.social.common.*
import tradly.social.common.base.AppConfigHelper
import tradly.social.common.base.AppConstant
import tradly.social.common.base.AppController
import tradly.social.common.base.ErrorHandler
import tradly.social.domain.entities.AppError
import tradly.social.common.base.BaseFragment

/**
 * A simple [Fragment] subclass.
 */
class ConfigurePayoutFragment : BaseFragment(),PayoutPresenter.View {

    private var fragmentListener:FragmentListener? = null
    lateinit var payoutPresenter: PayoutPresenter
    lateinit var stripeAccountType:String
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        payoutPresenter = PayoutPresenter(this)
        stripeAccountType = AppConfigHelper.getConfigKey(AppConfigHelper.Keys.STRIPE_CONNECT_ACCOUNT_TYPE)
        return inflater.inflate(R.layout.fragment_configure_payout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setProgressBarTheme()
        initWebView(arguments?.getString(AppConstant.BundleProperty.OAUTH_URL))
    }

    private fun initWebView(url:String?){
        webview.settings.setAppCacheEnabled(false)
        webview.settings.setSupportZoom(true)
        webview.settings.builtInZoomControls = true
        webview.settings.javaScriptEnabled = true
        webview.settings.allowContentAccess = false
        webview.settings.allowFileAccess = false
        webview.settings.cacheMode = WebSettings.LOAD_NO_CACHE
        webview.webChromeClient = CustomWebViewClient(progress)
        webview.webViewClient = CustomClient(stripeAccountType,payoutPresenter){

        }
        webview.loadUrl(url!!)
    }

    fun canGoBack() = webview.canGoBack()

    fun goBack() = webview.goBack()

    class CustomClient(var stripeAccountType:String,var payoutPresenter: PayoutPresenter,val onSuccess:(success:Boolean)->Unit):WebViewClient(){
        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        override fun shouldInterceptRequest(
            view: WebView?,
            request: WebResourceRequest?
        ): WebResourceResponse? {
            return super.shouldInterceptRequest(view, request)
        }

        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            request?.url?.toString()?.let { url->
                if(AppConstant.StripeConnectAccountType.STANDARD == stripeAccountType){
                    if(url.contains(BuildConfig.BASE_URL.plus("v1/payments/stripe/connect/oauth/verify"))){
                        payoutPresenter.verifyOAuth(url)
                        return true
                    }
                }
                else if(AppConstant.StripeConnectAccountType.EXPRESS == stripeAccountType){
                    if(url.contains(BuildConfig.BASE_URL.plus("v1/payments/stripe/connect/account_links/return"))){
                        val user = AppController.appController.getUser()
                        payoutPresenter.fetchUserDetail(user!!.id)
                    }
                }
            }
            return super.shouldOverrideUrlLoading(view, request)
        }

        override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
            super.onReceivedError(view, request, error)
        }

        override fun onReceivedError(
            view: WebView?,
            errorCode: Int,
            description: String?,
            failingUrl: String?
        ) {
            super.onReceivedError(view, errorCode, description, failingUrl)
        }
        override fun onReceivedHttpError(
            view: WebView?,
            request: WebResourceRequest?,
            errorResponse: WebResourceResponse?
        ) {
            super.onReceivedHttpError(view, request, errorResponse)
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
        }
    }
    private fun setProgressBarTheme(){
        val progressDrawable: Drawable = progress.getProgressDrawable().mutate()
        progressDrawable.setColorFilter(ContextCompat.getColor(requireContext(),R.color.colorBlueLight), PorterDuff.Mode.SRC_IN)
        progress.progressDrawable = progressDrawable
        progress.visibility = View.VISIBLE
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if(requireContext() is FragmentListener){
            fragmentListener = requireContext() as? FragmentListener
        }
    }

    override fun onDetach() {
        super.onDetach()
        fragmentListener = null
    }

    override fun showProgressLoader(msg: Int) {
       showLoader(msg)
    }

    override fun hideProgressLoader() {
        hideLoader()
    }

    override fun onSuccessAccountLink(oAuthUrl: String) {

    }

    override fun onError(appError: AppError) {
      ErrorHandler.handleError(exception = appError)
    }

    override fun onVerifySuccess(success: Boolean) {
        fragmentListener?.popFragment(AppConstant.FragmentTag.CONFIGURE_PAYOUT_FRAGMENT)
        fragmentListener?.callNextFragment(tag = AppConstant.FragmentTag.CONFIGURE_PAYOUT_FRAGMENT_SUCCESS)
    }

    override fun onDisconnectSuccess() {

    }

    override fun showProgressBar() {

    }

    override fun hideProgressBar() {

    }
    override fun onStripeStatus(
        isStripeOnBoardingConnected: Boolean,
        isPayoutEnabled: Boolean,
        errors: List<String>
    ) {

    }
}
