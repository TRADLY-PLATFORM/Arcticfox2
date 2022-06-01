package tradly.social.ui.splash

import kotlinx.coroutines.*
import tradly.social.R
import tradly.social.common.base.*
import tradly.social.common.persistance.shared.PreferenceSecurity
import tradly.social.common.util.common.LocaleHelper
import tradly.social.data.model.AppDataSyncHelper
import tradly.social.data.model.dataSource.ParseConfigDataSource
import tradly.social.data.model.dataSource.ParseCountryDataSource
import tradly.social.data.model.dataSource.ParseCurrencyDataSource
import tradly.social.domain.entities.AppConfig
import tradly.social.domain.entities.AppError
import tradly.social.domain.entities.Result
import tradly.social.domain.repository.CountryRepository
import tradly.social.domain.repository.CurrencyRepository
import tradly.social.domain.repository.ParseConfigRepository
import tradly.social.domain.usecases.GetCountry
import tradly.social.domain.usecases.GetCurrency
import tradly.social.domain.usecases.GetTenantConfig
import tradly.social.domain.entities.Intro
import kotlin.coroutines.CoroutineContext

class SplashPresenter(var view: View?) : CoroutineScope {

    private var job: Job
    private var getTenantConfig: GetTenantConfig? = null
    private var getCountry: GetCountry? = null
    private var getCurrency:GetCurrency

    interface View {
        fun onConfigComplete(appConfig: AppConfig)
        fun onFailure(appError: AppError)
        fun invalidInput(resId:Int)
        fun noInternet()
        fun showProgressLoader()
        fun hideProgressLoader()
        fun showIntroScreens()
    }

    init {
        val parseManagerConfigDataSource = ParseConfigDataSource()
        val parseConfigRepository = ParseConfigRepository(parseManagerConfigDataSource)
        val parseCountryDataSource = ParseCountryDataSource()
        val countryRepository = CountryRepository(parseCountryDataSource)
        val currencyDataSource = ParseCurrencyDataSource()
        val currencyRepository = CurrencyRepository(currencyDataSource)
        getCurrency = GetCurrency(currencyRepository)
        getTenantConfig = GetTenantConfig(parseConfigRepository)
        getCountry = GetCountry(countryRepository)
        job = Job()
    }


    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job


    fun getParseConfig(tenantID:String) {
        val parseAppConfig = AppController.appController.getAppConfig()
        parseAppConfig?.let { view?.onConfigComplete(parseAppConfig) } ?: run {
            if(!AppController.appController.isPlatformApp()){
               getTenantConfig(tenantID)
            }
        }
    }

    fun getTenantConfig(tenantID:String){
        if (NetworkUtil.isConnectingToInternet()) {
            launch(Dispatchers.Main) {
                view?.showProgressLoader()
                val result = async(Dispatchers.IO) { getTenantConfig?.invoke(tenantID) }.await()
                val currencyResult = async(Dispatchers.IO) { getCurrency.getCurrencies() }.await()
                when(result){
                    is Result.Success-> {
                        PreferenceSecurity.putString(AppConstant.PREF_CURRENT_TENANT_ID,tenantID)
                        when(currencyResult){
                            is Result.Success->checkAndSubmitIntroImageRequest(result.data)
                            is Result.Error->view?.onFailure(currencyResult.exception)
                        }
                    }
                    is Result.Error->view?.onFailure(result.exception)
                }

            }
        } else {
            view?.noInternet()
        }
    }

    private fun checkAndSubmitIntroImageRequest(config: AppConfig){
        if(config.localeSupported.size<=1){
            LocaleHelper.initialize()
            AppDataSyncHelper.getAppConfig<List<Intro>>(this, AppConfigHelper.Keys.INTRO_SCREENS){ introList->
                PreferenceSecurity.putBoolean(AppConstant.PREF_INTRO_CONFIG_FETCHED,true)
                if(introList.isNotEmpty()){
                    ImageHelper.getInstance().submitRequest(this,introList.map { it.image }){
                        PreferenceSecurity.putBoolean(
                            AppConstant.PREF_INTRO_IMAGES_FETCHED,true)
                        view?.hideProgressLoader()
                        view?.showIntroScreens()
                    }
                }
                else{
                    PreferenceSecurity.putBoolean(
                        AppConstant.PREF_KEY_INTRO,true)
                    view?.hideProgressLoader()
                    view?.onConfigComplete(config)
                }
            }
        }
        else{
            view?.hideProgressLoader()
            view?.onConfigComplete(config)
        }
    }

    fun isValid(tenantID: String) =
        if(tenantID.isEmpty()){
            view?.invalidInput(R.id.edTenantID)
            false
        }
        else{
            true
        }


    fun onDestroy() {
        job.cancel()
        view = null
    }
}