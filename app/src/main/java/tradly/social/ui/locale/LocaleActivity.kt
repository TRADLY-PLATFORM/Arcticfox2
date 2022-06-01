package tradly.social.ui.locale

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_locale.*
import kotlinx.android.synthetic.main.toolbar.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import tradly.social.R
import tradly.social.adapter.ListingAdapter
import tradly.social.common.base.*
import tradly.social.data.model.AppDataSyncHelper
import tradly.social.common.util.common.LocaleHelper
import tradly.social.common.util.parser.extension.toJson
import tradly.social.domain.entities.Locale
import tradly.social.ui.intro.IntroActivity
import tradly.social.ui.main.MainActivity
import tradly.social.domain.entities.Intro

class LocaleActivity : BaseActivity() {
    private var localeList = mutableListOf<Locale>()
    private var scope = CoroutineScope(Dispatchers.Main+ Job())
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_locale)
        setToolbar(toolbar,title = R.string.language_choose,backNavIcon = R.drawable.ic_clear_black_24dp)
        initAdapter()
    }

    private fun initAdapter(){
        localeList.addAll(AppController.appController.getAppConfig()!!.localeSupported)
        val listingAdapter = ListingAdapter(this,localeList,
            AppConstant.ListingType.LOCALE_LIST,recyclerView){ position, obj -> }
        recyclerView?.layoutManager = LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)
        recyclerView?.adapter = listingAdapter
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.findItem(R.id.action_save)?.isVisible = true
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.action_save){
            localeList.firstOrNull { l->l.default }?.let{ selectedLocale ->
                LocaleHelper.setLocale(this,selectedLocale.locale)
                getIntroScreenDetails()
            }?:run{
                showToast(R.string.language_alert_message_choose_your_language)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun getIntroScreenDetails(){
        showLoader(R.string.please_wait)
        AppDataSyncHelper.getAppConfig<List<Intro>>(scope,
            AppConfigHelper.Keys.INTRO_SCREENS){ result->
            if(result.isNotEmpty()){
                ImageHelper.getInstance().submitRequest(scope,result.map { it.image }){
                    hideLoader()
                    tradly.social.common.persistance.shared.PreferenceSecurity.putBoolean(
                        AppConstant.PREF_APP_INTRO_LANG_SELECTED,true)
                    startActivity(IntroActivity::class.java,Bundle().apply { putString(AppConstant.BundleProperty.INTRO,result.toJson<List<Intro>>()) })
                    this.finish()
                }
             }
            else{
                hideLoader()
                tradly.social.common.persistance.shared.PreferenceSecurity.putBoolean(AppConstant.PREF_KEY_INTRO,true)
                startActivity(MainActivity::class.java)
                finish()
            }
        }
    }
}