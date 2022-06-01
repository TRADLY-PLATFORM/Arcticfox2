package tradly.social.ui.settings

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.yalantis.ucrop.UCrop
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.android.synthetic.main.toolbar.*
import tradly.social.BuildConfig
import tradly.social.R
import tradly.social.adapter.CountryAdapter
import tradly.social.adapter.ListingAdapter
import tradly.social.common.*
import tradly.social.common.base.*
import tradly.social.common.uiEntity.Theme
import tradly.social.data.model.FireBaseAuthHelper
import tradly.social.common.network.parseHelper.ParseConstant
import tradly.social.common.persistance.shared.PreferenceSecurity
import tradly.social.common.resources.ResourceConfig
import tradly.social.data.model.payments.stripe.StripeHelper
import tradly.social.common.util.parser.extension.toJson
import tradly.social.domain.entities.AppError
import tradly.social.domain.entities.Constant
import tradly.social.domain.entities.Store
import tradly.social.domain.entities.User
import tradly.social.common.util.common.LocaleHelper
import tradly.social.ui.login.AuthenticationActivity
import tradly.social.ui.main.MainActivity
import tradly.social.ui.sellerProfile.EditProfileActivity
import tradly.social.ui.store.addStore.AddStoreDetailActivity
import tradly.social.ui.store.storeDetail.StoreDetailActivity
import java.io.File

class SettingsActivity : BaseActivity(), CustomOnClickListener.OnCustomClickListener,SettingsPresenter.View {

    val themeList: List<Theme> = ThemeUtil.getThemeList()
    var themeAdapter: ListingAdapter? = null
    var selectedImagePath:String?=null
    var mStoreDetail:Store?=null
    private val googleSignInOption by lazy { googleSignInOption() }
    private val mGoogleSignInClient by lazy { GoogleSignIn.getClient(this,googleSignInOption) }
    lateinit var settingsPresenter: SettingsPresenter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        setToolbar(toolbar, R.string.settings_header_title, R.drawable.ic_back)
        settingsPresenter = SettingsPresenter(this)
        themeRecycler.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        themeAdapter = ListingAdapter(this, themeList, AppConstant.ListingType.THEME_LIST, themeRecycler) { position, obj ->
                themeList.forEach { it.isSelected = false }
                themeList[position].isSelected = true
                themeAdapter?.notifyDataSetChanged()
                ThemeUtil.applyTheme(this, themeList[position].id)
                tradly.social.common.persistance.shared.PreferenceSecurity.putBoolean(AppConstant.PREF_SETTINGS_CHANGED, true)
            }
        themeRecycler?.adapter = themeAdapter
        toolbar?.setNavigationOnClickListener {
            onBackPressed()
        }
        with(CustomOnClickListener(this)){
            iconEditEmailOrMobile?.setOnClickListener(this)
            iconEditFullName?.setOnClickListener(this)
            iconEditStore.setOnClickListener(this)
            rlProfile?.setOnClickListener(this)
            storeLayout.setOnClickListener(this)
            logoutLayout.setOnClickListener(this)
            languageLayout.setOnClickListener(this)
            tenantLayout.setOnClickListener(this)
            terms_condition.setOnClickListener(this)
            privacy_policy.setOnClickListener(this)
        }
        setProfile()
        setStoreDetails()
        setSettingDetails()
    }

    private fun googleSignInOption() = GoogleSignInOptions
        .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestEmail()
        .requestIdToken("379071078531-behpcpksblalb8cn6np5166ehj0n3622.apps.googleusercontent.com")
        .build()

    override fun onCustomClick(view: View) {
        val intent = Intent(this,EditProfileActivity::class.java)
        val authType = AppController.appController.getAppConfig()?.authType
        val isEmailAuth = ParseConstant.AuthType.EMAIL == authType
        when(view.id){
            R.id.iconEditEmailOrMobile-> {
                intent.putExtra("isFor",if(isEmailAuth)"email" else "mobile")
                startActivityForResult(intent, AppConstant.ActivityResultCode.UPDATE_USER_DETAIL_RESULT)
            }
            R.id.iconEditFullName -> {
                intent.putExtra("isFor","name")
                startActivityForResult(intent, AppConstant.ActivityResultCode.UPDATE_USER_DETAIL_RESULT)
            }
            R.id.iconEditStore->{
                mStoreDetail?.let {
                    val bundle = Bundle().apply {
                        putBoolean(AppConstant.BundleProperty.IS_EDIT,true)
                        putString(AppConstant.BundleProperty.IS_FROM, AppConstant.BundleProperty.MY_STORE)
                        putString(AppConstant.BundleProperty.STORE , it.toJson<Store>())
                    }
                    startActivityForResult(AddStoreDetailActivity::class.java,
                        AppConstant.ActivityResultCode.EDIT_STORE,bundle)

                }?:run{
                    showAddAccountActivity()
                }
            }
            R.id.storeLayout->{
                if(mStoreDetail != null){
                    val storeIntent = Intent(this,StoreDetailActivity::class.java)
                    storeIntent.putExtra("id",mStoreDetail?.id)
                    startActivity(storeIntent)
                }
            }
            R.id.rlProfile,
            R.id.txtName,
            R.id.txtEmailOrMobile->{
                if(AppController.appController.getUser() == null){
                    startActivityForResult(AuthenticationActivity::class.java,
                        AppConstant.ActivityResultCode.LOGIN_FROM_SETTINGS, Bundle().apply { putString("isFor",
                            AppConstant.LoginFor.SETTINGS) })
                    return
                }
                if(R.id.rlProfile == view.id){
                    showMediaChooser()
                }
            }
            R.id.terms_condition-> Utils.openUrlInBrowser(
                AppConfigHelper.getConfigKey(
                    AppConfigHelper.Keys.TERMS_URL,ResourceConfig.TERMS_URL))
            R.id.privacy_policy-> Utils.openUrlInBrowser(
                AppConfigHelper.getConfigKey(
                    AppConfigHelper.Keys.PRIVACY_URL,ResourceConfig.PRIVACY_URL))
            R.id.languageLayout-> showLocaleDialog(this){
                tradly.social.common.persistance.shared.PreferenceSecurity.putBoolean(AppConstant.PREF_SETTINGS_CHANGED, true)
                refreshActivity(this)
            }
            R.id.logoutLayout-> {
                Utils.showAlertDialog(this,getString(R.string.settings_logout),getString(R.string.settings_are_you_sure_logout), true, true,object : Utils.DialogInterface {
                    override fun onAccept() {
                        settingsPresenter.logout()
                    }

                    override fun onReject() {
                    }
                })
            }
        }
    }


    private fun setProfile() {
        val user = AppController.appController.getUser()
        if(user != null){
            txtName?.text = user.name
            txtFullName?.text = user.name
            txtName?.typeface = Typeface.DEFAULT
            iconEditProfile.visibility = View.VISIBLE
            userInfoLayout.visibility = View.VISIBLE
            when(AppController.appController.getAppConfig()?.authType){
                ParseConstant.AuthType.EMAIL->{
                    txtEmailOrMobile?.text = user.email
                    txtEmailOrMobile_?.text = user.email
                    txtHintEmailOrMobile?.text = getString(R.string.settings_email)
                    EmailOrMobileIcon?.setImageResource(R.drawable.ic_email_black_24dp)
                }
                else -> {
                    txtEmailOrMobile_?.text = user.mobile
                    txtEmailOrMobile?.text = user.mobile
                    txtHintEmailOrMobile?.text = getString(R.string.editprofile_mobile)
                    EmailOrMobileIcon?.setImageResource(R.drawable.ic_call_black_24dp)
                }
            }
        }
        else{
            userInfoLayout.visibility = View.GONE
            iconEditProfile.visibility = View.GONE
            txtName?.setText(R.string.settings_welcome)
            txtEmailOrMobile?.setText(R.string.settings_signin_signup)
            txtName?.typeface = Typeface.DEFAULT_BOLD
        }

        ImageHelper.getInstance().showImage(this,
            user?.profilePic,
            profileImg,
            R.drawable.ic_user_placeholder,
            R.drawable.ic_user_placeholder
        )
    }

    private fun finishActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun showAddAccountActivity(){
        if(getLoggedInUser()!=null){
            val bundle = Bundle().apply { putString("isFrom" , "settings") }
            startActivityForResult(AddStoreDetailActivity::class.java,
                AppConstant.ActivityResultCode.ADD_STORE_RESULT_CODE,bundle)
        }
        else{
            startActivityForResult(AuthenticationActivity::class.java,
                AppConstant.ActivityResultCode.LOGIN_FROM_SETTINGS, Bundle().apply { putString("isFor",
                    AppConstant.LoginFor.ADD_ACCOUNT) })
        }
    }

    override fun onBackPressed() {
        if (tradly.social.common.persistance.shared.PreferenceSecurity.getBoolean(AppConstant.PREF_SETTINGS_CHANGED, false)) {
            finishActivity()
        } else {
            setResult(Activity.RESULT_OK,Intent())
            finish()
        }
    }

    private fun setStoreDetails(){
        val userStore = AppController.appController.getUserStore()
        userStore?.let {
            this.mStoreDetail = it
            txtStoreName?.text = it.storeName
            if(it.webAddress.isNotEmpty()){
                txtStoreWebAddress?.visibility = View.VISIBLE
                txtStoreWebAddress?.text = it.webAddress
            }
            iconEditStore?.setImageResource(R.drawable.ic_mode_edit_black_24dp)
            ImageHelper.getInstance().showImage(this, it.storePic, storeImage, R.drawable.ic_store_placeholder, R.drawable.ic_store_placeholder)
        }?:run{
            iconEditStore?.setImageResource(R.drawable.ic_add_black_24dp)
        }
    }

    private fun setSettingDetails(){
        txtVersionInfo.text = getTwoStringData(R.string.settings_version,BuildConfig.VERSION_NAME)
        themeLayout?.visibility = if(BuildConfig.IS_THEME_ENABLED) View.VISIBLE else View.GONE
        logoutLayout?.visibility = View.GONE
        txtTitleLogout?.visibility = View.GONE
        if(AppController.appController.getUser()!=null)
        {
            logoutLayout?.visibility = View.VISIBLE
            txtTitleLogout?.visibility = View.VISIBLE
        }
        if(AppConfigHelper.getTenantConfigKey(AppConfigHelper.Keys.HOME_LOCATION_ENABLED)){
            locationLayout?.visibility = View.VISIBLE
            locationTitle?.visibility = View.VISIBLE
            nearBySwitch?.isChecked = tradly.social.common.persistance.shared.PreferenceSecurity.getBoolean(
                AppConstant.PREF_HOME_LOCATION_ENABLED,false)
            nearBySwitch?.setOnCheckedChangeListener { compoundButton, b ->
                if(!b){
                    tradly.social.common.persistance.shared.PreferenceSecurity.putBoolean(
                        AppConstant.SharedPrefKey.PREF_HOME_LOCATION_ENABLED,false)
                }
                else{
                    if (PermissionHelper.checkPermission(this, PermissionHelper.Permission.LOCATION)) {
                        tradly.social.common.persistance.shared.PreferenceSecurity.putBoolean(
                            AppConstant.SharedPrefKey.PREF_HOME_LOCATION_ENABLED,true)
                    } else {
                        LocationHelper.getInstance().requestLocationPermission(this)
                    }
                }
            }
        }

        terms_condition?.visibility = if(AppConfigHelper.getConfigKey<String>(AppConfigHelper.Keys.TERMS_URL, Constant.EMPTY).isEmpty())View.GONE else View.VISIBLE
        privacy_policy?.visibility =  if(AppConfigHelper.getConfigKey<String>(AppConfigHelper.Keys.PRIVACY_URL, Constant.EMPTY).isEmpty())View.GONE else View.VISIBLE
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == AppConstant.ActivityResultCode.UPDATE_USER_DETAIL_RESULT && resultCode == Activity.RESULT_OK){
            setProfile()
        }
        else if(requestCode == AppConstant.ActivityResultCode.ADD_STORE_RESULT_CODE || requestCode == AppConstant.ActivityResultCode.EDIT_STORE && resultCode == Activity.RESULT_OK){
           setStoreDetails()
        }
        else if (requestCode == FileHelper.RESULT_OPEN_GALLERY && resultCode == Activity.RESULT_OK && data != null) {
            val file = FileHelper.createTempFile()
            FileHelper().copy(data.data, file.absolutePath) {
                if (it) {
                    val uri = FileProvider.getUriForFile(this, getPackageName() + ".fileProvider", file)
                    FileHelper.cropImage(uri, this, false, false)
                }
            }
        } else if (requestCode == FileHelper.RESULT_OPEN_CAMERA && resultCode == Activity.RESULT_OK) {
            FileHelper().addImageToGallery(FileHelper.tempFile)
            val uri = FileProvider.getUriForFile(this, "$packageName.fileProvider", FileHelper.tempFile)
            FileHelper.cropImage(uri, this, false, false)
        } else if (requestCode == UCrop.REQUEST_CROP && data != null && resultCode == Activity.RESULT_OK) {
            selectedImagePath = FileHelper.compressImageFile(this, FileHelper.tempFile.absolutePath)
            ImageHelper.getInstance().showImage(this, Uri.fromFile(File(selectedImagePath)), profileImg, R.drawable.placeholder_image, R.drawable.placeholder_image)
            settingsPresenter.uploadProfile(selectedImagePath)
        } else if(requestCode == AppConstant.ActivityResultCode.LOGIN_FROM_SETTINGS){
            setProfile()
            setStoreDetails()
            setSettingDetails()
            if(AppConstant.LoginFor.ADD_ACCOUNT==intent.getStringExtra("isFor")){
                showAddAccountActivity()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PermissionHelper.RESULT_CODE_READ_STORAGE) {
            if (grantResults.count() > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                FileHelper.openGallery(this)
            } else {
                showToast(R.string.app_permission_gallery)
            }
        } else if (requestCode == PermissionHelper.RESULT_CODE_CAMERA) {
            if (grantResults.count() > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    if (grantResults[1] != PackageManager.PERMISSION_GRANTED) {
                        showToast(R.string.app_permission_camera)
                        return
                    }
                }
                FileHelper.openCamera(this)
            } else {
                showToast(R.string.app_permission_camera)
            }
        }
        else if(requestCode == PermissionHelper.RESULT_CODE_LOCATION){
            if (grantResults.count() > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                tradly.social.common.persistance.shared.PreferenceSecurity.putBoolean(AppConstant.PREF_HOME_LOCATION_ENABLED,true)
            } else {
                nearBySwitch?.isChecked = false
                tradly.social.common.persistance.shared.PreferenceSecurity.putBoolean(AppConstant.PREF_HOME_LOCATION_ENABLED,false)
                showToast(R.string.app_need_location_permission)
            }
        }
    }
    private fun showMediaChooser() {
        val dialog = BottomChooserDialog()
        dialog.setListener(object : DialogListener {
            override fun onClick(any: Any) {
                when (any as? Int) {
                    BottomChooserDialog.Type.CAMERA -> {
                        if (PermissionHelper.checkPermission(
                                this@SettingsActivity,
                                PermissionHelper.Permission.CAMERA
                            )
                        ) {
                            FileHelper.openCamera(this@SettingsActivity)
                        } else {
                            ActivityCompat.requestPermissions(
                                this@SettingsActivity,
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) arrayOf(Manifest.permission.CAMERA) else arrayOf(
                                    Manifest.permission.CAMERA,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                                ),
                                PermissionHelper.RESULT_CODE_CAMERA
                            )
                        }
                    }
                    BottomChooserDialog.Type.GALLERY -> {
                        if (PermissionHelper.checkPermission(
                                this@SettingsActivity,
                                PermissionHelper.Permission.READ_PERMISSION
                            )
                        ) {
                            FileHelper.openGallery(this@SettingsActivity)
                        } else {
                            ActivityCompat.requestPermissions(
                                this@SettingsActivity,
                                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                                PermissionHelper.RESULT_CODE_READ_STORAGE
                            )
                        }
                    }
                }
            }
        })
        dialog.show(supportFragmentManager, AppConstant.FragmentTag.BOTTOM_CHOOSER_FRAGMENT)
    }

    override fun onProfileUploadFailure(appError: AppError) {
       ErrorHandler.handleError(this,appError)
       showToast(R.string.settings_cant_upload_profile)
    }

    override fun showProgressDialog(msg: Int) {
        showLoader(msg)
    }

    override fun hideProgressDialog() {
        hideLoader()
    }

    override fun onProfileUploadSuccess(user: User) {
        showToast(R.string.settings_profile_pic_updated_message,Toast.LENGTH_LONG)
    }

    override fun noInternet() {
        showToast(R.string.no_internet)
    }

    override fun onLogoutSuccess() {
        EventHelper.logoutUser()
        MessageHelper.removeRecentMessageListener()
        FireBaseAuthHelper.signOut()
        StripeHelper.endCustomerSession()
        BranchHelper.logoutBranch()
        when(PreferenceSecurity.getString(tradly.social.common.common.AppConstant.PREF_APP_LOGIN_TYPE)){
             AppConstant.SocialLoginProvider.GOOGLE-> mGoogleSignInClient.signOut()
            AppConstant.SocialLoginProvider.FACEBOOK->LoginManager.getInstance().logOut()
        }
        AppController.appController.clearUserData()
        val logoutIntent = Intent(this@SettingsActivity,MainActivity::class.java)
        logoutIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(logoutIntent)
    }

    fun showLocaleDialog(ctx: Context, onClick:(language:String)->Unit){
        var dialog: AlertDialog? = null
        val alertDialog = AlertDialog.Builder(ctx)
        alertDialog.setTitle(tradly.social.common.base.R.string.language_select)
        val prevLocale = tradly.social.common.persistance.shared.PreferenceSecurity.getString(
            preferenceConstant.APP_LOCALE, LocaleHelper.getDefaultLocale(), AppConstant.PREF_LANGUAGE
        )
        var localeList = AppController.appController.getAppConfig()?.localeSupported
        localeList?.apply {
            if(this.isNotEmpty()){
                this.find { it.default }?.default = false
                this.find { it.locale == prevLocale }?.default = true
                val adapter = CountryAdapter(this,ctx, AppConstant.ListingType.LOCALE_LIST)
                alertDialog.setNegativeButton(android.R.string.cancel) { p0, p1 -> dialog?.dismiss()}
                alertDialog.setPositiveButton(android.R.string.ok) { p0, p1 ->
                    localeList.find { it.default }?.apply {
                        if(prevLocale != this.locale){
                            LocaleHelper.setLocale(ctx,this.locale)
                            onClick(this.locale)
                        }
                    }
                }
                alertDialog.setAdapter(adapter) { p0, pos -> }
                dialog = alertDialog.create()
            }
            dialog?.show()
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        settingsPresenter.onDestroy()
    }
}
