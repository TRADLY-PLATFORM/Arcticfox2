package tradly.social.common.network.base

open class ConfigAPIConstant {

        val STRIPE_API_PUB_KEY = "stripe_api_publishable_key"
        val LISTING_ADDRESS_LABEL = "listing_address_label"
        val HIDE_OFFER_PERCENT = "hide_offer_percent"
        val HIDE_LANDMARK = "hide_landmark"
        val ACCOUNT_ADDRESS_LABEL = "account_address_label"
        val SHOW_MAX_QUANTITY = "show_max_quantity"
        val SHOW_SHIPPING_CHARGE = "show_shipping_charges"
        val SHIPPING_METHOD_PREFERENCE = "enable_shipping_methods_preference"
        val BRANCH_LINK_DESCRIPTION = "branch_link_description"
        val FAQ_URL= "faq_url"
        val TERMS_URL = "terms_url"
        val PRIVACY_URL = "privacy_policy_url"
        val STRIPE_CONNECT_ACCOUNT_TYPE = "stripe_connect_account_type"
        val STRIPE_CONFIG_CONNECT_MESSAGE_STANDARD = "stripe_config_screen_connect_message_standard"
        val STRIPE_CONFIG_CONNECT_MESSAGE_EXPRESS = "stripe_config_screen_connect_message_express"
        val STRIPE_CONFIG_DISCONNECT_MESSAGE_STANDARD  = "stripe_config_screen_disconnect_message_standard"
        val STRIPE_CONFIG_DISCONNECT_MESSAGE_EXPRESS  = "stripe_config_screen_disconnect_message_express"
        val PAYMENT_METHOD = "payout_method"
        val LISTING_MAP_LOCATION_SELECTOR_ENABLED = "listing_map_location_selector_enabled"
        val ACCOUNT_MAP_LOCATION_SELECTOR_ENABLED = "account_map_location_selector_enabled"
        val HOME_LOCATION_ENABLED = "home_location_enabled"
        val BRANCH_LINK_BASE_URL = "branch_link_base_url"
        val ENABLE_BRANCH_UNIQUE_LINK = "enable_branch_unique_url"
        val FEEDBACK_ENABLED = "enable_feedback"
        val ACCOUNT_CATEGORY_LABEL = "account_category_label"
        val INTRO_SCREENS = "intro_screens"
        val SUBSCRIPTION_ENABLED = "subscription_enabled"
        val SUBSCRIPTION_NOT_ACTIVE_MESSAGE = "subscription_not_active_message"
        val SUPPORT_URL = "support_url"
        val REGISTRATION_TITLE = "registration_title"
        val APP_TITLE_HOME = "app_title_home"
        val HOME_INVITE_FRIEND_COLLECTION = "invite_friends_collection_enabled"
        val INVITE_FRIENDS_DETAIL_IMAGE = "invite_friends_detail_image"
        val HOME_PROMO_BANNER_ENABLED = "home_promo_banners_enabled"
        val HOME_CATEGORY_ENABLED = "home_categories_enabled"
        val STRIPE_ENABLED = "stripe_enabled"
        val BOTTOM_TAB_MORE_LABEL = "tab_item_more_text"
        val STOCK_ENABLED = "enable_stock"
        val FEEDBACK_CONTACT_FIELD = "feedback_contact_field"
        val SIMILAR_LISTING_ENABLED = "enable_similar_listings"
        val SHOW_SUB_CATEGORY_AS_LIST = "show_sub_categories_as_list"
        val SHOW_PENDING_BALANCE = "show_pending_balance"
        val LISTING_ADDRESS_ENABLED = "listing_address_enabled"
        val ACCOUNT_ADDRESS_ENABLED = "account_address_enabled"
        val ENABLE_NEGOTIATE = "enable_negotiate"
        val HIDE_LISTING_TAGS = "listing_hide_tags"
        val LISTING_PHOTO_UPLOAD_COUNT = "listing_pictures_count"
        val LISTING_MIN_PRICE = "listing_min_price"
        val SUBSCRIPTION_ALREADY_PURCHASED_MESSAGE = "subscription_already_purchased_message"
        val SUBSCRIPTION_ALLOW_LISTINGS_EXHAUSTED_MESSAGE = "subscription_allow_listings_exhausted_message"
        val ENABLE_ADMOB = "enable_admob"
        val LISTINGS_ADMOB_ENABLED = "listings_admob_enabled"
        val LISTINGS_ADMOB_ID = "listings_admob_id"
        val LISTINGS_DETAILS_ADMOB_ENABLED = "listing_detail_admob_enabled"
        val LISTING_DETAIL_ADMOB_ID = "listing_detail_admob_id"
        val HOME_ADMOB_ENABLED = "home_admob_enabled"
        val HOME_ADMOB_ID = "home_admob_id"
        val FACEBOOK_SIGN_IN = "facebook_sign_in"
        val GOOGLE_CLIENT_ID = "google_client_id"
        val ENABLE_FACEBOOK_SHARE = "enable_facebook_share"
        val FACEBOOK_APP_ID = "facebook_app_id"


    companion object{
        const val PAYMENTS = "payments"
        const val LISTINGS = "listings"
        const val ADDRESS = "address"
        const val ACCOUNTS = "accounts"
        const val GENERAL = "general"
        const val ON_BOARDING = "onboarding"
        const val ADMOB = "admob"

        fun getGroupList() = listOf(
            LISTINGS,
            PAYMENTS,
            ADDRESS,
            ACCOUNTS,
            GENERAL,
            ADMOB
        )
    }
}