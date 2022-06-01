package tradly.social.common.base

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Bundle
import io.branch.indexing.BranchUniversalObject
import io.branch.referral.Branch
import io.branch.referral.BranchError
import io.branch.referral.util.ContentMetadata
import io.branch.referral.util.LinkProperties
import org.json.JSONObject
import tradly.social.common.network.CustomError
import tradly.social.domain.entities.AppError

object BranchHelper {

    object BranchConstant{
        //KEYS
        const val KEY_FEATURE = "feature"
        const val LINK_TYPE = "link_type"
        //store keys
        const val STORE_NAME = "store_name"
        const val STORE_ID =  "store_id"
        //Listing
        const val PRODUCT_ID =  "product_id"

    }

    object Feature{
        const val PRODUCT_SHARING = "Product Sharing"
        const val STORE_SHARING = "Store Sharing"
        const val APP_SHARING = "App Sharing"
        const val COPY_LINK = "copy link"
    }

    object Channel {
        const val GENERAL_SHARE = "General Share"
        const val WHATS_APP_SHARE = "whatsApp"
        const val FACEBOOK = "facebook"
        const val INSTAGRAM = "instagram"
    }

    object LinkType{
        const val TYPE_LISTING = "listing"
        const val TYPE_STORE = "store"
        const val TYPE_APP_SHARE = "app_share"
    }

    var isBranchSessionInitialized: Boolean = false

    fun init() {
        Branch.getAutoInstance(AppController.appContext)
        if (BuildConfig.DEBUG) {
            Branch.enableLogging()
        }
    }

    fun branchSessionInit(
        activity: Activity,
        uri: Uri?,
        callback: ((bundle: Bundle) -> Unit)? = null
    ) {
        if (!isBranchSessionInitialized) {
            isBranchSessionInitialized = true
            Branch.sessionBuilder(activity).withCallback(getInitListener(activity, callback))
                .withData(uri).init()
        }
    }

    fun branchSessionReInit(
        activity: Activity,
        uri: Uri?,
        callback: ((bundle: Bundle) -> Unit)? = null
    ) {
        Branch.sessionBuilder(activity).withCallback(getInitListener(activity, callback))
            .withData(uri).reInit()
    }

    private fun getInitListener(
        activity: Activity,
        callback: ((bundle: Bundle) -> Unit)? = null
    ): Branch.BranchReferralInitListener {
        return Branch.BranchReferralInitListener { referringParams, error ->
            if (!activity.isFinishing) {
                if (error == null) {
                    callback?.let { it(parseURL( referringParams)) }
                } else if (error.errorCode == BranchError.ERR_BRANCH_ALREADY_INITIALIZED) {
                    callback?.let {
                        it(
                            parseURL(
                                Branch.getInstance().latestReferringParams
                            )
                        )
                    }
                }
            }
        }
    }

    private fun parseURL(referringParams: JSONObject?): Bundle {
        val bundle = Bundle()
        if (referringParams != null) {
            if (referringParams.optBoolean("+clicked_branch_link")) {
                val type = referringParams.optString(BranchConstant.LINK_TYPE)
                if (type.isNotEmpty()) {
                    bundle.putString(BranchConstant.LINK_TYPE, type)
                    when(type){
                        LinkType.TYPE_LISTING ->{
                            bundle.putString(
                                BranchConstant.PRODUCT_ID, referringParams.optString(
                                    BranchConstant.PRODUCT_ID
                                ))
                        }
                        LinkType.TYPE_STORE ->{
                            bundle.putString(
                                BranchConstant.STORE_ID, referringParams.optString(
                                    BranchConstant.STORE_ID
                                ))
                            bundle.putString(
                                BranchConstant.STORE_NAME, referringParams.optString(
                                    BranchConstant.STORE_NAME
                                ))
                        }
                    }

                }
            }
        }
        return bundle
    }

    private fun getLinkProperties(
        channel: String,
        feature: String,
        alias: String = AppConstant.EMPTY
    ) =
        LinkProperties().setChannel(channel).setFeature(feature).also { if(alias.isNotEmpty()){ it.alias = alias} }

    private fun getBranchUniversalObject(
        id: String = AppConstant.EMPTY,
        title: String= AppConstant.EMPTY,
        description: String = AppConstant.EMPTY,
        imageUrl: String = AppConstant.EMPTY,
        contentMeta:ContentMetadata?=null
    ) = BranchUniversalObject()
        .setCanonicalIdentifier(id)
        .setTitle(title)
        .setContentDescription(description)
        .setContentImageUrl(imageUrl)
        .setContentIndexingMode(BranchUniversalObject.CONTENT_INDEX_MODE.PUBLIC)
        .setLocalIndexMode(BranchUniversalObject.CONTENT_INDEX_MODE.PUBLIC).also {
            if(contentMeta!=null){
                it.contentMetadata = contentMeta
            }
        }

    fun getBranchStoreSharingUrl(
        context: Context,
        storeId: String,
        storeName: String,
        storeUniqueName:String,
        storeImage: String,
        description: String,
        callback: (url: String?,errorCode:Int) -> Unit) {
        val contentMetadata = ContentMetadata().apply {
            addCustomMetadata(BranchConstant.LINK_TYPE, LinkType.TYPE_STORE)
            addCustomMetadata(BranchConstant.STORE_ID,storeId)
            addCustomMetadata(BranchConstant.STORE_NAME,storeName)
        }
        getBranchUniversalObject(storeId, storeName, description,storeImage,contentMetadata).generateShortUrl(context, getLinkProperties(
            Channel.GENERAL_SHARE, Feature.STORE_SHARING, storeUniqueName)
        )
        { url, error ->
            if (error == null) {
                callback(url,0)
            } else {
                ErrorHandler.handleError(exception = AppError(error.message, CustomError.BRANCH_URL_GENERATION_ERROR))
                ErrorHandler.printLog(error.message)
                callback(null,error.errorCode)
            }
        }
    }

    fun getBranchProductSharingUrl(activity: Activity, id: String, title: String, description: String, imageUrl: String, channel: String, feature: String, callback: (url: String) -> Unit) {

        val contentMetadata = ContentMetadata().apply {
            addCustomMetadata(BranchConstant.LINK_TYPE, LinkType.TYPE_LISTING)
            addCustomMetadata(BranchConstant.PRODUCT_ID,id)
        }
        getBranchUniversalObject(id, title, description, imageUrl,contentMetadata).generateShortUrl(
            activity,
            getLinkProperties(channel, feature)
        ) { url, error ->
            if (error == null) {
                callback(url)
            } else {
                ErrorHandler.handleError(exception = AppError(error.message, CustomError.BRANCH_URL_GENERATION_ERROR))
                ErrorHandler.printLog(error.message)
            }
        }
    }

    fun getAppShareBranchLink(context: Context,channel:String,callback: (url: String) -> Unit){
        getBranchUniversalObject().generateShortUrl(context,
            getLinkProperties(channel, Feature.APP_SHARING)
        ){ url, error ->
            if(error==null){
                tradly.social.common.persistance.shared.PreferenceSecurity.putString(AppConstant.PREF_APP_SHARE_LINK,url)
                callback(url)
            }
            else {
                ErrorHandler.handleError(exception = AppError(error.message, CustomError.BRANCH_URL_GENERATION_ERROR))
                ErrorHandler.printLog(error.message)
                callback(AppConstant.EMPTY)
            }
        }
    }

    fun getAppSharePersistLink():String = tradly.social.common.persistance.shared.PreferenceSecurity.getString(
        AppConstant.PREF_APP_SHARE_LINK)

    fun createAppLink(){
        if(getAppSharePersistLink().isEmpty()){
            getAppShareBranchLink(AppController.appContext, Channel.GENERAL_SHARE){}
        }
    }

    fun logoutBranch() = Branch.getInstance().logout()
}