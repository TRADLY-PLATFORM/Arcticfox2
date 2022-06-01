package tradly.social.ui.sellerProfile

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import tradly.social.R
import tradly.social.adapter.CountryAdapter
import tradly.social.common.*
import tradly.social.domain.entities.AppError
import tradly.social.domain.entities.Country
import tradly.social.domain.entities.User
import kotlinx.android.synthetic.main.activity_edit_profile.*
import kotlinx.android.synthetic.main.activity_edit_profile.spinner
import kotlinx.android.synthetic.main.toolbar.*
import tradly.social.common.base.*

class EditProfileActivity : BaseActivity(), ProfilePresenter.ProfilePresenterView {


    lateinit var isFor: String
    private val countryList = mutableListOf<Country>()
    lateinit var profilePresenter: ProfilePresenter
    lateinit var countryAdapter:CountryAdapter
    private var selectedCountry:Country?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
        setToolbar(toolbar, R.string.edit_profile, R.drawable.ic_back)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
        profilePresenter = ProfilePresenter(this)
        isFor = getIntentExtra("isFor")
        initView()
    }

    private fun initView() {
        if ("name" == isFor) {
            edOne.visibility = View.VISIBLE
            edTwo.visibility = View.VISIBLE
            hintOne.visibility = View.VISIBLE
            hintTwo.visibility = View.VISIBLE
            dropDownValueLayout.visibility = View.GONE
            hintOne.text = getString(R.string.editprofile_first_name)
            hintTwo.text = getString(R.string.editprofile_last_name)
            edOne?.setText(AppController.appController.getUser()?.firstName)
            edTwo?.setText(AppController.appController.getUser()?.lastName)
            edOne?.inputType = InputType.TYPE_CLASS_TEXT
            edTwo?.inputType = InputType.TYPE_CLASS_TEXT
        } else if ("email" == isFor) {
            edOne.visibility = View.VISIBLE
            hintOne.visibility = View.VISIBLE
            hintOne.text = getString(R.string.editprofile_email)
            edOne?.setText(AppController.appController.getUser()?.email)
            edOne?.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        } else {
            hintTwo.visibility = View.VISIBLE
            hintTwo.text = getString(R.string.editprofile_mobile)
            edTwo.setText(AppController.appController.getUser()?.mobile)
            dropDownValueLayout.visibility = View.VISIBLE
            edTwo.visibility = View.VISIBLE
            edTwo?.inputType = InputType.TYPE_CLASS_PHONE
            countryAdapter = CountryAdapter(countryList, this, AppConstant.ListingType.COUNTRY_LIST)
            profilePresenter.getCountries()
            dropDownValueLayout.setOnClickListener { if(countryList.isEmpty()) profilePresenter.getCountries() else spinner.performClick() }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        val saveItem = menu?.findItem(R.id.action_save)
        saveItem?.isVisible = true
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        AppController.appController.getUser()?.let { user->
            when (item.itemId) {
                R.id.action_save -> {
                    if (isFor == "name") {
                        profilePresenter.updateProfile(user.id,isFor, edOne?.text.toString().trim(), edTwo?.text.toString().trim())
                    }
                    else if(isFor == "email"){
                        profilePresenter.updateProfile(
                            user.id,
                            isFor,
                            edOne.text.toString().trim()
                        )
                    }
                    else{
                        selectedCountry?.let { profilePresenter.updateProfile(user.id,isFor,inputOne = edTwo.getString(), country = selectedCountry)
                        }?:run{
                            checkAndDownloadCountries()
                        }
                    }
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun checkAndDownloadCountries(){
        if(countryList.isEmpty()){
            profilePresenter.getCountries()
        }
        else{
            showToast(R.string.editprofile_alert_message_select_country)
        }
    }

    override fun onSuccess(user: User) {
       showToast(R.string.editprofile_profile_updated_message)
        onBackPressed()
    }

    override fun onFailure(appError: AppError) {
        ErrorHandler.handleError(this, appError)
    }

    override fun onLoadCountries(list: List<Country>?) {
        list?.let { it ->
            countryList.clear()
            countryList.addAll(list)
            countryAdapter.notifyDataSetChanged()
            spinner?.adapter = countryAdapter
            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View,
                    position: Int,
                    id: Long
                ) {
                    val country = countryAdapter.getItem(position)
                    if (country != null) {
                        selectedCountry = country as Country
                        txtDropDownValue?.text = String.format(getString(R.string.dialCodeConcat), country.dialCode)
                        ImageHelper.getInstance().showImage(
                            this@EditProfileActivity,
                            country.flag,
                            imageFlag,
                            R.drawable.placeholder_image,
                            R.drawable.placeholder_image
                        )
                        edTwo?.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(country.mobileNumberLength))
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>) {

                }
            }
            it.find { i->i.dialCode == AppController.appController.getUser()?.dialCode }?.let {
                spinner.setSelection(list.indexOf(it))
                return
            }
            spinner.setSelection(0)
        }
    }

    override fun onDisconnectSuccess() {

    }

    override fun showProgressDialog(msg: Int) {
        showLoader(msg)
    }

    override fun hideProgressDialog() {
        hideLoader()
    }

    override fun noInternet() {
        showToast(R.string.no_internet)
    }

    override fun inputError(id: Int, msg: Int) {

    }

    override fun onError(appError: AppError) {

    }

    override fun onDestroy() {
        super.onDestroy()
        profilePresenter.onDestroy()
    }

    override fun onBackPressed() {
        val intent = Intent()
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

}
