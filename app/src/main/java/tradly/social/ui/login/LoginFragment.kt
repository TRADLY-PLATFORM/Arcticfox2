package tradly.social.ui.login

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.AdapterView
import androidx.core.content.ContextCompat
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import tradly.social.R
import tradly.social.adapter.CountryAdapter
import tradly.social.common.*
import tradly.social.common.network.parseHelper.ParseConstant
import tradly.social.domain.entities.AppError
import tradly.social.domain.entities.Country
import tradly.social.domain.entities.User
import tradly.social.domain.entities.Verification
import tradly.social.common.base.BaseFragment
import kotlinx.android.synthetic.main.activity_login.*
import tradly.social.common.base.*
import tradly.social.common.util.parser.extension.toJson

class LoginFragment : BaseFragment(),LoginPresenter.View {

    var mView:View?=null
    private var fragmentListener:FragmentListener?=null
    private var loginPresenter: LoginPresenter? = null
    private var selectedCountry: Country? = null
    private val countryList = mutableListOf<Country>()
    private var countryAdapter: CountryAdapter? = null
    private val googleSignInOption by lazy { googleSignInOption() }
    private val mGoogleSignInClient by lazy { GoogleSignIn.getClient(requireContext(),googleSignInOption) }
    private val callbackManager by lazy { CallbackManager.Factory.create() }
    private val googleClientId by lazy { AppConfigHelper.getConfigKey<String>(AppConfigHelper.GOOGLE_CLIENT_ID) }
    private val facebookAppId by lazy { AppConfigHelper.getConfigKey<String>(AppConfigHelper.FACEBOOK_APP_ID) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (facebookAppId.isNotEmpty()){
            LoginManager.getInstance().registerCallback(callbackManager,facebookLoginCallback)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mView = inflater.inflate(R.layout.activity_login, container, false)
        loginPresenter = LoginPresenter(this)
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initList()
        initView()
        setListeners()
    }

    private fun setListeners(){
        btnLogin?.safeClickListener { v ->
            Utils.hideKeyBoard(requireActivity())
            login()
        }


        llSignUp.setOnClickListener {
            fragmentListener?.callNextFragment(tag = AppConstant.FragmentTag.SIGNUP_FRAGMENT)
        }

        dialCodeLayout?.setOnClickListener {
            if(countryList.count()>0){
                spinner?.performClick()
            }
            else{
                loginPresenter?.getCountries()
            }
        }

        backNav.setOnClickListener {
            activity?.finish()
        }

        btnGoogle.setOnClickListener {
            startActivityResult(mGoogleSignInClient.signInIntent, ActivityRequestCode.REQUEST_CODE_GOOGLE_SIGN_IN)
        }

        btnFacebook.setOnClickListener {
            LoginManager.getInstance().logIn(this, listOf("email","public_profile"))
        }
    }

    private fun initList() = countryList.clear()

    private fun initView() {
        when (AppController.appController.getAppConfig()?.authType) {
            ParseConstant.AuthType.EMAIL -> {
                dialCodeLayout.visibility = View.GONE
                edMobileNumber.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                edMobileNumber.setHint(R.string.login_email)
            }
            ParseConstant.AuthType.MOBILE -> {
                dialCodeLayout.visibility = View.VISIBLE
                edMobileNumber.inputType = InputType.TYPE_CLASS_PHONE
                edMobileNumber.setHint(R.string.login_mobile)
                edPassword.visibility = View.GONE
                countryAdapter = CountryAdapter(countryList, requireContext(), AppConstant.ListingType.COUNTRY_LIST)
                loginPresenter?.getCountries()
                dialCodeLayout.setOnClickListener {
                    if(countryList.isEmpty()){loginPresenter?.getCountries()} else spinner.performClick()
                }
            }
            ParseConstant.AuthType.MOBILE_PASSWORD,
            ParseConstant.AuthType.MOBILE_PASSWORD_NO_OTP-> {
                dialCodeLayout.visibility = View.VISIBLE
                edMobileNumber.inputType = InputType.TYPE_CLASS_PHONE
                edMobileNumber.setHint(R.string.login_mobile)
                edPassword.visibility = View.VISIBLE
                countryAdapter = CountryAdapter(countryList, requireContext(), AppConstant.ListingType.COUNTRY_LIST)
                loginPresenter?.getCountries()
                dialCodeLayout.setOnClickListener {
                    if(countryList.isEmpty()){loginPresenter?.getCountries()} else spinner.performClick()
                }
            }
        }

        val colorPrimary = ThemeUtil.getResourceDrawable(requireContext(), R.attr.colorPrimary)
        parentLayout.setBackgroundColor(ContextCompat.getColor(requireContext(), colorPrimary))
        txtWelcome?.text = AppConfigHelper.getConfigKey<String>(AppConfigHelper.Keys.REGISTRATION_TITLE,String.format(getString(R.string.login_welcome_to_app),getString(R.string.app_label)))
        txtForgotPassword?.safeClickListener {
            fragmentListener?.callNextFragment(Bundle(), AppConstant.FragmentTag.FORGOT_PASSWORD_FRAGMENT_ONE)
        }
        when{
            googleClientId.isNotEmpty() && facebookAppId.isNotEmpty()->{
                clSocialLogin.setVisible()
                btnFacebook.setVisible()
                btnGoogle.setVisible()
            }
            googleClientId.isNotEmpty()->{
                clSocialLogin.setVisible()
                btnFacebook.setGone()
                btnGoogle.setVisible()
            }
            facebookAppId.isNotEmpty()->{
                clSocialLogin.setVisible()
                btnFacebook.setVisible()
                btnGoogle.setGone()
            }
            else->clSocialLogin.setGone()
        }
    }


    private fun login(){
        val authType = AppController.appController.getAppConfig()?.authType
        when(authType){
            ParseConstant.AuthType.MOBILE->{
                selectedCountry?.let {
                    loginPresenter?.initLogin(ParseConstant.AuthType.MOBILE,
                        Utils.getAndroidID(),edMobileNumber.getString(),it)
                }?:run{
                    checkAndDownloadCountries()
                }
            }
            ParseConstant.AuthType.MOBILE_PASSWORD,
            ParseConstant.AuthType.MOBILE_PASSWORD_NO_OTP->{
                selectedCountry?.let {
                    loginPresenter?.login(authType,
                        Utils.getAndroidID(),edMobileNumber.getString(),edPassword.getString(),it)
                }?:run{
                    checkAndDownloadCountries()
                }
            }
            ParseConstant.AuthType.EMAIL->{
                loginPresenter?.login(ParseConstant.AuthType.EMAIL,
                    Utils.getAndroidID(),edMobileNumber.getString(),edPassword.getString())
            }
        }
    }

    private fun checkAndDownloadCountries(){
        if(countryList.isEmpty()){
            loginPresenter?.getCountries()
        }
        else{
            requireContext().showToast(R.string.login_alert_message_select_country)
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

    override fun onSuccess(user: User) {
        activity?.let {
            val intent = Intent()
            intent.putExtra("user",user.toJson<User>())
            intent.putExtras(it.intent)
            it.setResult(Activity.RESULT_OK,intent)
            it.finish()
            AppController.appController.clearHome()
        }
    }


    override fun launchVerificationPage(verification: Verification,uuid:String, mobile: String, country: Country?) {
        val bundle = Bundle()
        bundle.putBoolean("isFromLogin",true)
        bundle.putString("vId",verification.verifyId)
        bundle.putString("mobile",mobile)
        bundle.putString("country",country.toJson<Country>())
        bundle.putString("uuid",uuid)
        fragmentListener?.callNextFragment(bundle, AppConstant.FragmentTag.VERIFICATION_FRAGMENT)

    }

    override fun doSocialLogout(provider: String) {
        if (provider == AppConstant.SocialLoginProvider.FACEBOOK){
            LoginManager.getInstance().logOut()
        }
        else if (provider == AppConstant.SocialLoginProvider.GOOGLE){
            mGoogleSignInClient.signOut()
        }
    }

    override fun onFailure(appError: AppError) {
        hideProgressDialog()
        ErrorHandler.handleError(requireContext(), appError)
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

    override fun inputError(id: Int, msg: Int) {
        when (id) {
            R.id.edMobileNumber -> edMobileNumber.error = getString(msg)
            R.id.edPassword -> edPassword.error = getString(msg)
        }
    }

    override fun onLoadCountries(list: List<Country>?) {
        list?.let {
            countryList.clear()
            countryList.addAll(list)
        }
        countryAdapter?.notifyDataSetChanged()
        spinner?.adapter = countryAdapter
        spinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val country = countryAdapter?.getItem(position)
                if (country != null) {
                    selectedCountry = country as Country
                    txtDialCode?.text = String.format(getString(R.string.dialCodeConcat), country.dialCode)
                    edMobileNumber?.filters = if(country.mobileNumberLength!=0){
                        arrayOf<InputFilter>(InputFilter.LengthFilter(country.mobileNumberLength))
                    }
                    else{
                        arrayOf<InputFilter>()
                    }
                    ImageHelper.getInstance().showImage(requireContext(),selectedCountry?.flag,imageFlag,R.drawable.placeholder_image,R.drawable.placeholder_image)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }
        spinner?.setSelection(0)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ActivityRequestCode.REQUEST_CODE_GOOGLE_SIGN_IN){
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(task:Task<GoogleSignInAccount>){
        try{
            if (task.isSuccessful){
                val result = task.getResult(ApiException::class.java)
                val token = result.idToken
                if (token.isNotNullOrEmpty()){
                    loginPresenter?.socialLogin(token!!,AppConstant.SocialLoginProvider.GOOGLE,Utils.getAndroidID())
                }
            }
        }catch (e:ApiException){
            ErrorHandler.printLog(e.message)
        }
    }

    private fun googleSignInOption() = GoogleSignInOptions
            .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken(googleClientId)
            .build()

    private  val facebookLoginCallback = object :FacebookCallback<LoginResult>{
        override fun onSuccess(result: LoginResult?) {
            if (result!=null){
                loginPresenter?.socialLogin(result.accessToken.token,AppConstant.SocialLoginProvider.FACEBOOK,Utils.getAndroidID())
            }
        }

        override fun onCancel() {

        }

        override fun onError(error: FacebookException?) {
            ErrorHandler.printLog(error?.message)
        }

    }


    override fun showProgressDialog(msg: Int) {
        showLoader(msg)
    }

    override fun hideProgressDialog() {
        hideLoader()
    }

    override fun onPasswordSetSuccess(){}

    override fun onDestroy() {
        super.onDestroy()
        loginPresenter?.onDestroy()
    }
}
