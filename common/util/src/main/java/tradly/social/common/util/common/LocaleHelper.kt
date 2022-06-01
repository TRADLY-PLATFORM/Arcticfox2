package tradly.social.common.util.common

import android.annotation.TargetApi
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import tradly.social.common.common.AppConstant
import tradly.social.common.persistance.shared.PreferenceSecurity
import tradly.social.common.util.parser.extension.toList
import tradly.social.common.util.parser.extension.toObject
import tradly.social.domain.entities.AppConfig
import tradly.social.domain.entities.Constant
import java.util.*


object LocaleHelper {

    var currentLocale:String = Constant.EMPTY

    fun initialize(){
        currentLocale = Constant.EMPTY
        PreferenceSecurity.putString(AppConstant.APP_LOCALE, getHomeLocale(),AppConstant.PREF_LANGUAGE)
    }

    fun onAttach(context: Context): Context {
        val lang = getPersistedData(Locale.getDefault().language)
        return setLocale(context, lang)
    }

    fun onAttach(
        context: Context,
        defaultLanguage: String
    ): Context {
        val lang = getPersistedData(defaultLanguage)
        return setLocale(context, lang)
    }

    fun getCurrentAppLanguage(): String = getPersistedData(Locale.getDefault().language)

    fun setLocale(
        context: Context,
        language: String
    ): Context {
        persist(language)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            updateResources(context, language)
        } else updateResourcesLegacy(context, language)
    }

    private fun getPersistedData(defaultLanguage: String):String {
        if(currentLocale.isEmpty()){
            currentLocale = PreferenceSecurity.getString(AppConstant.APP_LOCALE, defaultLanguage, AppConstant.PREF_LANGUAGE)
        }
        return currentLocale
    }

    private fun persist(language: String){
        currentLocale = language
        PreferenceSecurity.putString(AppConstant.APP_LOCALE, language, AppConstant.PREF_LANGUAGE)
    }

    @TargetApi(Build.VERSION_CODES.N)
    private fun updateResources(
        context: Context,
        language: String
    ): Context {
        val localeCode = language.split("_")
        val locale = when{
            localeCode.size>1->Locale(localeCode[0],localeCode[1])
            localeCode.isNotEmpty()->Locale(localeCode[0])
            else-> Locale.getDefault()
        }
        Locale.setDefault(locale)
        val configuration =
            context.resources.configuration
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)
        return context.createConfigurationContext(configuration)
    }

    @SuppressWarnings("deprecation")
    private fun updateResourcesLegacy(
        context: Context,
        language: String
    ): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val resources = context.resources
        val configuration = resources.configuration
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)
        resources.updateConfiguration(configuration, resources.displayMetrics)
        return context
    }

    fun getConfiguredLocale(context: Context,language: String):Context{
        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(Locale(language))
        return context.createConfigurationContext(configuration)
    }

    fun getDefaultLocale() = Locale.getDefault().language

    fun getHomeLocale():String{
        val appConfig = PreferenceSecurity.getString(AppConstant.PREF_KEY_APP_CONFIG)
        val localeList = PreferenceSecurity.getDecryptedString(appConfig).toObject<AppConfig>()?.localeSupported?: listOf()
        val sysDefLocale = getDefaultLocale()
        localeList.apply {
            return this.find { it.default }?.locale?:sysDefLocale
        }
    }
}