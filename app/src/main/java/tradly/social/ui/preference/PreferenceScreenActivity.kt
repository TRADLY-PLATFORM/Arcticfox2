package tradly.social.ui.preference

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import tradly.social.R
import tradly.social.common.network.parseHelper.ParseConstant
import tradly.social.common.base.ErrorHandler
import tradly.social.domain.entities.AppError
import tradly.social.domain.entities.User
import tradly.social.common.base.BaseActivity
import tradly.social.ui.main.MainActivity
import tradly.social.ui.store.addStore.AddStoreDetailActivity
import kotlinx.android.synthetic.main.activity_preference_screen.*

class PreferenceScreenActivity : BaseActivity(), PreferenceScreenPresenter.View {

    private var zoomInAnimation: Animation? = null
    private var zoomOutAnimation: Animation? = null
    private var selectedCard: Int? = null
    private var preferenceScreenPresenter: PreferenceScreenPresenter? = null
    private var isFromSplash: Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preference_screen)
        preferenceScreenPresenter = PreferenceScreenPresenter(this)
        zoomInAnimation = AnimationUtils.loadAnimation(this, R.anim.zoom_in)
        zoomOutAnimation = AnimationUtils.loadAnimation(this, R.anim.zoom_out)
        isFromSplash = intent.getBooleanExtra("isFromSplash", false)
        cardSeller?.startAnimation(zoomInAnimation)
        cardBuyer?.startAnimation(zoomOutAnimation)
        checkboxSeller.isChecked = true
        backNav?.setOnClickListener { onBackPressed() }
        btnNext?.setOnClickListener {
            if (checkboxBuyer.isChecked) {

            } else {
                val intent = Intent(this, AddStoreDetailActivity::class.java)
                intent.putExtra("isFromBoarding",true)
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
            }
        }
    }

    fun cardClick(view: View) {
        when (view.id) {
            R.id.cardSeller -> {
                if (selectedCard != R.id.cardSeller) {
                    selectedCard = R.id.cardSeller
                    checkboxSeller.isChecked = true
                    checkboxBuyer.isChecked = false
                    cardSeller?.startAnimation(zoomInAnimation)
                    cardBuyer?.startAnimation(zoomOutAnimation)
                }
            }

            R.id.cardBuyer -> {
                if (selectedCard != R.id.cardBuyer) {
                    selectedCard = R.id.cardBuyer
                    checkboxBuyer.isChecked = true
                    checkboxSeller.isChecked = false
                    cardBuyer?.startAnimation(zoomInAnimation)
                    cardSeller?.startAnimation(zoomOutAnimation)
                }
            }
        }
    }

    override fun onSuccess(user: User) {
        val intent = Intent(this, if ((user.userType == ParseConstant.UserType.SELLER)) AddStoreDetailActivity::class.java else MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    override fun onFailure(appError: AppError) {
        ErrorHandler.handleError(this, appError)
    }

    override fun hideProgressDialog() {
        hideLoader()
    }

    override fun showProgressDialog(msg: Int) {
        showLoader(msg)
    }

    override fun noInternet() {
        Toast.makeText(this, getString(R.string.no_internet), Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        preferenceScreenPresenter?.onDestroy()
    }

    override fun onBackPressed() {
        finish()
    }
}
