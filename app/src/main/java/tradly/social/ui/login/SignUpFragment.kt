package tradly.social.ui.login

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.AdapterView
import androidx.core.content.ContextCompat
import tradly.social.R
import tradly.social.adapter.CountryAdapter
import tradly.social.common.*
import tradly.social.common.network.parseHelper.ParseConstant
import tradly.social.domain.entities.AppError
import tradly.social.domain.entities.Country
import tradly.social.domain.entities.Verification
import tradly.social.common.base.BaseFragment
import tradly.social.ui.account.AccountPresenter
import kotlinx.android.synthetic.main.activity_create_account.*
import tradly.social.common.base.*
import tradly.social.common.util.parser.extension.toJson
import tradly.social.domain.entities.User


class SignUpFragment : BaseFragment(),AccountPresenter.AccountInteractor, CustomOnClickListener.OnCustomClickListener {

    var mView:View?=null
    var fragmentListener: FragmentListener?=null
    private var accountPresenter: AccountPresenter? = null
    private var countryAdapter: CountryAdapter? = null
    private var selectedCountry: Country?=null
    private val countryList = mutableListOf<Country>()
    private var authType:Int? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mView = inflater.inflate(R.layout.activity_create_account, container, false)
        accountPresenter = AccountPresenter(this)
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        authType = AppController.appController.getAppConfig()?.authType
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        when(authType){
            ParseConstant.AuthType.EMAIL->{
                flEmail?.visibility = View.VISIBLE
                rlMobileNumber?.visibility = View.GONE
                flPassword?.visibility = View.VISIBLE
                flConfirmPassword?.visibility = View.VISIBLE
            }
            ParseConstant.AuthType.MOBILE_PASSWORD,
            ParseConstant.AuthType.MOBILE_PASSWORD_NO_OTP->{
                rlMobileNumber?.visibility = View.VISIBLE
                flEmail?.visibility = View.GONE
                flPassword?.visibility = View.VISIBLE
                flConfirmPassword?.visibility = View.VISIBLE
                countryAdapter = CountryAdapter(countryList, requireContext(), AppConstant.ListingType.COUNTRY_LIST)
                accountPresenter?.getCountries()
                dialCodeLayout.setOnClickListener {
                    if(countryList.isEmpty()){accountPresenter?.getCountries()} else spinner.performClick()
                }
            }
            ParseConstant.AuthType.MOBILE->{
                rlMobileNumber?.visibility = View.VISIBLE
                flPassword?.visibility = View.GONE
                flConfirmPassword?.visibility = View.GONE
                countryAdapter = CountryAdapter(countryList, requireContext(), AppConstant.ListingType.COUNTRY_LIST)
                accountPresenter?.getCountries()
                dialCodeLayout.setOnClickListener {
                    if(countryList.isEmpty()){accountPresenter?.getCountries()} else spinner.performClick()
                }
            }
        }

        with(CustomOnClickListener(this)){
            btnCreate?.setOnClickListener(this)
            llSignIn?.setOnClickListener(this)
            backNav?.setOnClickListener(this)
            txtTermsCondition?.setOnClickListener(this)
        }

        val colorPrimary = ThemeUtil.getResourceDrawable(requireContext(), R.attr.colorPrimary)
        parentLayout.setBackgroundColor(ContextCompat.getColor(requireContext(), colorPrimary))

        txtWelcome?.text = AppConfigHelper.getConfigKey<String>(AppConfigHelper.Keys.REGISTRATION_TITLE,String.format(getString(R.string.signup_welcome_to_message),getString(R.string.app_label)))
    }

    override fun onCustomClick(view: View) {
        when(view.id){
          R.id.btnCreate->signUp()
          R.id.llSignIn, R.id.backNav->activity?.supportFragmentManager?.popBackStack()
          R.id.txtTermsCondition-> Utils.openUrlInBrowser(
              AppConfigHelper.getConfigKey(
                  AppConfigHelper.Keys.TERMS_URL,tradly.social.common.resources.BuildConfig.TERMS_URL))
        }
    }

    private fun signUp() {
        when (authType) {
            ParseConstant.AuthType.EMAIL -> {
                accountPresenter?.signUp(
                    ParseConstant.AuthType.EMAIL,
                    Utils.getAndroidID(),
                    edFirstName.text.toString().trim(),
                    edLastName.text.toString().trim(),
                    edEmail.text.toString().trim(),
                    edPassword.text.toString().trim(),
                    edConfirmPassword.text.toString().trim(),
                    isTermsAgreed = termsCheckBox.isChecked
                )
            }
            ParseConstant.AuthType.MOBILE_PASSWORD,
            ParseConstant.AuthType.MOBILE_PASSWORD_NO_OTP-> {
                selectedCountry?.let {
                    accountPresenter?.signUp(
                        authType,
                        Utils.getAndroidID(),
                        edFirstName.text.toString().trim(),
                        edLastName.text.toString().trim(),
                        mobileNumber = edMobileNumber.text.toString().trim(),
                        password = edPassword.text.toString().trim(),
                        confirmPassword = edConfirmPassword.text.toString().trim(),
                        country = selectedCountry,
                        isTermsAgreed = termsCheckBox.isChecked
                    )
                }?:run{
                    checkAndDownloadCountries()
                }
            }
            ParseConstant.AuthType.MOBILE -> {
                selectedCountry?.let {
                    accountPresenter?.signUp(
                        ParseConstant.AuthType.MOBILE,
                        Utils.getAndroidID(),
                        edFirstName.text.toString().trim(),
                        edLastName.text.toString().trim(),
                        mobileNumber = edMobileNumber.text.toString().trim(),
                        country = selectedCountry,
                        isTermsAgreed = termsCheckBox.isChecked
                    )
                }?:run{
                    checkAndDownloadCountries()
                }
            }
        }
    }

    override fun onLoadCountries(list: List<Country>?) {
        list?.let {
            countryList.clear()
            countryList.addAll(list)
            countryAdapter?.notifyDataSetChanged()
            spinner?.adapter = countryAdapter
            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View,
                    position: Int,
                    id: Long
                ) {
                    val country = countryAdapter?.getItem(position)
                    if (country != null) {
                        selectedCountry = country as Country
                        txtDialCode?.text =
                            String.format(getString(R.string.dialCodeConcat), country.dialCode)
                        ImageHelper.getInstance().showImage(
                            requireContext(),
                            country.flag,
                            imgFlag,
                            R.drawable.placeholder_image,
                            R.drawable.placeholder_image
                        )
                        edMobileNumber?.filters = if(country.mobileNumberLength!=0){
                                arrayOf<InputFilter>(InputFilter.LengthFilter(country.mobileNumberLength))
                            }
                            else{
                                arrayOf<InputFilter>()
                            }
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>) {

                }
            }
        }
    }

    private fun checkAndDownloadCountries(){
        if(countryList.isEmpty()){
            accountPresenter?.getCountries()
        }
        else{
            requireContext().showToast(R.string.signup_alert_message_select_country)
        }
    }

    override fun onSuccess(verification: Verification,uuid:String, email: String, mobile: String, password:String,country: Country?) {
        val bundle = Bundle()
        bundle.putString("isFrom","signUp")
        bundle.putString("uuid",uuid)
        bundle.putString("vId",verification.verifyId)
        bundle.putString("firstName",edFirstName.getString())
        bundle.putString("lastName",edLastName.getString())
        bundle.putString("email",email)
        bundle.putString("password",password)
        bundle.putString("country",country?.toJson<Country>())
        bundle.putString("mobile",mobile)
        fragmentListener?.callNextFragment(bundle, AppConstant.FragmentTag.VERIFICATION_FRAGMENT)
    }

    override fun onFailure(appError: AppError?) {
        ErrorHandler.handleError(requireContext(), appError)
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

    override fun showProgressDialog(msg: Int) {
        showLoader(msg)
    }

    override fun hideProgressDialog() {
        hideLoader()
    }


    override fun inputError(id: Int, msg: Int) {
        when (id) {
            R.id.edFirstName -> edFirstName.error = getString(msg)
            R.id.edLastName -> edLastName.error = getString(msg)
            R.id.edMobileNumber -> edMobileNumber.error = getString(msg)
            R.id.edEmail -> edEmail.error = getString(msg)
            R.id.edPassword -> edPassword.error = getString(msg)
            R.id.edConfirmPassword -> edConfirmPassword.error = getString(msg)
            R.id.termsCheckBox -> requireContext().showToast(getString(msg))
        }
    }

    override fun noInternet() {
        Utils.showAlertDialog(
            requireContext(),
            getString(R.string.no_internet),
            getString(R.string.no_internet_warning_message),
            false,
            false, null
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        accountPresenter?.onDestroy()
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
}
