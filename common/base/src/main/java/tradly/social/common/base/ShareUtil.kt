package tradly.social.common.base

import android.app.Activity
import android.content.*
import android.net.Uri
import com.facebook.FacebookSdk
import com.facebook.share.model.ShareLinkContent
import com.facebook.share.widget.ShareDialog

object ShareUtil {

    const val WHATSAPP_PCKG = "com.whatsapp"
    const val INSTAGRAM_PCKG = "com.instagram.android"
    const val FACEBOOK_PCKG = "com.facebook.katana"

    fun initFaceBookSdk() {
        val facebookId = getFaceBookId()
        if (!FacebookSdk.isInitialized() && facebookId.isNotEmpty()) {
            FacebookSdk.setApplicationId(facebookId)
            FacebookSdk.setAutoInitEnabled(true)
            FacebookSdk.fullyInitialize()
            FacebookSdk.sdkInitialize(AppController.appContext)
        }
    }

    fun showIntentChooser(context: Context, data: String) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, data)
        val title = context.resources.getString(R.string.share)
        val chooser = Intent.createChooser(intent, title)
        try {
            context.startActivity(chooser)
        } catch (ex: ActivityNotFoundException) {
        }
    }

    fun getShareStoreDescription(context: Context): String {
        return String.format(context.getString(R.string.link_store_desc), context.getString(R.string.app_name))
    }

    fun openThirdPartyApp(context: Context, packageName: String, data: String) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.setPackage(packageName)
        intent.putExtra(Intent.EXTRA_TEXT, data)
        try{
            context.startActivity(intent)
        }
        catch (ex:ActivityNotFoundException){
            context.showToast(R.string.app_not_found)
        }
    }

    fun openFacebookShareDialog(context: Activity, url: String) {
        if (FacebookSdk.isInitialized() && AppConfigHelper.getConfigKey(AppConfigHelper.ENABLE_FACEBOOK_SHARE)) {
            val shareLikConstant = ShareLinkContent.Builder()
            shareLikConstant.setContentUrl(Uri.parse(url))
            val shareDialog = ShareDialog(context)
            if (ShareDialog.canShow(ShareLinkContent::class.java)) {
                shareDialog.show(shareLikConstant.build(), ShareDialog.Mode.AUTOMATIC)
            }
        } else {
            openThirdPartyApp(context, FACEBOOK_PCKG, url)
        }
    }

    fun copyToClipBoard(ctx:Context,data:String){
        val clipBoard = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText(ctx.getString(R.string.storedetail_link_copied),data)
        clipBoard.setPrimaryClip(clipData)
        ctx.showToast(ctx.getString(R.string.storedetail_link_copied))
    }

    private fun getFaceBookId() = AppConfigHelper.getConfigKey<String>(AppConfigHelper.FACEBOOK_APP_ID)

}