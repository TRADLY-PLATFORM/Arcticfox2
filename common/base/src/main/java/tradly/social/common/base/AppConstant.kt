package tradly.social.common.base

class AppConstant {
    companion object SharedPrefKey {
        const val DEVICE_SOURCE = 2
        const val EMPTY = ""
        const val PREF_LANGUAGE = "tradly.language.app"
        const val DEFAULT_KEY = "tradly.app"
        const val PREF_KEY_INTRO = "tradly.intro"
        const val PREF_APP_THEME = "tradly.theme"
        const val PREF_APP_INTRO_LANG_SELECTED = "tradly.intro.lang.selected"
        const val PREF_INTRO_IMAGES_FETCHED = "tradly.intro.images.fetched"
        const val PREF_INTRO_CONFIG_FETCHED = "tradly.intro.config.fetched"
        const val PREF_USER = "tradly.user"
        const val PREF_SETTINGS_CHANGED = "settings.changed"
        const val THUMB_PREFIX = "thumb_"
        const val PREF_CURRENT_TENANT_ID = "current.tenant.id"
        const val PREF_HOME_LOCATION_ENABLED = "home_location_enabled"
        const val PREF_SHOULD_ASK_HOME_LOCATION_CONSENT = "home_location_consent"
        const val PREF_APP_SHARE_LINK = "app_share_link"
    }

    object SocialLoginProvider{
        const val GOOGLE = "google"
        const val FACEBOOK = "facebook"
    }

    object FilterType{
        const val PRODUCTS = 1
        const val EVENTS = 2
        const val ACCOUNTS = 3
    }

    object ConfigModuleType{
        const val PRODUCT = 1
        const val EVENT = 2
    }

    object NegotiationStatus{
        const val ACCEPTED = 2
        const val REJECTED = 3
    }

    object AuthType {
        const val AUTH_TYPE = 3
    }

    object BusinessType{
        const val C2C = 1
        const val B2C = 2
    }

    object UrlScheme{
        const val HTTP = "http"
        const val HTTPS = "https"
    }

    object ModuleType{
        const val LISTINGS = "listings"
        const val ACCOUNTS = "accounts"
        const val EVENTS = "events"
    }

    object AccountFeedType{
        const val FOLLOWING = "following"
        const val BLOCKED = "blocked"
    }

    object SocialFeedTypes{
        const val LISTING = "listings"
    }
    object ActivityResultCode{
        const val LOGIN_FROM_SETTINGS = 400
        const val UPDATE_USER_DETAIL_RESULT = 401
        const val LOGIN_FROM_HOME = 402
        const val ADD_STORE_RESULT_CODE = 403
        const val CHAT_FROM_PRODUCT_DETAIL = 404
        const val FOLLOW_LOGIN_FROM_PRODUCT_DETAIL = 405
        const val FOLLOW_LOGIN_FROM_STORE_DETAIL = 406
        const val LOGIN_FROM_CHAT_LIST = 407
        const val LOGIN_FROM_SOCIAL = 408
        const val REFRESH_PROFILE_FROM_SETTINGS = 409
        const val REFRESH_LISTING_IN_STORE_DETAIL = 410
        const val LOGIN_FROM_MY_STORE_PROFILE = 411
        const val LOGIN_FROM_WISH_LIST = 412
        const val LOGIN_FROM_MY_CART = 413
        const val ADD_TO_CART_FROM_PRODUCT_DETAIL = 414
        const val LOGIN_FROM_MY_ORDER = 415
        const val EDIT_STORE = 416
        const val STRIPE_DISCONNECT = 417
        const val REFRESH_PRODUCT_DETAIL = 418
        const val CHAT_FROM_STORE_DETAIL = 419
        const val MAKE_OFFER_PRODUCT_DETAIL = 420
        const val CONFIRM_BOOKING_FROM_PRODUCT_DETAIL = 421
        const val LOGIN_FROM_SEARCH = 422
        const val LOGIN_FROM_IN_APP_PRODUCTS = 423
    }

    object LoginFor{
        const val STORE = "store"
        const val GROUP = "group"
        const val SETTINGS = "settings"
        const val CHAT = "chat"
        const val FOLLOW = "follow"
        const val ADD_CART = "addCart"
        const val MY_CART = "myCart"
        const val SOCIAL = "social"
        const val NOTIFICATION = "notification"
        const val PRODUCT_DETAIL = "productDetail"
        const val ADD_ACCOUNT = "addAccount"
    }

    object FragmentTag{
        const val HOME_FRAGMENT = "homeFragment"
        const val PROFILE_FRAGMENT = "profileFragment"
        const val TRENDING_FRAGMENT = "trendingFragment"
        const val CHAT_FRAGMENT = "ChatListFragment"
        const val CATEGORY_FRAGMENT = "CategoryFragment"
        const val MULTI_CHIP_FRAGMENT = "MultiSelectChipBottomSheet"
        const val PRODUCT_LIST_FRAGMENT = "ProductListFragment"
        const val SET_VARIANT_FRAGMENT = "SetVariantDialog"
        const val BOTTOM_CHOOSER_FRAGMENT = "BottomChooserDialog"
        const val DIALOG_VARIANT = "DialogVariant"
        const val VERIFICATION_FRAGMENT = "VerificationFragment"
        const val LOGIN_FRAGMENT = "LoginFragment"
        const val SIGNUP_FRAGMENT = "SignUpFragment"
        const val FORGOT_PASSWORD_FRAGMENT_ONE = "ForgotPasswordFragmentOne"
        const val FORGOT_PASSWORD_FRAGMENT_TWO = "ForgotPasswordFragmentTwo"
        const val FILTER_BOTTOM_SHEET_DIALOG = "FilterBottomSheetDialog"
        const val CATEGORY_FULLSCREEN_DIALOG = "CategoryFullScreenDialog"
        const val CONFIGURE_PAYOUT_INIT_FRAGMENT = "ConfigurePayoutInitFragment"
        const val CONFIGURE_PAYOUT_FRAGMENT = "ConfigurePayoutFragment"
        const val CONFIGURE_PAYOUT_FRAGMENT_SUCCESS = "ConfigurePayoutFragmentSuccess"
        const val ORDER_SUCCESS_DIALOG = "DialogUtilOrderSuccess"
        const val CART_LIST_FRAGMENT = "CartListFragment"
        const val TRANSACTION_LIST_FRAGMENT = "transactionListFragment"
        const val SEARCH_LOCATION_FRAGMENT = "searchLocationFragment"
        const val INVITE_FRAGMENT = "inviteFragment"
        const val LISTING_FRAGMENT = "listingFragment"
        const val ACCOUNT_FOLLOWERS_FRAGMENT = "AccountFollowersFragment"
    }


    //Don't Use Zero
    object ListingType {
        const val GROUP_FOLLOW = 1
        const val GROUP_TAG_PRODUCT = 2
        const val STORE_LIST = 3
        const val CHAT_ROOM = 4
        const val CART_LIST = 5
        const val VARIANT = 6
        const val SET_LIST = 7
        const val SET_VARIANT_LIST = 8
        const val ADDRESS_LIST = 9
        const val THEME_LIST = 10
        const val SPECIFICATION = 11
        const val WISH_LIST = 12
        const val GRID_LIST = 13
        const val ACCOUNT_CHOOSER_LIST = 14
        const val PAYMENT_TYPES = 15
        const val COUNTRY_LIST = 16
        const val LOCALE_LIST = 17
        const val CURRENCY_LIST = 18
        const val PRODUCT_LIST = 19
        const val ORDER_TIME_LINE_LIST = 20
        const val STATUS_BY_FILTER = 21
        const val SELLER_STATUS_LIST = 22
        const val NOTIFICATION_LIST = 23
        const val TRANSACTION_LIST = 24
        const val CATEGORY_LIST = 25
        const val LOCATION_SEARCH = 26
        const val REVIEW_LIST = 27
        const val SHIPPING_METHOD_LIST = 28
        const val SHIPMENT_METHOD_SELECT_LIST = 29
        const val ATTACHMENT_LIST= 30
        const val PRODUCT_BY_SEARCH = 31
        const val STORE_BY_SEARCH = 32
        const val MY_ORDER_LIST = 33
        const val STORE_PRODUCT_LIST = 34
        const val SIMILAR_PRODUCT_LIST = 35
        const val STORE_FEEDS = 36
        const val STORE_FEEDS_BLOCK = 37
        const val AD_VIEW = 38
    }

    object FeedbackEmailContactField{
        const val EMAIL = 1
        const val PHONE = 2
        const val EMAIL_PHONE = 3
    }

    object ManageAction {
        const val ADD = 1
        const val EDIT = 2
    }

    object SORT {
        const val PRICE_LOW_HIGH = "price_low_to_high"
        const val PRICE_HIGH_LOW = "price_high_to_low"
        const val RECENT = "newest_first"
    }

    object AttrTypes{
        const val SINGLE_SELECT =1
        const val MULTI_SELECT = 2
        const val OPEN_VALUE = 3
        const val OPEN_VALUES = 4
        const val UPLOAD_FIELD = 5
    }
    object CartType {
        const val CART_PRODUCT = 1
        const val CART_VARIANT = 2
        const val CART_SET = 3
    }

    object ListingStatus{
        const val WAITING_FOR_APPROVAL = 1
        const val APPROVED = 2
        const val REJECTED = 3
    }

    object StoreStatus{
        const val WAITING_FOR_APPROVAL = 1
        const val APPROVED = 2
        const val REJECTED = 3
    }

    object CategoryType{
        const val LISTINGS = "listings"
    }
    object BannerType {
        const val BANNER_HOME_LISTING = "listing"
        const val BANNER_HOME_ACCOUNT = "account"
        const val BANNER_HOME_GENERAL = "general"
        const val BANNER_HOME_EXTERNAL = "external"
        const val BANNER_CATEGORY_PRODUCT = "product"
    }

    object MediaContentType {
        const val DEFAULT_IMAGE_MIME_TYPE = "image/jpeg"
        const val IMAGE = "image"
        const val AUDIO = "audio"
        const val VIDEO = "video"
        const val APPLICATION = "application"
        const val TEXT = "text"
    }

    object ListPerPage{
        const val PRODUCT_LIST = 10
        const val ORDER_LIST = 10
        const val TRANSACTION_LIST = 10
        const val SIMILAR_PRODUCT_LIST = 20
    }


    object AddressType{
        const val SHIPPING = "shipping"
        const val PICKUP_ADDRESS = "pickup"
        const val STORAGE_HUB = "storage_hub"
    }
    object BundleProperty{
        const val ID = "id"
        const val ORDER_ID = "orderId"
        const val IS_FOR_PASSWORD_SET = "isForPasswordSet"
        const val VERIFY_ID = "verifyId"
        const val STORE = "store"
        const val STORE_NAME = "storeName"
        const val IS_FOR = "is_for"
        const val ISFOR = "isFor"
        const val IS_FROM = "isFrom"
        const val IS_EDIT = "is_Edit"
        const val START_DATE = "start_date"
        const val END_DATE = "end_date"
        const val IS_MY_STORE_ORDER = "is_my_store_order"
        const val MY_STORE = "my_store"
        const val ACCOUNT_ID = "account_id"
        const val IS_FOR_MY_ORDER = "isForMyOrder"
        const val TOOLBAR_TITLE = "toolbarTitle"
        const val IS_FROM_NOTIFICATION = "isFromNotification"
        const val CHAT_ID = "chat_id"
        const val CHAT_ROOM_ID = "chatroomId"
        const val CHAT_ROOM = "chatRoom"
        const val PRODUCT_ID = "productId"
        const val OAUTH_URL = "OAuthUrl"
        const val FILTER_BY_DISTANCE_KEY = "filterDistanceKey"
        const val IMAGES = "images"
        const val WEB_URL = "web_url"
        const val IS_FOR_PAYOUT_LIST = "isForPayoutList"
        const val CATEGORY_LIST = "categoryList"
        const val PRODUCT_DETAIL = "productDetail"
        const val REVIEW_TITLE = "reviewTitle"
        const val REVIEW_CONTENT = "reviewContent"
        const val REVIEW_RATING = "reviewRating"
        const val SHIPPING_METHOD_ID = "shippingMethodId"
        const val SHIPPING_METHOD = "shippingMethod"
        const val ADDRESS_ID = "addressId"
        const val IS_FOR_PICK_ADDRESS = "isForPickUpAddress"
        const val IS_FROM_ORDER_DETAIL = "isFromOrderDetail"
        const val IS_STRIPE_CONNECTED = "isStripeConnected"
        const val IS_STRIPE_ONBOARD_COMPLETED = "isStripeOnBoardCompleted"
        const val IS_FROM_BROWSER_RESULT = "isFromBrowserResult"
        const val IS_STRIPE_ONBOARDING_CONNECTED = "isStripeOnBoardingConnected"
        const val IS_PAYOUT_ENABLED = "isPayoutEnabled"
        const val STRIPE_ERRORS = "stripeErrors"
        const val CATEGORY_ID = "categoryId"
        const val CATEGORY_NAME = "categoryName"
        const val SEARCH_KEYWORD = "searchKeyword"
        const val SORT = "sort"
        const val EXTRAS = "extras"
        const val STORE_ID = "storeId"
        const val TYPE = "type"
        const val IS_ACTIVE = "isActive"
        const val POSITION = "position"
        const val INTRO = "intro"
        const val TITLE = "title"
        const val IMAGE = "image"
        const val COLLECTION_ID = "collectionId"
        const val IS_FROM_WEB_PAYMENT = "isFromWebPayment"
        const val STATUS = "status"
        const val ORDER_REFERENCE = "orderReference"
        const val PAYMENT_METHOD_ID = "paymentTypeId"
        const val FRAGMENT_TAG = "fragment_tag"
        const val SHIPMENT_TYPE = "shipmentType"
        const val SHOULD_CHECK_SUB_CATEGORY_CONFIG = "checkSubCategoryConfig"
    }

    object OrderStatus{
        const val ORDER_STATUS_INCOMPLETE = 1
        const val ORDER_STATUS_PLACED_SUCCESS = 2
        const val ORDER_STATUS_IN_PROCESS = 3
        const val ORDER_STATUS_SHIPPED = 4
        const val ORDER_STATUS_CUSTOMER_UNREACHABLE = 5
        const val ORDER_STATUS_OUT_FOR_DELIVERY = 6
        const val ORDER_STATUS_UNDELIVERED_RETURNED = 7
        const val ORDER_STATUS_UNDELIVERED_RETURN_CONFIRMED = 8
        const val ORDER_STATUS_DELIVERED = 9
        const val ORDER_STATUS_DELIVERED_CONFIRMED = 10
        const val ORDER_STATUS_CUSTOMER_RETURN_INITIATED = 11
        const val ORDER_STATUS_CUSTOMER_RETURN_PICKED = 12
        const val ORDER_STATUS_CUSTOMER_RETURN_CONFIRMED = 13
        const val ORDER_STATUS_CUSTOMER_RETURN_DISPUTED = 14
        const val ORDER_STATUS_CANCELED_BY_ACCOUNT = 15
        const val ORDER_STATUS_CANCELED_BY_CUSTOMER = 16
        const val ORDER_STATUS_READY_FOR_PICKUP = 17

    }

    object ApiParam{
        const val ACTIVATE = "active"
        const val ACCOUNT = "account"
    }

    object NotificationType{
        const val ACTIVITY_TYPE_ACCOUNT_FOLLOW = 1
        const val ACTIVITY_TYPE_LIKE_LISTING = 2
        const val ACTIVITY_TYPE_ORDER_STATUS_CHANGE = 3
        const val ACTIVITY_REFERENCE_TYPE_ACCOUNT =  1
        const val ACTIVITY_REFERENCE_TYPE_LISTING =  2
        const val ACTIVITY_REFERENCE_TYPE_ORDER =  3
    }


    object PaymentTypes{
        const val COD = "cod"
        const val STRIPE = "stripe"
        const val PAYULATAM = "payulatam"
    }

    object PaymentChannel{
        const val SDK = "sdk"
        const val WEB = "web"
        const val COD = "cod"
    }

    object PaymentStatus{
        const val SUCCESS = "success"
        const val FAILURE = "failure"
    }

    object PayoutMethod{
        const val STRIPE_CONNECT = "stripe"
    }

    object StripeConnectAccountType{
        const val STANDARD = "standard"
        const val EXPRESS = "express"
    }

    object TransactionTypes{
        const val SUPER_TYPE = "2"

        const val SALES = "1"
        const val SALES_NO_PAYOUTS = "2"
        const val SALES_REVERSAL = "3"
        const val SALES_REVERSAL_NO_PAYOUTS = "4"
        const val COMMISSION = "5"
        const val COMMISSION_REVERSAL = "6"
        const val SELLER_TRANSFER = "7"
        const val SELLER_TRANSFER_REVERSAL = "8"
        const val SELLER_SUBSCRIPTION = "10"
        const val SELLER_PAYMENT_PROCESSING_FEE = "11"
        const val SELLER_PAYMENT_PROCESSING_FEE_REVERSAL = "12"
        const val SELLER_PAYOUTS = "9"
    }

    object ShipmentType{
        const val DELIVERY = "delivery"
        const val PICK_UP = "pickup"
        const val STORAGE_HUB = "storage_hub"
    }
}