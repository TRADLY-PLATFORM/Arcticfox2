package tradly.social.common.base

import android.annotation.TargetApi
import android.app.*
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessaging
import org.json.JSONObject
import tradly.social.common.navigation.Activities
import tradly.social.common.navigation.common.NavigationIntent
import tradly.social.common.network.CustomError
import tradly.social.common.util.R
import tradly.social.domain.entities.AppError

object NotificationHelper {
     const val NOTIFICATION_CHANNEL_ID_GENERAL = "notification_channel_general"
     const val NOTIFICATION_CHANNEL_ID_CHAT = "notification_channel_chat"
     const val NOTIFICATION_CHANNEL_ID_LISTING = "notification_channel_listing"
     const val NOTIFICATION_CHANNEL_ID_ORDER = "notification_channel_chat"
     const val NOTIFICATION_CHANNEL_ID_STORE = "notification_channel_store"
     const val CHANNEL_NAME_CHAT = "Chat"
     const val CHANNEL_NAME_LISTING = "Listing"
     const val CHANNEL_NAME_ORDER = "Order"
     const val CHANNEL_NAME_STORE = "Store"
     const val CHANNEL_NAME_GENERAL = "general"
     const val NOTIFICATION_ID = 100

    object NotificationTypes{
        const val TYPE_CHAT = "chat"
        const val TYPE_LISTING = "listing"
        const val TYPE_ORDER = "order"
        const val TYPE_ACCOUNT = "account"
        const val TYPE_MAKE_OFFER = "make_offer"
    }

    object NotificationSubType{
        const val MESSAGE = "message"
        const val IMAGE = "image"
        const val LISTING = "listing"
        const val OTHERS = "others"
        const val APPROVED = "approved"
        const val REJECTED = "rejected"
        const val FOLLOW = "follow"
        const val LIKED = "liked"
        const val STATUS_CHANGED = "status_change"
        const val NEW_ORDER = "new_order"
        const val MAKE_OFFER_REQUESTED = "requested"
        const val MAKE_OFFER_ACCEPTED = "accepted"
        const val MAKE_OFFER_REJECTED = "rejected"
    }

    object PayloadKey{
        const val TYPE = "type"
        const val SUB_TYPE = "sub_type"
        const val DATA = "data"
        const val SENDER_NAME = "sender_name"
        const val MESSAGE = "message"
        const val CHAT_ROOM_ID = "chat_room_id"
        const val IMAGE_PATH = "image_path"
        const val LISTING_ID = "listing_id"
        const val FILE_PATH = "file_path"
        const val ORDER_ID = "order_id"
        const val ORDER_REFERENCE = "order_reference"
        const val ORDER_STATUS = "order_status"
        const val ACCOUNT_ID = "account_id"
        const val ACCOUNT_NAME = "account_name"
        const val FIRST_NAME = "first_name"
    }

    private fun getNotificationManager(context: Context): NotificationManager? {
        return context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }


    private fun registerInformationNotificationChannel(context: Context,channelId: String,channelName:String){
        if (Build.VERSION.SDK_INT >= 26) {
            val mngr = getNotificationManager(context)
            if (mngr!!.getNotificationChannel(channelId) != null) {
                return
            }
            val channel = NotificationChannel(channelId,channelName, NotificationManager.IMPORTANCE_HIGH)
            // Configure the notification channel.
            channel.enableLights(false)
            channel.enableVibration(false)
            channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            val notification = RingtoneManager.getDefaultUri(
                RingtoneManager.TYPE_NOTIFICATION
            )
            channel.setSound(notification, getNotificationAudioAttributes())
            mngr.createNotificationChannel(channel)
        }
    }

    @TargetApi(21)
    private fun getNotificationAudioAttributes(): AudioAttributes? {
        return AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION).build()
    }

    fun getNotificationIcon(): Int {
        return R.drawable.notification_icon
    }

    private fun registerChannels(ctx:Context){
        registerInformationNotificationChannel(ctx, NOTIFICATION_CHANNEL_ID_GENERAL, CHANNEL_NAME_GENERAL)
        registerInformationNotificationChannel(ctx, NOTIFICATION_CHANNEL_ID_CHAT, CHANNEL_NAME_CHAT)
        registerInformationNotificationChannel(ctx, NOTIFICATION_CHANNEL_ID_LISTING, CHANNEL_NAME_LISTING)
        registerInformationNotificationChannel(ctx, NOTIFICATION_CHANNEL_ID_ORDER, CHANNEL_NAME_ORDER)
        registerInformationNotificationChannel(ctx, NOTIFICATION_CHANNEL_ID_STORE, CHANNEL_NAME_STORE)
    }

    fun sendNotification(
        context: Context,
        title: String?,
        message: String?,
        penIndent: Intent?,
        playSound: Boolean,
        channelId: String?,
        notificationId: Int
    ) {
        registerChannels(context)
        val mBuilder =
            NotificationCompat.Builder(context, channelId!!)
                .setSmallIcon(getNotificationIcon())
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
        if (!title.isNullOrEmpty()) {
            mBuilder.setContentTitle(title)
        }
        mBuilder.setAutoCancel(true)
        mBuilder.color = context.resources.getColor(R.color.colorWhite)
        if (playSound) {
            val notification =
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            mBuilder.setSound(notification)
        }
        val nm = getNotificationManager(context)
        val resultPendingIntent = PendingIntent.getActivity(
            context,
            0,
            penIndent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        mBuilder.setContentIntent(resultPendingIntent)
        nm!!.notify(notificationId, mBuilder.build())
    }


     fun cancelNotification(context: Context?, notifyId: Int) {
        getNotificationManager(context!!)!!.cancel(notifyId)
    }

    fun getFCMToken(callback:(token:String?)->Unit){
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                callback(task.result)
            } else {
                ErrorHandler.handleError(
                    exception = AppError(
                        task.exception?.message,
                        CustomError.CODE_FCM_TOKEN_FAILED
                    )
                )
                callback(null)
            }
        }
    }

    fun handleChatNotification(ctx:Context,subType:String,dataPayload:JSONObject){
        try{
            val chatRoomID = dataPayload.optString(PayloadKey.CHAT_ROOM_ID)
            val visibleChatRoomId = AppController.appController.currentChatRoomId
            if(visibleChatRoomId.isNullOrEmpty() || visibleChatRoomId!=chatRoomID){
                var title = dataPayload.optString(PayloadKey.SENDER_NAME)
                var message = ctx.getString(R.string.notification_chat_message_fallback)
                val bundle = Bundle()
                bundle.putBoolean(AppConstant.BundleProperty.IS_FROM_NOTIFICATION,true)
                bundle.putString(PayloadKey.TYPE, NotificationTypes.TYPE_CHAT)
                bundle.putString(PayloadKey.CHAT_ROOM_ID,chatRoomID)
                bundle.putString(PayloadKey.SENDER_NAME,title)
                when(subType){
                    NotificationSubType.MESSAGE ->{
                        message = dataPayload.optString(PayloadKey.MESSAGE,ctx.getString(R.string.notification_chat_message_fallback))
                    }
                    NotificationSubType.IMAGE ->{
                        val imageURL = dataPayload.optString(PayloadKey.IMAGE_PATH)
                        message = ctx.getString(R.string.notification_chat_image_fallback)
                    }
                    NotificationSubType.LISTING ->{
                        message = ctx.getString(R.string.notification_chat_listing_fallback)
                        bundle.putString(PayloadKey.LISTING_ID,dataPayload.optString(PayloadKey.LISTING_ID))
                    }
                    NotificationSubType.OTHERS ->{
                        message = ctx.getString(R.string.notification_chat_file_fallback)
                    }
                }

                if(title.isEmpty()){
                    title = AppController.appController.getString(R.string.app_name)
                }

                sendNotification(ctx,title,message,NavigationIntent.getIntent(Activities.MainActivity,bundle),true, NOTIFICATION_CHANNEL_ID_CHAT, NOTIFICATION_ID)
            }
        }catch (ex:Exception){

        }
    }

    fun handleListingNotification(ctx:Context,subType:String,dataPayload:JSONObject){
        val listingId = dataPayload.optString(PayloadKey.LISTING_ID)
        val message = when(subType){
            NotificationSubType.APPROVED -> ctx.getString(R.string.notification_notification_approved_fallback)
            NotificationSubType.REJECTED -> ctx.getString(R.string.notification_rejected_fallback)
            NotificationSubType.LIKED ->{
                val firstName = dataPayload.optString(PayloadKey.FIRST_NAME)
                String.format(ctx.getString(R.string.notification_liked_fallback),firstName)
            }
            else -> AppConstant.EMPTY
        }
        if(message.isNotEmpty()){
            val bundle = Bundle().apply {
                putString(PayloadKey.TYPE, NotificationTypes.TYPE_LISTING)
                putBoolean(AppConstant.BundleProperty.IS_FROM_NOTIFICATION,true)
                putString(PayloadKey.SUB_TYPE,subType)
                putString(PayloadKey.LISTING_ID,listingId)
            }
            sendNotification(ctx,"Listing",message,NavigationIntent.getIntent(Activities.MainActivity,bundle),true,
                NOTIFICATION_CHANNEL_ID_LISTING,
                NOTIFICATION_ID
            )
        }
    }

    fun handleAccountNotification(ctx: Context,subType:String,dataPayload:JSONObject){
        if(subType == NotificationSubType.FOLLOW){
            val accountId = dataPayload.optString(PayloadKey.ACCOUNT_ID)
            var title = dataPayload.optString(PayloadKey.ACCOUNT_NAME)
            if(title.isEmpty()){
                title = AppController.appController.getString(R.string.app_name)
            }
            val message = String.format(ctx.getString(R.string.notification_followed_fallback),dataPayload.optString(
                PayloadKey.FIRST_NAME
            ))
            val bundle = Bundle().apply {
                putString(PayloadKey.TYPE, NotificationTypes.TYPE_ACCOUNT)
                putBoolean(AppConstant.BundleProperty.IS_FROM_NOTIFICATION,true)
                putString(PayloadKey.SUB_TYPE,subType)
                putString(PayloadKey.ACCOUNT_ID,accountId)
                putString(PayloadKey.ACCOUNT_NAME,title)
            }
            sendNotification(ctx,title,message, NavigationIntent.getIntent(Activities.MainActivity,bundle),true, NOTIFICATION_CHANNEL_ID_STORE,
                NOTIFICATION_ID
            )

        }
    }

    fun handleMakeOfferNotification(ctx: Context,subType:String,dataPayload:JSONObject){
        var title = dataPayload.optString(PayloadKey.SENDER_NAME)
        val bundle = Bundle()
        bundle.putBoolean(AppConstant.BundleProperty.IS_FROM_NOTIFICATION,true)
        bundle.putString(PayloadKey.TYPE, NotificationTypes.TYPE_CHAT)
        bundle.putString(PayloadKey.SENDER_NAME,title)
        val res = when(subType){
            NotificationSubType.MAKE_OFFER_REQUESTED ->R.string.chat_requested_an_offer
            NotificationSubType.MAKE_OFFER_ACCEPTED ->R.string.chat_offer_accepted
            NotificationSubType.MAKE_OFFER_REJECTED ->R.string.chat_offer_declined
            else->R.string.notification_chat_message_fallback
        }

        sendNotification(ctx,title,ctx.getString(res), NavigationIntent.getIntent(Activities.MainActivity,bundle),true, NOTIFICATION_CHANNEL_ID_CHAT, NOTIFICATION_ID)
    }

    fun handleOrderNotification(ctx: Context,subType:String,dataPayload:JSONObject){
        var message = AppConstant.EMPTY
        val orderReference = dataPayload.optString(PayloadKey.ORDER_REFERENCE)
        val title = String.format(ctx.getString(R.string.notification_order_title),orderReference)
        when(subType){
            NotificationSubType.STATUS_CHANGED ->{
                val orderStatus = dataPayload.optString(PayloadKey.ORDER_STATUS).toInt()
                message = String.format(ctx.getString(R.string.notification_order_change_fallback),ctx.getString(getOrderStatus(orderStatus)))
            }
            NotificationSubType.NEW_ORDER -> message = ctx.getString(R.string.notification_new_order_fallback)
        }
        if(message.isNotEmpty()){
            val bundle =Bundle().apply {
                putString(PayloadKey.TYPE, NotificationTypes.TYPE_ORDER)
                putBoolean(AppConstant.BundleProperty.IS_FROM_NOTIFICATION,true)
                putString(PayloadKey.SUB_TYPE,subType)
                putString(PayloadKey.ORDER_ID,dataPayload.optString(PayloadKey.ORDER_ID))
                putString(PayloadKey.ACCOUNT_ID,dataPayload.optString(PayloadKey.ACCOUNT_ID))
            }
            sendNotification(ctx,title,message, NavigationIntent.getIntent(Activities.MainActivity,bundle),true,
                NOTIFICATION_CHANNEL_ID_ORDER,
                NOTIFICATION_ID
            )
        }
    }

    fun getOrderStatus(status:Int) = when(status){
        AppConstant.OrderStatus.ORDER_STATUS_INCOMPLETE -> tradly.social.common.base.R.string.orderdetail_order_status_incomplete
        AppConstant.OrderStatus.ORDER_STATUS_PLACED_SUCCESS -> tradly.social.common.base.R.string.orderdetail_order_placed
        AppConstant.OrderStatus.ORDER_STATUS_CANCELED_BY_CUSTOMER -> tradly.social.common.base.R.string.orderdetail_cancelled
        AppConstant.OrderStatus.ORDER_STATUS_CANCELED_BY_ACCOUNT -> tradly.social.common.base.R.string.orderdetail_cancelled
        AppConstant.OrderStatus.ORDER_STATUS_CUSTOMER_RETURN_INITIATED -> tradly.social.common.base.R.string.orderdetail_return_initiated
        AppConstant.OrderStatus.ORDER_STATUS_IN_PROCESS -> tradly.social.common.base.R.string.orderdetail_order_in_progress
        AppConstant.OrderStatus.ORDER_STATUS_SHIPPED -> tradly.social.common.base.R.string.orderdetail_shipped
        AppConstant.OrderStatus.ORDER_STATUS_UNDELIVERED_RETURNED -> tradly.social.common.base.R.string.orderdetail_undelivered_returned
        AppConstant.OrderStatus.ORDER_STATUS_UNDELIVERED_RETURN_CONFIRMED -> tradly.social.common.base.R.string.orderdetail_undelivered_return_confirmed
        AppConstant.OrderStatus.ORDER_STATUS_DELIVERED -> tradly.social.common.base.R.string.orderdetail_order_delivered
        AppConstant.OrderStatus.ORDER_STATUS_DELIVERED_CONFIRMED -> tradly.social.common.base.R.string.orderdetail_delivery_confirmed
        AppConstant.OrderStatus.ORDER_STATUS_CUSTOMER_RETURN_PICKED -> tradly.social.common.base.R.string.orderdetail_return_pickedup
        AppConstant.OrderStatus.ORDER_STATUS_READY_FOR_PICKUP -> tradly.social.common.base.R.string.orderdetail_ready_for_pickup
        AppConstant.OrderStatus.ORDER_STATUS_CUSTOMER_RETURN_CONFIRMED -> tradly.social.common.base.R.string.orderdetail_order_return_confirmed
        AppConstant.OrderStatus.ORDER_STATUS_CUSTOMER_RETURN_DISPUTED -> tradly.social.common.base.R.string.orderdetail_order_return_disputed
        AppConstant.OrderStatus.ORDER_STATUS_CUSTOMER_UNREACHABLE -> tradly.social.common.base.R.string.orderdetail_unreachable
        AppConstant.OrderStatus.ORDER_STATUS_OUT_FOR_DELIVERY -> tradly.social.common.base.R.string.orderdetail_out_for_delivery
        else -> 0
    }
}