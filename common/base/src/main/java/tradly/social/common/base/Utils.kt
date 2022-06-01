package tradly.social.common.base

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.DisplayMetrics
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.moe.pushlibrary.MoEHelper
import kotlinx.android.synthetic.main.dialog_media_chooser_option.view.*
import kotlinx.android.synthetic.main.dialog_product_add_success.view.*
import kotlinx.android.synthetic.main.layout_qty_dialog.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import tradly.social.common.base.*
import tradly.social.common.cache.AppCache
import tradly.social.common.network.CustomError
import tradly.social.common.network.feature.common.datasource.DeviceDataSourceImpl
import tradly.social.common.network.parseHelper.ParseConstant
import tradly.social.common.util.common.LocaleHelper
import tradly.social.common.util.parser.extension.toObject
import tradly.social.domain.entities.*
import tradly.social.domain.repository.DeviceInfoRepository
import tradly.social.domain.usecases.UpdateDeviceInfo
import java.util.Locale
import java.util.regex.Matcher
import java.util.regex.Pattern


typealias preferenceConstant = tradly.social.common.common.AppConstant

class Utils {
    interface DialogInterface {
        fun onAccept()
        fun onReject()
    }

    companion object {
        fun showAlertDialog(
            context: Context,
            title: String?,
            message: String?,
            isCancellable: Boolean,
            withCancel: Boolean,
            dialogInterface: DialogInterface?
        ) = buildAlertDialog(context,title,message,isCancellable,withCancel,
            android.R.string.ok,dialogInterface)

        fun showAlertDialog(
            context: Context,
            title: String?,
            message: String?,
            isCancellable: Boolean,
            withCancel: Boolean,
            positiveButtonName:Int,
            dialogInterface: DialogInterface?
        ) = buildAlertDialog(context,title,message,isCancellable,withCancel,positiveButtonName,dialogInterface)


        private fun buildAlertDialog(context: Context,
                                     title: String?,
                                     message: String?,
                                     isCancellable: Boolean,
                                     withCancel: Boolean,
                                     positiveButtonName:Int,
                                     dialogInterface: DialogInterface?){

            val alertDialog = AlertDialog.Builder(context)
            alertDialog.apply {
                title?.let { setTitle(title) }
                setMessage(message)
                setPositiveButton(positiveButtonName) { dialog, which ->
                    dialog.dismiss()
                    dialogInterface?.onAccept()
                }
                if (withCancel) {
                    alertDialog.setNegativeButton(android.R.string.cancel) { dialog, which ->
                        dialog.dismiss()
                        dialogInterface?.onReject()
                    }
                }
                setCancelable(isCancellable)
            }
            val dialog = alertDialog.create()
            dialog.show()
        }

        fun showQtyDialog(context: Context,stockEnabled:Boolean, stock:Int ,maxQty: String, callback: (any: Any) -> Unit) {
            val alertDialog = AlertDialog.Builder(context)
            alertDialog.setCancelable(true)
            val view = LayoutInflater.from(context).inflate(R.layout.layout_qty_dialog, null, false)
            view?.edQty?.hint = String.format(context.getString(R.string.cart_max_quantity), maxQty)
            alertDialog.setView(view)
            val ad = alertDialog.create()
            view?.actionCancel?.setOnClickListener {
                ad.dismiss()
            }
            view?.actionApply?.setOnClickListener {
                val value = view.edQty.text.trim().toString()
                if (value.isEmpty()) {
                    view.edQty?.error = context.getString(R.string.cart_required)
                }
                else if (stockEnabled && (value.toInt() == 0 || value.toInt() > stock)){
                    view.edQty?.error = context.getString(R.string.cart_invalid_qty)
                }
                else if (value.toInt() == 0 || value.toInt() > maxQty.toInt()) {
                    view.edQty?.error = context.getString(R.string.cart_alert_stock_not_available)
                } else {
                    callback(value)
                    ad.dismiss()
                }
            }
            ad.show()
        }


        fun showProductAddedDialog(context: Context, isForEdit:Boolean, callback: (id: Int) -> Unit) {
            val alertDialog = AlertDialog.Builder(context)
            val view = LayoutInflater.from(context).inflate(R.layout.dialog_product_add_success, null, false)
            view?.txtOne?.text = context.getString(if(isForEdit) R.string.product_updated_successfully else R.string.product_added_successfully)
            alertDialog.setView(view)
            alertDialog.setCancelable(false)
            val ad = alertDialog.create()
            ad.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            if(isForEdit){
                view?.btnTxt?.text = context.getString(R.string.done)
            }
            view?.btnOne?.setOnClickListener {
                callback(R.id.btnOne)
                ad.dismiss()
            }
            view?.txtTwo?.setOnClickListener {
                ad.dismiss()
            }
            ad.show()
        }

        fun showCheckOutSuccessDialog(context: Context,isSuccess:Boolean,callback: (id: Int) -> Unit):AlertDialog {
            val alertDialog = AlertDialog.Builder(context)
            val view = LayoutInflater.from(context).inflate(R.layout.dialog_product_add_success, null, false)
            view?.paymentStatusImage?.setImageResource(if(isSuccess) R.drawable.ic_done else R.drawable.ic_order_payment_failed)
            view?.txtOne?.text = context.getString(if(isSuccess) R.string.payment_order_successfully_placed else R.string.payment_order_payment_failed)
            view?.txtTwo?.visibility = View.VISIBLE
            if(!isSuccess){
                view?.statusMessage?.setVisible()
            }
            view?.txtTwo?.text = context.getString(R.string.addproduct_go_to_myorders)
            view?.btnTxt?.text = context.getString(if (isSuccess) R.string.payment_continue_shopping else R.string.payment_order_retry_payment)
            alertDialog.setView(view)
            alertDialog.setCancelable(false)
            val ad = alertDialog.create()
            ad.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            view?.btnOne?.setOnClickListener {
                callback(R.id.btnOne)
            }
            view?.txtTwo?.setOnClickListener {
                callback(R.id.txtTwo)
            }
            return ad
        }

        fun showDirectCheckOutSuccessDialog(context: Context,isSuccess:Boolean,callback: (id: Int) -> Unit):AlertDialog {
            val alertDialog = AlertDialog.Builder(context)
            val view = LayoutInflater.from(context).inflate(R.layout.dialog_product_add_success, null, false)
            view?.paymentStatusImage?.setImageResource(if(isSuccess) R.drawable.ic_done else R.drawable.ic_order_payment_failed)
            view?.txtOne?.text = context.getString(if(isSuccess) R.string.payment_event_successfully_booked else R.string.payment_order_payment_failed)
            view?.txtTwo?.visibility = View.VISIBLE
            if(!isSuccess){
                view?.statusMessage?.setVisible()
            }
            view?.txtTwo?.text = context.getString(R.string.addproduct_go_to_home)
            view?.btnTxt?.text = context.getString(if (isSuccess) R.string.payment_continue_shopping else R.string.payment_order_retry_payment)
            alertDialog.setView(view)
            alertDialog.setCancelable(false)
            val ad = alertDialog.create()
            ad.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            view?.btnOne?.setOnClickListener {
                callback(R.id.btnOne)
            }
            view?.txtTwo?.setOnClickListener {
                callback(R.id.txtTwo)
            }
            return ad
        }


        fun showMediaChooseDialog(context: Context,callback: (view: Int) -> Unit){
            val alertDialog = AlertDialog.Builder(context)
            alertDialog.setCancelable(true)
            val view = LayoutInflater.from(context).inflate(R.layout.dialog_media_chooser_option, null, false)
            view.dialogContainer.background = getDrawable(context,0,color = R.color.white,stokeColor = 0,radius = 12f)
            alertDialog.setView(view)
            val ad = alertDialog.create()
            ad.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            view.clPhotoView.setOnClickListener {
                ad.dismiss()
                callback(view.clPhotoView.id)
            }
            view.clGalleryView.setOnClickListener {
                ad.dismiss()
                callback(view.clGalleryView.id)
            }
            view.btnCancel.setOnClickListener { ad.dismiss() }
            ad.show()
        }

        fun showAppUpdateDialog(context: Context) {
            var ad:AlertDialog?=null
            val alertDialog = AlertDialog.Builder(context)
            alertDialog.setCancelable(true)
            alertDialog.setTitle(R.string.update_tradly)
            alertDialog.setMessage(R.string.version_update_msg)
            alertDialog.setPositiveButton(R.string.update) { p0, p1 ->
                launchPlayStore(context)
                ad?.dismiss()
            }
            alertDialog.setCancelable(false)
            ad = alertDialog.create()
            ad.show()
        }

        fun launchPlayStore(ctx:Context,packageName:String? = null){
            try{
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse(ctx.getString(R.string.google_play_url,packageName?:ctx.packageName))
                    setPackage("com.android.vending")
                }
                ctx.startActivity(intent)
            }
            catch (ex:ActivityNotFoundException){
                ctx.showToast(ctx.getString(R.string.chosen_app_not_found,ctx.getString(R.string.playstore)))
            }
        }


        fun hideKeyBoard(activity: Activity) {
            var imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            val view = activity.currentFocus
            if (view != null) {
                imm?.hideSoftInputFromWindow(activity.currentFocus?.windowToken, 0)
            }

        }

        fun isValidUrl(url:String) = Patterns.WEB_URL.matcher(url).find()

        fun validateEmail(hex: String?): Boolean {
            val pattern: Pattern
            val matcher: Matcher
            val EMAIL_PATTERN =
                "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$"
            pattern = Pattern.compile(EMAIL_PATTERN)
            matcher = pattern.matcher(hex)
            return matcher.matches()
        }

        fun getPixel(context: Context, value: Int): Int {
            val density = context.resources
                .displayMetrics
                .density
            return Math.round(value.toFloat() * density)
        }

          fun getDeviceDetail(callback: (device: Device) -> Unit) {
              NotificationHelper.getFCMToken { token ->
                  val device = Device().apply {
                      deviceName = Build.MANUFACTURER + " " + Build.MODEL
                      deviceManufacturer = Build.MANUFACTURER
                      deviceModel = Build.MODEL
                      osVersion = Build.VERSION.RELEASE
                      osType = AppConstant.DEVICE_SOURCE
                      appVersion = BuildConfig.VERSION_NAME
                      locale = LocaleHelper.getCurrentAppLanguage()
                      fcmToken = token
                  }
                  callback(device)
              }
        }

        fun updateDeviceDetail(forceUpdate:Boolean = false){
            val device = if(AppCache.getDeviceInfo() != null){
                AppCache.getDeviceInfo()
            }
            else{
               val encString = tradly.social.common.persistance.shared.PreferenceSecurity.getString(
                   preferenceConstant.DEVICE_INFO
               )
               tradly.social.common.persistance.shared.PreferenceSecurity.getDecryptedString(encString).toObject<Device>()
           }
           AppCache.cacheDeviceInfo(device)
           getDeviceDetail {
               val deviceInfoRepository = DeviceInfoRepository(DeviceDataSourceImpl())
               val updateDeviceInfo = UpdateDeviceInfo(deviceInfoRepository)
               if(device != null && !forceUpdate){
                   if(!(device.osVersion != it.osVersion ||
                       device.appVersion != it.appVersion ||
                       device.locale != it.locale ||
                       device.fcmToken != it.fcmToken)){
                       return@getDeviceDetail
                   }
               }
               GlobalScope.launch(Dispatchers.IO){
                   updateDeviceInfo.invoke(it.deviceName,it.deviceManufacturer,it.deviceModel,it.appVersion,it.osVersion,it.fcmToken,it.locale,it.clientType)
               }
           }
        }

        fun showSnackBarSettings(viewGroup:ViewGroup , ctx:Context){
            Snackbar.make(viewGroup,ctx.getString(R.string.app_need_location_permission),
                Snackbar.LENGTH_SHORT).setAction(ctx.getString(R.string.settings_header_title)){ view ->
                val intent = Intent()
                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                intent.data = Uri.fromParts("package",ctx.packageName,null)
                ctx.startActivity(intent)

            }.show()
        }

        @SuppressLint("HardwareIds")
        fun getAndroidID():String = Settings.Secure.getString(AppController.appContext.contentResolver, Settings.Secure.ANDROID_ID)

        fun getAppLocale(): String = Locale.getDefault().language

        fun isValidNumberFormat(regex:String,number:String):Boolean{
            return try{
                Pattern.compile(regex).matcher(number).find()
            }catch (ex:Exception){
                ErrorHandler.handleError(
                    exception = AppError(
                        ex.message,
                        CustomError.CODE_CATCH_PART
                    )
                )
                true
            }
        }


        fun showGoogleMap(lat:Double,lng:Double,label:String? = Constant.EMPTY){
            val uri = if(!label.isNullOrEmpty()){
                Uri.parse("geo:0,0?q=$lat,$lng($label)")
            }
            else{
                Uri.parse("geo:0,0?q=$lat,$lng")
            }
            val intent =  Intent(Intent.ACTION_VIEW, uri)
            intent.setPackage("com.google.android.apps.maps")
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            try{
                AppController.appContext.startActivity(intent)
            }
            catch (ex:ActivityNotFoundException){
               ActivityHelper.getCurrentActivityInstance()?.apply {
                   showToast(getString(R.string.chosen_app_not_found,getString(R.string.google_map)))
               }
            }
        }

        fun openUrlInBrowser(url:String){
            if(url.isNotEmpty()){
                try{
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse(url)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    AppController.appContext.startActivity(intent)
                }catch (ex:ActivityNotFoundException){
                    AppController.appContext.showToast(R.string.browser_not_found)
                }
            }
        }

        fun<T> changeTenant(activity: Activity,destActivity: Class<T>){
            showAlertDialog(activity,activity.getString(R.string.splash_change_tenant),activity.getString(
                R.string.more_are_you_sure_change_tenant_id
            ), true, true,object : DialogInterface {
                override fun onAccept() {
                    AppController.appController.clearUserDataWithConfig()
                    LocaleHelper.setLocale(activity, LocaleHelper.getHomeLocale())
                    val logoutIntent = Intent(activity, destActivity)
                    logoutIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    MoEHelper.getInstance(AppController.appContext).logoutUser()
                    BranchHelper.logoutBranch()
                    (activity as BaseActivity).startActivity(logoutIntent)
                }

                override fun onReject() {
                }
            })
        }

        fun getOtpSentTo(ctx:Context,bundle: Bundle , authType:Int?) = when(authType){
            ParseConstant.AuthType.EMAIL->bundle.getString("email")
            ParseConstant.AuthType.MOBILE,
            ParseConstant.AuthType.MOBILE_PASSWORD->{
                val country = bundle.getString("country").toObject<Country>()
                String.format(ctx.getString(R.string.otp_sent_to),country?.dialCode,bundle.getString("mobile"))
            }
            else-> Constant.EMPTY
        }

        fun getOrderStatus(status:Int) = when(status){
            AppConstant.OrderStatus.ORDER_STATUS_INCOMPLETE -> R.string.orderdetail_order_status_incomplete
            AppConstant.OrderStatus.ORDER_STATUS_PLACED_SUCCESS -> R.string.orderdetail_order_placed
            AppConstant.OrderStatus.ORDER_STATUS_CANCELED_BY_CUSTOMER -> R.string.orderdetail_cancelled
            AppConstant.OrderStatus.ORDER_STATUS_CANCELED_BY_ACCOUNT -> R.string.orderdetail_cancelled
            AppConstant.OrderStatus.ORDER_STATUS_CUSTOMER_RETURN_INITIATED -> R.string.orderdetail_return_initiated
            AppConstant.OrderStatus.ORDER_STATUS_IN_PROCESS -> R.string.orderdetail_order_in_progress
            AppConstant.OrderStatus.ORDER_STATUS_SHIPPED -> R.string.orderdetail_shipped
            AppConstant.OrderStatus.ORDER_STATUS_UNDELIVERED_RETURNED -> R.string.orderdetail_undelivered_returned
            AppConstant.OrderStatus.ORDER_STATUS_UNDELIVERED_RETURN_CONFIRMED -> R.string.orderdetail_undelivered_return_confirmed
            AppConstant.OrderStatus.ORDER_STATUS_DELIVERED -> R.string.orderdetail_order_delivered
            AppConstant.OrderStatus.ORDER_STATUS_DELIVERED_CONFIRMED -> R.string.orderdetail_delivery_confirmed
            AppConstant.OrderStatus.ORDER_STATUS_CUSTOMER_RETURN_PICKED -> R.string.orderdetail_return_pickedup
            AppConstant.OrderStatus.ORDER_STATUS_READY_FOR_PICKUP -> R.string.orderdetail_ready_for_pickup
            AppConstant.OrderStatus.ORDER_STATUS_CUSTOMER_RETURN_CONFIRMED -> R.string.orderdetail_order_return_confirmed
            AppConstant.OrderStatus.ORDER_STATUS_CUSTOMER_RETURN_DISPUTED -> R.string.orderdetail_order_return_disputed
            AppConstant.OrderStatus.ORDER_STATUS_CUSTOMER_UNREACHABLE -> R.string.orderdetail_unreachable
            AppConstant.OrderStatus.ORDER_STATUS_OUT_FOR_DELIVERY -> R.string.orderdetail_out_for_delivery
            else -> 0
        }

        private fun getNotificationOrderStatus(status:Int) = when(status){
            AppConstant.OrderStatus.ORDER_STATUS_INCOMPLETE -> R.string.orderdetail_order_status_incomplete
            AppConstant.OrderStatus.ORDER_STATUS_PLACED_SUCCESS -> R.string.notification_order_placed_success
            AppConstant.OrderStatus.ORDER_STATUS_CANCELED_BY_CUSTOMER -> R.string.notification_order_cancelled
            AppConstant.OrderStatus.ORDER_STATUS_CANCELED_BY_ACCOUNT -> R.string.notification_order_cancelled
            AppConstant.OrderStatus.ORDER_STATUS_CUSTOMER_RETURN_INITIATED -> R.string.notification_order_return_initiated
            AppConstant.OrderStatus.ORDER_STATUS_IN_PROCESS -> R.string.notification_order_is_in_progress
            AppConstant.OrderStatus.ORDER_STATUS_SHIPPED -> R.string.notification_order_shipped
            AppConstant.OrderStatus.ORDER_STATUS_UNDELIVERED_RETURNED -> R.string.notification_order_deliver_returned
            AppConstant.OrderStatus.ORDER_STATUS_UNDELIVERED_RETURN_CONFIRMED -> R.string.notification_order_delivery_return_confrmed
            AppConstant.OrderStatus.ORDER_STATUS_DELIVERED -> R.string.notification_order_order_delivered
            AppConstant.OrderStatus.ORDER_STATUS_CUSTOMER_RETURN_PICKED -> R.string.notification_order_picked_up
            AppConstant.OrderStatus.ORDER_STATUS_CUSTOMER_RETURN_CONFIRMED -> R.string.notification_order_customer_return_confirmed
            AppConstant.OrderStatus.ORDER_STATUS_CUSTOMER_RETURN_DISPUTED -> R.string.notification_order_customer_return_disputed
            AppConstant.OrderStatus.ORDER_STATUS_CUSTOMER_UNREACHABLE -> R.string.notification_order_customer_unreachable
            AppConstant.OrderStatus.ORDER_STATUS_OUT_FOR_DELIVERY -> R.string.notification_order_out_for_delivery
            AppConstant.OrderStatus.ORDER_STATUS_READY_FOR_PICKUP -> R.string.orderdetail_ready_for_pickup
            AppConstant.OrderStatus.ORDER_STATUS_DELIVERED_CONFIRMED -> R.string.orderdetail_delivery_confirmed
            else -> 0
        }

        fun getNotificationMessage(ctx: Context,notification: Notification):String{
            val type = notification.type
            return when(notification.referenceType){
                AppConstant.NotificationType.ACTIVITY_REFERENCE_TYPE_ACCOUNT ->{
                    when(type){
                        AppConstant.NotificationType.ACTIVITY_TYPE_ACCOUNT_FOLLOW ->{
                            String.format(ctx.getString(R.string.notification_started_following_your_account),notification.user.name,notification.store.storeName)
                        }
                        else-> AppConstant.EMPTY
                    }
                }
                AppConstant.NotificationType.ACTIVITY_REFERENCE_TYPE_LISTING ->{
                    when(type){
                        AppConstant.NotificationType.ACTIVITY_TYPE_LIKE_LISTING ->{
                            String.format(ctx.getString(R.string.notification_liked_your_listing),notification.user.name)
                        }
                        else-> AppConstant.EMPTY
                    }

                }
                AppConstant.NotificationType.ACTIVITY_REFERENCE_TYPE_ORDER ->{
                    val resId = getNotificationOrderStatus(notification.orderStatus)
                    if(resId!=0){
                        return ctx.getString(resId)
                    }
                    AppConstant.EMPTY
                }
                else-> AppConstant.EMPTY
            }
        }

        fun getTransactionTypeInfo(type:String):Int = when(type){
            AppConstant.TransactionTypes.SALES,
            AppConstant.TransactionTypes.SALES_NO_PAYOUTS -> R.string.sales
            AppConstant.TransactionTypes.COMMISSION -> R.string.commision_created
            AppConstant.TransactionTypes.COMMISSION_REVERSAL -> R.string.commision_canceled
            AppConstant.TransactionTypes.SALES_REVERSAL,
            AppConstant.TransactionTypes.SALES_REVERSAL_NO_PAYOUTS -> R.string.sales_cancelled
            AppConstant.TransactionTypes.SELLER_TRANSFER -> R.string.transfer_created
            AppConstant.TransactionTypes.SELLER_TRANSFER_REVERSAL -> R.string.transfer_canceled
            AppConstant.TransactionTypes.SELLER_SUBSCRIPTION -> R.string.subscription_fee
            AppConstant.TransactionTypes.SELLER_PAYMENT_PROCESSING_FEE -> R.string.processing_fee
            AppConstant.TransactionTypes.SELLER_PAYMENT_PROCESSING_FEE_REVERSAL -> R.string.processing_fee_canceled
            else->0
        }

        fun getRatingPercent(x:Int,y:Int):Int{
            if(x!=0){
                return y percentOf x
            }
            return 5
        }

        fun getDrawable(ctx:Context,strokeWidth:Int,stokeColor:Int,color:Int=0,radius:Float=0f ,shape:Int = GradientDrawable.RECTANGLE):GradientDrawable{
            val gradientDrawable = GradientDrawable()
            gradientDrawable.shape = shape
            if(strokeWidth!=0){
                gradientDrawable.setStroke(getPixel(ctx,strokeWidth),ContextCompat.getColor(ctx,stokeColor))
            }
            if(color!=0){
                gradientDrawable.setColor(ContextCompat.getColor(ctx,color))
            }
            if(radius!=0f){
                gradientDrawable.cornerRadius = radius
            }
            return gradientDrawable
        }

        fun getRippleDrawable(ctx:Context,strokeWidth:Int,stokeColor:Int,color:Int=0,radius:Float=0f,pressedColor:Int):RippleDrawable{
            return RippleDrawable(getColorStateList(pressedColor), getDrawable(ctx, strokeWidth, stokeColor, color, radius),null)
        }

        fun getRippleDrawable(pressedColor:Int,drawable:Drawable):RippleDrawable{
            return RippleDrawable(getColorStateList(pressedColor),drawable,null)
        }

        fun getColorStateList(pressedColor:Int):ColorStateList{
            return ColorStateList(arrayOf(intArrayOf()), intArrayOf(pressedColor))
        }
        fun getSpannableMandatoryTitle(title: String):SpannableString{
            if(title.isNotEmpty()) {
                val spannableString = SpannableString("$title *")
                spannableString.setSpan(ForegroundColorSpan(ContextCompat.getColor(
                    AppController.appContext,
                    R.color.colorRed
                )), spannableString.length - 1, spannableString.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                return spannableString
            }
            return SpannableString(title)
        }

        private fun getNavigationBarHeight(activity: Activity): Int {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                val metrics = DisplayMetrics()
                activity.getWindowManager().getDefaultDisplay().getMetrics(metrics)
                val usableHeight = metrics.heightPixels
                activity.getWindowManager().getDefaultDisplay().getRealMetrics(metrics)
                val realHeight = metrics.heightPixels
                return if (realHeight > usableHeight) realHeight - usableHeight else 0
            }
            return 0
        }

        fun getDisplayHeight(activity: Activity):Int{
            val metrics = DisplayMetrics()
            activity.windowManager.defaultDisplay.getMetrics(metrics)
            return metrics.heightPixels + getNavigationBarHeight(activity)
        }
    }
}