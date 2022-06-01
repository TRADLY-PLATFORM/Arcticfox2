package tradly.social.common.base

import android.app.Application
import android.content.Context
import android.util.Log
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import com.moe.pushlibrary.MoEHelper
import com.moengage.core.MoEngage
import com.moengage.core.Properties
import com.moengage.core.config.FcmConfig
import com.moengage.firebase.MoEFireBaseHelper
import io.sentry.Sentry
import io.sentry.SentryEvent
import io.sentry.SentryLevel
import io.sentry.protocol.Message
import tradly.social.common.common.AppConstant
import tradly.social.common.persistance.BuildConfig
import tradly.social.domain.entities.User

object EventHelper {

    private lateinit var  facebookEvent:AppEventsLogger
    fun init(application: Application) {
        val moEngage = MoEngage.Builder(application, BuildConfig.MOENGAGE_APP_ID)
        moEngage.setNotificationSmallIcon(NotificationHelper.getNotificationIcon())
        moEngage.setNotificationLargeIcon(NotificationHelper.getNotificationIcon())
        moEngage.configureFcm(FcmConfig(false))
        moEngage.enableSegmentIntegration()
        if(BuildConfig.DEBUG){
            moEngage.setLogLevel(Log.INFO)
        }
        MoEngage.initialise(moEngage.build())
        setAppStatus(application.applicationContext)
        if(FacebookSdk.isInitialized()){
            facebookEvent = AppEventsLogger.newLogger(application.applicationContext)
        }
    }

    private fun setAppStatus(context: Context){
        val previousVersion =  tradly.social.common.persistance.shared.PreferenceSecurity.getInt(AppConstant.STORED_VERSION_CODE, 0)
        when{
            previousVersion == 0 -> {
                MoEHelper.getInstance(context).setAppStatus(com.moengage.core.model.AppStatus.INSTALL)
            }
            BuildConfig.VERSION_CODE>previousVersion->{
                MoEHelper.getInstance(context).setAppStatus(com.moengage.core.model.AppStatus.UPDATE)
                NotificationHelper.getFCMToken {
                    it?.let { token ->
                        MoEFireBaseHelper.getInstance().passPushToken(context, token)
                    }
                }
            }
        }
        tradly.social.common.persistance.shared.PreferenceSecurity.putInt(AppConstant.STORED_VERSION_CODE, BuildConfig.VERSION_CODE)

    }

    object Event{
        const val APP_ERROR = "APP_ERROR"
    }

    object EventParam{
        const val BRANCH_IO_ERROR = "BranchIoError"
    }

    object Tags{
        const val APP = "app"
    }

    fun setUserAttribute(context: Context,user:User){
        val moEngageInstance = MoEHelper.getInstance(context)
        if (user.id.isNotEmpty()) moEngageInstance.setUniqueId(user.id)
        if (user.mobile.isNotEmpty()) moEngageInstance.setNumber("${user.dialCode}+${user.mobile}")
        user.email?.let { moEngageInstance.setEmail(it) }
    }

    fun logEvent(event:String){
        MoEHelper.getInstance(AppController.appController).trackEvent(event, Properties())
    }

    fun logEvent(event:String, param:String, value:String?){
        val properties = Properties()
        properties.addAttribute(param,value)
        MoEHelper.getInstance(AppController.appController).trackEvent(event, properties)
    }

    fun logEvent(event:String ,param:String,value:Boolean){
        val properties = Properties()
        properties.addAttribute(param,value)
        MoEHelper.getInstance(AppController.appController).trackEvent(event, properties)
    }

    fun logEvent(event:String ,param:String,value:Int){
        val properties = Properties()
        properties.addAttribute(param,value)
        MoEHelper.getInstance(AppController.appController).trackEvent(event, properties)
    }

    fun logEvent(event: String,  params:List<String> ,  values:List<Any>){
        val properties = Properties()
        val paramsSize = params.size
        for ( i in values.indices){
            if(i < paramsSize){
                properties.addAttribute(params[i],values[i])
            }
        }
        MoEHelper.getInstance(AppController.appController).trackEvent(event, properties)
    }

    fun logFbEvent(message:String){
        if(EventHelper::facebookEvent.isInitialized) {
            facebookEvent.logEvent(message)
        }
    }

    fun logSentryEvent(errorMessage:String,errorCode:Int,url:String,payload:String){
        if(errorMessage.isNotEmpty()){
            val event = SentryEvent()
            event.level = SentryLevel.ERROR
            event.setTag(Tags.APP,tradly.social.common.resources.BuildConfig.APP_NAME)
            event.message = Message().apply { message = "API Error - Android - $url" }
            event.environment = if(BuildConfig.BUILD_TYPE == "release") "production" else BuildConfig.BUILD_TYPE
            event.setExtras(getSentryExtras(errorMessage, errorCode, url, payload))
            Sentry.captureEvent(event)
        }
    }

    private fun getSentryExtras(errorMessage:String, errorCode:Int, url:String, payload:String):MutableMap<String,Any>{
        val map = mutableMapOf<String,Any>()
        if(payload.isNotEmpty()){
            map["request"] = payload
        }
        map["url"] = url
        map["errorCode"] = errorCode.toString()
        map["message"] = errorMessage
        return map
    }

    fun logoutUser(){
        MoEHelper.getInstance(AppController.appController).logoutUser();
    }
}