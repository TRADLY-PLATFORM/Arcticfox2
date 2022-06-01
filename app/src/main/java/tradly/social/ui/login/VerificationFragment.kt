package tradly.social.ui.login

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import tradly.social.R
import tradly.social.common.*
import tradly.social.common.network.parseHelper.ParseConstant
import tradly.social.ui.verification.VerificationPresenter
import kotlinx.android.synthetic.main.activity_verification.*
import tradly.social.common.base.*
import tradly.social.common.util.parser.extension.toJson
import tradly.social.common.util.parser.extension.toObject
import tradly.social.domain.entities.*

/**
 * A simple [Fragment] subclass.
 */
class VerificationFragment : BaseFragment(),VerificationPresenter.View {

    var mView:View?= null
    var fragmentListener:FragmentListener?=null
    private var verificationPresenter: VerificationPresenter? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mView = inflater.inflate(R.layout.activity_verification, container, false)
        verificationPresenter = VerificationPresenter(this)
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        setListener()
    }

    private fun initView(){
        val authType = AppController.appController.getAppConfig()?.authType
        txtVerification.text = if (authType == ParseConstant.AuthType.EMAIL) getString(R.string.otp_email_verification) else getString(R.string.otp_phone_verification)
        arguments?.let {
            txtEnterOtp.text = String.format(getString(R.string.otp_otp_send_info),
                Utils.getOtpSentTo(requireContext(),it,authType))
        }
        txtResendOtp?.setOnClickListener {
            arguments?.let {
                when(it.getString("isFrom")){
                    "login"-> {
                        val country = it.getString("country").toObject<Country>()
                        val uuid = it.getString("uuid")?:Constant.EMPTY
                        val mobile = it.getString("mobile")?:Constant.EMPTY
                        country?.let { verificationPresenter?.initLogin(uuid,mobile,it) }
                    }
                    "signUp"->{
                        val uuid = it.getString("uuid")?:Constant.EMPTY
                        val firstName = it.getString("firstName")?:Constant.EMPTY
                        val lastName = it.getString("lastName")?:Constant.EMPTY
                        val email = it.getString("email")?:Constant.EMPTY
                        val password = it.getString("password")?:Constant.EMPTY
                        val mobile = it.getString("mobile")?:Constant.EMPTY
                        val country = it.getString("country").toObject<Country>()
                        verificationPresenter?.signUp(authType, uuid, firstName, lastName, email, password,password,mobile,country)
                    }
                    else -> {}
                }
            }
        }
    }


    private fun setListener(){
        edCodeOne.addTextChangedListener(OtpTextWatcher(requireContext(), edCodeOne, edCodeTwo))
        edCodeTwo.addTextChangedListener(OtpTextWatcher(requireContext(), edCodeTwo, edCodeThree))
        edCodeThree.addTextChangedListener(OtpTextWatcher(requireContext(), edCodeThree, edCodeFour))
        edCodeFour.addTextChangedListener(OtpTextWatcher(requireContext(), edCodeFour, edCodeFive))
        edCodeFive.addTextChangedListener(OtpTextWatcher(requireContext(), edCodeFive, edCodeSix))
        edCodeSix.addTextChangedListener(OtpTextWatcher(requireContext(), edCodeSix, edCodeSix))

        edCodeOne.setOnKeyListener(KeyListener(requireContext(), null, edCodeOne))
        edCodeTwo.setOnKeyListener(KeyListener(requireContext(), edCodeOne, edCodeTwo))
        edCodeThree.setOnKeyListener(KeyListener(requireContext(), edCodeTwo, edCodeThree))
        edCodeFour.setOnKeyListener(KeyListener(requireContext(), edCodeThree, edCodeFour))
        edCodeFive.setOnKeyListener(KeyListener(requireContext(), edCodeFour, edCodeFive))
        edCodeSix.setOnKeyListener(KeyListener(requireContext(), edCodeFive, edCodeSix))

        btnVerify.safeClickListener {
            val code = edCodeOne.getString().plus(edCodeTwo.getString())
                .plus(edCodeThree.getString()).plus(edCodeFour.getString())
                .plus(edCodeFive.getString()).plus(edCodeSix.getString())
            if (code.length == 6) {
                onOtpComplete(code)
            } else {
                requireContext().showToast(getString(R.string.otp_please_enter_otp))
            }
        }

        backNav.setOnClickListener {
            activity?.supportFragmentManager?.popBackStack()
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

    override fun noNetwork() {
        requireContext().showToast(R.string.no_internet)
    }

    private fun onOtpComplete(code: String) {
       arguments?.let {
           verificationPresenter?.verifyAuthentication(code,it.getString("vId"))
       }
    }

    override fun showProgressDialog(msg: Int) {
        showLoader(msg)
    }

    override fun hideProgressDialog() {
        hideLoader()
    }

    override fun onComplete(user: User) {
        activity?.let {
            val intent = Intent()
            intent.putExtra("user",user.toJson<User>())
            intent.putExtras(it.intent)
            it.setResult(Activity.RESULT_OK,intent)
            it.finish()
            AppController.appController.clearHome()
        }
    }

    override fun otpResend(verification: Verification) {
        requireContext().showToast(R.string.otp_resend_message)
        arguments?.putString("vId",verification.verifyId)
    }

    override fun onFailure(appError: AppError?) {
        hideLoader()
        ErrorHandler.handleError(requireContext(), appError)
    }

    override fun onDestroy() {
        super.onDestroy()
        verificationPresenter?.onDestroy()
    }
}
