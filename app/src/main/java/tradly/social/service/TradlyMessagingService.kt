package tradly.social.service

import android.content.Intent
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.moengage.firebase.MoEFireBaseHelper
import com.moengage.pushbase.MoEPushHelper
import org.json.JSONObject
import tradly.social.common.base.AppConstant
import tradly.social.common.base.NetworkUtil
import tradly.social.common.util.common.LocaleHelper
import tradly.social.common.base.NotificationHelper
import tradly.social.common.base.Utils
import tradly.social.ui.main.MainActivity
import java.lang.Exception

class TradlyMessagingService :FirebaseMessagingService(){
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        if(MoEPushHelper.getInstance().isFromMoEngagePlatform(remoteMessage.data)){
            MoEFireBaseHelper.getInstance().passPushPayload(applicationContext, remoteMessage.data)
        }
        else if(tradly.social.common.persistance.shared.PreferenceSecurity.getString(AppConstant.PREF_USER).isNotEmpty()){
            remoteMessage.notification?.let { notification->
                val title = notification.title
                val message = notification.body
                val intent = Intent(this,MainActivity::class.java)
                NotificationHelper.sendNotification(this,title,message,intent,true,
                    NotificationHelper.NOTIFICATION_CHANNEL_ID_GENERAL,
                    NotificationHelper.NOTIFICATION_ID)

            }?:run{
                val map:Map<String,String> = remoteMessage.data
                if(map.isNotEmpty()){
                    try{
                        val payload = JSONObject(map)
                        val type = payload.optString(NotificationHelper.PayloadKey.TYPE)
                        val subType = payload.optString(NotificationHelper.PayloadKey.SUB_TYPE)
                        val context = LocaleHelper.getConfiguredLocale(baseContext, LocaleHelper.getCurrentAppLanguage())
                        when(type){
                            NotificationHelper.NotificationTypes.TYPE_CHAT-> NotificationHelper.handleChatNotification(context,subType,payload)
                            NotificationHelper.NotificationTypes.TYPE_LISTING-> NotificationHelper.handleListingNotification(context,subType,payload)
                            NotificationHelper.NotificationTypes.TYPE_ORDER-> NotificationHelper.handleOrderNotification(context,subType,payload)
                            NotificationHelper.NotificationTypes.TYPE_ACCOUNT-> NotificationHelper.handleAccountNotification(context,subType,payload)
                            NotificationHelper.NotificationTypes.TYPE_MAKE_OFFER-> NotificationHelper.handleMakeOfferNotification(context,subType,payload)
                        }
                    }catch (ex:Exception){

                    }
                }
            }
        }

    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        MoEFireBaseHelper.getInstance().passPushToken(applicationContext,token)
        if(tradly.social.common.persistance.shared.PreferenceSecurity.getString(AppConstant.PREF_USER).isNotEmpty() && NetworkUtil.isConnectingToInternet()){
            Utils.updateDeviceDetail(true)
        }
    }
}