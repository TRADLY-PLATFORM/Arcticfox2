package tradly.social.ui.payment.configurePayout

import android.content.Context
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.ForegroundColorSpan
import android.text.util.Linkify
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.fragment_configure_init_payout.*

import tradly.social.R
import tradly.social.common.*
import tradly.social.common.base.*
import tradly.social.domain.entities.AppError

import tradly.social.common.base.BaseFragment

/**
 * A simple [Fragment] subclass.
 */
class ConfigurePayoutInitFragment : BaseFragment(),PayoutPresenter.View {

    private var fragmentListener:FragmentListener? = null
    private lateinit var payoutPresenter: PayoutPresenter
    private lateinit var stripeAccountType:String
    private lateinit var accountId:String
    private var isStripeOnBoardingConnected:Boolean = false
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        payoutPresenter = PayoutPresenter(this)
        stripeAccountType = AppConfigHelper.getConfigKey(AppConfigHelper.Keys.STRIPE_CONNECT_ACCOUNT_TYPE)
        accountId = arguments?.getString(AppConstant.BundleProperty.ACCOUNT_ID)!!
        return inflater.inflate(R.layout.fragment_configure_init_payout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        connectInfo?.movementMethod = LinkMovementMethod.getInstance()
        connectInfo?.linksClickable = true
        connectInfo?.autoLinkMask = Linkify.WEB_URLS
        btnConnect?.safeClickListener {
            if(stripeAccountType== AppConstant.StripeConnectAccountType.STANDARD || !isStripeOnBoardingConnected){
                payoutPresenter.connectOAuth(stripeAccountType,accountId)
            }
            else{
                payoutPresenter.getStripeLink(accountId)
            }
        }

        val isFromBrowserResult = arguments!!.getBoolean(AppConstant.BundleProperty.IS_FROM_BROWSER_RESULT)
        if(!isFromBrowserResult){
            parentLayout?.visibility = View.VISIBLE
        }
        if(AppConstant.StripeConnectAccountType.STANDARD == stripeAccountType){
            if (NetworkUtil.isConnectingToInternet(true) && isFromBrowserResult) {
                payoutPresenter.fetchUserDetail(AppController.appController.getUser()!!.id)
            }
            else {
                parentLayout?.visibility = View.VISIBLE
                btnConnectTxt.text = getString(R.string.payments_connect_with_stripe)
                connectInfo.text = AppConfigHelper.getConfigKey<String>(AppConfigHelper.Keys.STRIPE_CONFIG_CONNECT_MESSAGE_STANDARD)
            }
        }
        else{
            fetchStripeStatus()
        }
    }

    fun fetchStripeStatus(){
        parentLayout?.visibility = View.GONE
        payoutPresenter.getStripeStatus(accountId)
    }

    private fun showSuccessState(){
        if(AppController.appController.isStripeConnected()){
            fragmentListener?.callNextFragment(tag = AppConstant.FragmentTag.CONFIGURE_PAYOUT_FRAGMENT_SUCCESS)
        }
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

    override fun onSuccessAccountLink(oAuthUrl: String) {
        Utils.openUrlInBrowser(oAuthUrl)
    }

    override fun onVerifySuccess(success: Boolean) {
        showSuccessState()
    }

    override fun onDisconnectSuccess() {

    }

    override fun onError(appError: AppError) {
        ErrorHandler.handleError(exception = appError)
    }

    override fun showProgressLoader(msg: Int) {
        showLoader(msg)
    }

    override fun hideProgressLoader() {
        hideLoader()
    }

    override fun showProgressBar() {
        progress?.visibility = View.VISIBLE
    }

    override fun hideProgressBar() {
        progress?.visibility = View.GONE
    }
    override fun onStripeStatus(
        isStripeOnBoardingConnected: Boolean,
        isPayoutEnabled: Boolean,
        errors: List<String>
    ) {
       this.isStripeOnBoardingConnected = isStripeOnBoardingConnected
       parentLayout?.visibility = View.VISIBLE
       setConnectInfo(isStripeOnBoardingConnected,isPayoutEnabled,errors)
    }

    private fun setConnectInfo(isStripeOnBoardingConnected: Boolean, isPayoutEnabled: Boolean, errors: List<String>){

        btnConnectTxt?.text = if(isStripeOnBoardingConnected){
            getString(R.string.payments_view_dashboard)
        }
        else{
            getString(R.string.payments_connect_with_stripe)
        }

        if(errors.isEmpty()){
            if(!isStripeOnBoardingConnected){
                stripeStatusImage?.setImageResource(R.drawable.ic_stripe)
                connectInfo?.text = getString(R.string.payments_stripe_express_connect_message)
                stripeStatusMessage?.text = getString(R.string.payments_stripe_status_connect)
            }
            else if(isStripeOnBoardingConnected && !isPayoutEnabled){
                stripeStatusImage?.setImageResource(R.drawable.ic_stripe_waiting)
                connectInfo?.text = getString(R.string.payments_stripe_express_connect_waiting)
                stripeStatusMessage?.text = getString(R.string.payments_stripe_status_waiting_message)

            }
            else{
                stripeStatusImage?.setImageResource(R.drawable.ic_done)
                connectInfo?.text = getString(R.string.payments_stripe_express_connect_success)
                stripeStatusMessage?.text = getString(R.string.payments_stripe_status_connection_success)
            }
        }
        else{
            val formattedList = errors.joinToString(",")
            stripeStatusImage?.setImageResource(R.drawable.ic_stripe_failed)
            val txt1 = getString(R.string.payments_stripe_express_connect_failed1)
            val txt2 = getString(R.string.payments_stripe_express_connect_failed2)
            val errorMessage = txt1.plus(formattedList).plus(txt2)
            val spannableString = SpannableString(errorMessage)
            spannableString.setSpan(ForegroundColorSpan(ContextCompat.getColor(AppController.appContext, R.color.colorRed)), txt1.length, txt1.length +formattedList.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            connectInfo?.text = spannableString
            stripeStatusMessage?.text = getString(R.string.payments_stripe_status_verification_failed)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        payoutPresenter.onDestroy()
    }
}
