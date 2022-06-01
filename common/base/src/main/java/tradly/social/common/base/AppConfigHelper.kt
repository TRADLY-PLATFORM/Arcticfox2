package tradly.social.common.base
import tradly.social.common.cache.ConfigCache
import tradly.social.common.network.base.ConfigAPIConstant
import tradly.social.common.network.converters.BaseConverter
import tradly.social.common.resources.BuildConfig
import tradly.social.common.resources.BuildConfig.FAQ
import tradly.social.common.util.parser.extension.toList
import tradly.social.domain.entities.Constant
import tradly.social.domain.entities.Intro
import tradly.social.domain.entities.Key

object AppConfigHelper:ConfigAPIConstant() {
    object Keys:ConfigAPIConstant()


    fun getTenantConfig(key: String):Any{
            return when (key) {
                Keys.BRANCH_LINK_DESCRIPTION ->{
                    var desc = AppController.appController.getAppConfig()?.config?.branchLinkDescription
                    if(desc.isNullOrEmpty()){
                        ShareUtil.getShareStoreDescription(AppController.appContext)
                    }
                    else{
                        desc
                    }
                }
                Keys.FAQ_URL ->{
                    val faqUrl =  AppController.appController.getAppConfig()?.config?.faqUrl
                    if(faqUrl.isNullOrEmpty()){
                        FAQ
                    }
                    else{
                        faqUrl
                    }
                }
                Keys.HOME_LOCATION_ENABLED -> AppController.appController.getAppConfig()?.config?.homeLocationEnabled?:false
                Keys.FEEDBACK_ENABLED -> AppController.appController.getAppConfig()?.config?.enableFeedback?:false
                Keys.BRANCH_LINK_BASE_URL ->AppController.appController.getAppConfig()?.config?.branchLinkBaseUrl?:Constant.EMPTY
                else -> AppConstant.EMPTY
            }
        }


        fun getConfig(key: String): Any {
            return when (key) {
                Keys.STRIPE_API_PUB_KEY -> ConfigCache.hashMap.getOrElse(key, { AppConstant.EMPTY })
                Keys.LISTING_ADDRESS_LABEL -> ConfigCache.hashMap.getOrElse(key,{ getStringFromResource(R.string.addproduct_address) })
                Keys.ACCOUNT_ADDRESS_LABEL -> ConfigCache.hashMap.getOrElse(key,{ getStringFromResource(R.string.addstore_address) })
                Keys.HIDE_OFFER_PERCENT -> ConfigCache.hashMap.getOrElse(key, { false })
                Keys.HIDE_LANDMARK -> ConfigCache.hashMap.getOrElse(key, { false })
                Keys.SHOW_MAX_QUANTITY -> ConfigCache.hashMap.getOrElse(key, { false })
                Keys.SHOW_SHIPPING_CHARGE -> ConfigCache.hashMap.getOrElse(key, { false })
                Keys.SHIPPING_METHOD_PREFERENCE -> ConfigCache.hashMap.getOrElse(key, { false })
                Keys.PAYMENT_METHOD -> ConfigCache.hashMap.getOrElse(key, { AppConstant.EMPTY })
                Keys.BRANCH_LINK_DESCRIPTION -> ConfigCache.hashMap.getOrElse(key,{ AppConstant.EMPTY})
                Keys.STRIPE_CONNECT_ACCOUNT_TYPE -> ConfigCache.hashMap.getOrElse(key,{ AppConstant.StripeConnectAccountType.STANDARD})
                Keys.STRIPE_CONFIG_CONNECT_MESSAGE_STANDARD -> ConfigCache.hashMap.getOrElse(key,{ getStringFromResource(
                    R.string.payments_stripe_config_screen_connect_message_standard_fallback) })
                Keys.STRIPE_CONFIG_CONNECT_MESSAGE_EXPRESS -> ConfigCache.hashMap.getOrElse(key,{ getStringFromResource(
                    R.string.payments_stripe_express_connect_message) })
                Keys.STRIPE_CONFIG_DISCONNECT_MESSAGE_STANDARD -> ConfigCache.hashMap.getOrElse(key,{ getStringFromResource(
                    R.string.payments_stripe_config_screen_disconnect_message_standard_fallback) })
                Keys.STRIPE_CONFIG_DISCONNECT_MESSAGE_EXPRESS -> ConfigCache.hashMap.getOrElse(key,{ getStringFromResource(
                    R.string.payments_stripe_config_screen_disconnect_message_express_fallback) })
                Keys.LISTING_MAP_LOCATION_SELECTOR_ENABLED -> ConfigCache.hashMap.getOrElse(key,{false})
                Keys.ACCOUNT_MAP_LOCATION_SELECTOR_ENABLED -> ConfigCache.hashMap.getOrElse(key,{false})
                Keys.ENABLE_BRANCH_UNIQUE_LINK -> ConfigCache.hashMap.getOrElse(key,{false})
                Keys.ACCOUNT_CATEGORY_LABEL -> ConfigCache.hashMap.getOrElse(key,{ getStringFromResource(R.string.addstore_category) })
                Keys.INTRO_SCREENS -> ConfigCache.hashMap.getOrElse(key,{Constant.EMPTY}).toString().toList<Intro>().filter { it.text.isNotEmpty() }
                Keys.SUBSCRIPTION_ENABLED -> ConfigCache.hashMap.getOrElse(key,{false})
                Keys.SUPPORT_URL -> ConfigCache.hashMap.getOrElse(key,{Constant.EMPTY})
                Keys.SUBSCRIPTION_NOT_ACTIVE_MESSAGE -> ConfigCache.hashMap.getOrElse(key,{ getStringFromResource(R.string.addproduct_alert_no_subscription) })
                Keys.STOCK_ENABLED -> ConfigCache.hashMap.getOrElse(key,{true})
                Keys.SIMILAR_LISTING_ENABLED -> ConfigCache.hashMap.getOrElse(key,{true})
                Keys.ACCOUNT_ADDRESS_ENABLED -> ConfigCache.hashMap.getOrElse(key,{false})
                Keys.LISTING_ADDRESS_ENABLED -> ConfigCache.hashMap.getOrElse(key,{false})
                Keys.ENABLE_NEGOTIATE -> ConfigCache.hashMap.getOrElse(key,{false})
                Keys.LISTING_MIN_PRICE-> ConfigCache.hashMap.getOrElse(key,{0})
                Keys.INVITE_FRIENDS_DETAIL_IMAGE->ConfigCache.hashMap.getOrElse(key,{AppConstant.EMPTY})
                Keys.SUBSCRIPTION_ALLOW_LISTINGS_EXHAUSTED_MESSAGE->ConfigCache.hashMap.getOrElse(key,{ getStringFromResource(R.string.subscription_allow_listings_exhausted_message)})
                Keys.SUBSCRIPTION_ALREADY_PURCHASED_MESSAGE->ConfigCache.hashMap.getOrElse(key,{ getStringFromResource(R.string.subscription_already_purchased_message)})
                Keys.FACEBOOK_SIGN_IN->ConfigCache.hashMap.getOrElse(key,{ false })
                Keys.GOOGLE_CLIENT_ID->ConfigCache.hashMap.getOrElse(key,{AppConstant.EMPTY})
                Keys.ENABLE_FACEBOOK_SHARE->ConfigCache.hashMap.getOrElse(key,{ false })
                Keys.FACEBOOK_APP_ID->ConfigCache.hashMap.getOrElse(key,{AppConstant.EMPTY})
                Keys.HOME_ADMOB_ENABLED->ConfigCache.hashMap.getOrElse(key,{ false })
                Keys.LISTINGS_ADMOB_ENABLED->ConfigCache.hashMap.getOrElse(key,{ false })
                Keys.LISTINGS_DETAILS_ADMOB_ENABLED->ConfigCache.hashMap.getOrElse(key,{ false })
                else -> AppConstant.EMPTY
            }
        }

    fun getKeyList() = listOf(
        TERMS_URL,
        PRIVACY_URL,
        REGISTRATION_TITLE)

        fun getConfig(key: String, default: Any) = ConfigCache.hashMap.getOrElse(key,{default})

        inline fun <reified T> getConfigKey(key: String): T = getConfig(key) as T

        inline fun <reified T> getConfigKey(key: String , default:Any): T = getConfig(key,default) as T

        inline fun <reified T> getTenantConfigKey(key: String): T = getTenantConfig(key) as T

        private fun getStringFromResource(res: Int):String{
            ActivityHelper.getCurrentActivityInstance()?.let {
                return it.getString(res)
            }
            return AppConstant.EMPTY
        }

        fun clearConfigs() {
            ConfigCache.clearCache()
        }
    }