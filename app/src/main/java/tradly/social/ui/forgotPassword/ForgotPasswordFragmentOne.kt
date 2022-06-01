package tradly.social.ui.forgotPassword

import android.content.Context
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.AdapterView
import kotlinx.android.synthetic.main.fragment_forgot_password_one.*
import kotlinx.android.synthetic.main.fragment_forgot_password_one.backNav
import kotlinx.android.synthetic.main.fragment_forgot_password_one.dialCodeLayout
import kotlinx.android.synthetic.main.fragment_forgot_password_one.spinner

import tradly.social.R
import tradly.social.adapter.CountryAdapter
import tradly.social.common.*
import tradly.social.common.base.*
import tradly.social.common.network.parseHelper.ParseConstant
import tradly.social.common.util.parser.extension.toJson
import tradly.social.domain.entities.AppError
import tradly.social.domain.entities.Country
import tradly.social.domain.entities.User
import tradly.social.domain.entities.Verification
import tradly.social.common.base.BaseFragment
import tradly.social.ui.login.LoginPresenter


class ForgotPasswordFragmentOne : BaseFragment(), LoginPresenter.View {

    private lateinit var loginPresenter: LoginPresenter
    private var selectedCountry: Country? = null
    private val countryList = mutableListOf<Country>()
    private var countryAdapter: CountryAdapter? = null
    private var isForPasswordSet: Boolean = false
    private var fragmentListener:FragmentListener?=null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        loginPresenter = LoginPresenter(this)
        arguments?.let {
            isForPasswordSet = it.getBoolean(AppConstant.BundleProperty.IS_FOR_PASSWORD_SET, false)
        }
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        return inflater.inflate(R.layout.fragment_forgot_password_one, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val authType = AppController.appController.getAppConfig()?.authType
        if (!isForPasswordSet) {
            when (authType) {
                ParseConstant.AuthType.EMAIL -> {
                    dialCodeLayout.visibility = View.GONE
                    edInput.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                    edInput.setHint(R.string.forgotpassword_email)
                    txtEnterRecoveryInfo?.text = getString(R.string.forgotpassword_enter_your_email)
                }
                ParseConstant.AuthType.MOBILE -> {
                    dialCodeLayout.visibility = View.VISIBLE
                    edInput.inputType = InputType.TYPE_CLASS_PHONE
                    edInput.setHint(R.string.forgotpassword_mobile)
                    txtEnterRecoveryInfo?.text = getString(R.string.forgotpassword_enter_your_mobile_number)
                    countryAdapter = CountryAdapter(
                        countryList,
                        requireContext(),
                        AppConstant.ListingType.COUNTRY_LIST
                    )
                    loginPresenter.getCountries()
                    dialCodeLayout.setOnClickListener {
                        if (countryList.isEmpty()) {
                            loginPresenter.getCountries()
                        } else spinner.performClick()
                    }
                }
                ParseConstant.AuthType.MOBILE_PASSWORD,
                ParseConstant.AuthType.MOBILE_PASSWORD_NO_OTP-> {
                    dialCodeLayout.visibility = View.VISIBLE
                    edInput.inputType = InputType.TYPE_CLASS_PHONE
                    edInput.setHint(R.string.forgotpassword_mobile)
                    txtEnterRecoveryInfo?.text = getString(R.string.forgotpassword_enter_your_mobile_number)
                    countryAdapter = CountryAdapter(
                        countryList,
                        requireContext(),
                        AppConstant.ListingType.COUNTRY_LIST
                    )
                    loginPresenter.getCountries()
                    dialCodeLayout.setOnClickListener {
                        if (countryList.isEmpty()) {
                            loginPresenter.getCountries()
                        } else spinner.performClick()
                    }
                }
            }
        } else {
            dialCodeLayout.visibility = View.GONE
            edInput.inputType = InputType.TYPE_CLASS_NUMBER
            edInput.setHint(R.string.forgotpassword_verification_password)
            frPasswordTwo.visibility = View.VISIBLE
            frPasswordOne.visibility = View.VISIBLE
            txtEnterRecoveryInfo?.visibility = View.VISIBLE
            arguments?.let {
                txtEnterRecoveryInfo?.text =  String.format(getString(R.string.forgotpassword_otp_send_info),
                    Utils.getOtpSentTo(requireContext(),it,authType))
            }
        }
        btnVerify?.safeClickListener {
            if (NetworkUtil.isConnectingToInternet(true)) {
                if (!isForPasswordSet) {
                    when (AppController.appController.getAppConfig()?.authType) {
                        ParseConstant.AuthType.EMAIL -> {
                            loginPresenter.recoverPassword(edInput.getString())
                        }
                        ParseConstant.AuthType.MOBILE_PASSWORD,
                        ParseConstant.AuthType.MOBILE_PASSWORD_NO_OTP,
                        ParseConstant.AuthType.MOBILE->{
                            selectedCountry?.let {
                                loginPresenter.recoverPassword(it,edInput.getString())
                            }?:run{
                                checkAndDownloadCountries()
                            }
                        }
                    }
                }
                else{
                    loginPresenter.resetPassword(arguments!!.getString(AppConstant.BundleProperty.VERIFY_ID)!!,edInput.getStringWithID(),edPasswordOne.getStringWithID(),edPasswordTwo.getStringWithID())
                }
            }
        }

        dialCodeLayout?.setOnClickListener {
            if(countryList.count()>0){
                spinner?.performClick()
            }
            else{
                loginPresenter.getCountries()
            }
        }

        backNav.setOnClickListener { activity?.onBackPressed()}
    }

    override fun onSuccess(user: User) {

    }

    override fun launchVerificationPage(
        verification: Verification,
        uuid: String,
        mobile: String,
        country: Country?
    ) {
        val bundle = Bundle().apply {
            putString(AppConstant.BundleProperty.VERIFY_ID,verification.verifyId)
            putString("email",edInput.getString())
            putString("mobile",mobile)
            putString("country",country.toJson<Country>())
            putBoolean(AppConstant.BundleProperty.IS_FOR_PASSWORD_SET,true)
        }
        fragmentListener?.callNextFragment(bundle, AppConstant.FragmentTag.FORGOT_PASSWORD_FRAGMENT_TWO)
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

    override fun onFailure(appError: AppError) {
        ErrorHandler.handleError(exception = appError)
    }

    override fun noInternet() {

    }

    override fun inputError(id: Int, msg: Int) {
        when (id) {
            R.id.edInput -> edInput?.error = getString(msg)
            R.id.edPasswordOne->edPasswordOne?.error = getString(msg)
            R.id.edPasswordTwo->edPasswordTwo?.error = getString(msg)
        }
    }

    override fun onLoadCountries(list: List<Country>?) {
        list?.let { countryList.addAll(list) }
        countryAdapter?.notifyDataSetChanged()
        spinner?.adapter = countryAdapter
        spinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val country = countryAdapter?.getItem(position)
                if (country != null) {
                    selectedCountry = country as Country
                    txtDialCode?.text = String.format(getString(R.string.dialCodeConcat), country.dialCode)
                    edInput?.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(country.mobileNumberLength))
                    ImageHelper.getInstance().showImage(requireContext(),selectedCountry?.flag,imageFlag,R.drawable.placeholder_image,R.drawable.placeholder_image)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }
        spinner?.setSelection(0)
    }

    override fun showProgressDialog(msg: Int) {
        showLoader(msg)
    }

    override fun hideProgressDialog() {
        hideLoader()
    }

    private fun checkAndDownloadCountries(){
        if(countryList.isEmpty()){
            loginPresenter.getCountries()
        }
        else{
            requireContext().showToast(R.string.forgotpassword_alert_message_select_country)
        }
    }

    override fun onPasswordSetSuccess() {
        Utils.showAlertDialog(requireContext(),
            AppConstant.EMPTY,getString(R.string.otp_password_changed_success_msg),false,false,object:
                Utils.DialogInterface{
            override fun onAccept() {
                fragmentListener?.popFragment(AppConstant.FragmentTag.FORGOT_PASSWORD_FRAGMENT_ONE)
                fragmentListener?.popFragment(AppConstant.FragmentTag.FORGOT_PASSWORD_FRAGMENT_TWO)
            }

            override fun onReject() {

            }
        })
    }

    override fun doSocialLogout(provider: String) {

    }

    override fun onDestroy() {
        super.onDestroy()
        loginPresenter.onDestroy()
    }
}
