package tradly.social.ui.payment.configurePayout

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_payout_success.*
import tradly.social.R
import tradly.social.common.*
import tradly.social.common.base.*
import tradly.social.domain.entities.AppError

/**
 * A simple [Fragment] subclass.
 */
class PayoutSuccessFragment : BaseFragment(),PayoutPresenter.View {

    private lateinit var payoutPresenter:PayoutPresenter
    private var fragmentListener:FragmentListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        payoutPresenter = PayoutPresenter(this)
        return inflater.inflate(R.layout.fragment_payout_success, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btnDisConnect?.safeClickListener {
            Utils.showAlertDialog(requireContext(),getString(R.string.payments_disconnect_stripe),getString(R.string.payments_disconnect_stripe_warning),true,true,object :
                Utils.DialogInterface{
                override fun onAccept() {
                    payoutPresenter.disconnectOAuth()
                }

                override fun onReject() {

                }
            })
        }

        val stripeAccountType = AppConfigHelper.getConfigKey<String>(AppConfigHelper.Keys.STRIPE_CONNECT_ACCOUNT_TYPE)
         if(AppConstant.StripeConnectAccountType.EXPRESS == stripeAccountType){
             btnDisConnect?.visibility = View.GONE
             disconnectInfo?.text = AppConfigHelper.getConfigKey<String>(AppConfigHelper.Keys.STRIPE_CONFIG_DISCONNECT_MESSAGE_EXPRESS)
        }
        else{
             btnDisConnect?.visibility = View.VISIBLE
             disconnectInfo?.text = AppConfigHelper.getConfigKey<String>(AppConfigHelper.Keys.STRIPE_CONFIG_DISCONNECT_MESSAGE_STANDARD)
        }
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
    }

    override fun onDisconnectSuccess() {
        activity?.let {
            it.showToast(R.string.payments_stripe_disconnected_success)
            (it as PayoutConfigureActivity).finishActivityWithResult()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if(requireContext() is FragmentListener){
            fragmentListener = requireContext() as? FragmentListener
        }
    }

    override fun onStripeStatus(
        isStripeOnBoardingConnected: Boolean,
        isPayoutEnabled: Boolean,
        errors: List<String>
    ) {

    }

    override fun showProgressBar() {

    }

    override fun hideProgressBar() {

    }
    override fun onDetach() {
        super.onDetach()
        fragmentListener = null
    }

}
