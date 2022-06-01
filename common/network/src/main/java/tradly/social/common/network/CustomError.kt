package tradly.social.common.network

object CustomError {

    const val INVALID_CREDENTIALS = 301
    const val USER_ALREADY_REGISTERED = 303
    const val NO_USER_FOUND = 305
    const val NO_OBJECT_FOUND_IN_LOCAL = 204
    const val COUNTRY_NOT_FOUND = 205
    const val CART_ADD_NOT_AVAILABLE = 563

    // app error code
    const val ADD_GROUP_ERROR = 1
    const val ADD_STORE_ERROR = 2
    const val ADD_PRODUCT_ERROR = 3
    const val DELETE_PRODUCT_ERROR = 4
    const val CODE_NO_ADDRESS_SELECTED = 5
    const val TIME_OUT_EXCEPTION = 6
    const val CODE_CATCH_PART = 7
    const val CODE_FCM_TOKEN_FAILED = 8
    const val BRANCH_URL_GENERATION_ERROR = 9
    const val DELETE_CART_ERROR = 10
    const val UNKNOWN_ERROR = 11
    const val FIREBASE_AUTH_FAILED = 12
    const val SET_PASSWORD_FAILED = 13
    const val UPDATE_STATUS_FAILED = 14
    const val CHAT_ROOM_FETCH_FAILED = 15
    const val PAYOUT_SET_UP_FAILED = 16
    const val STRIPE_CONNECT_FAILED = 17
    const val INIT_PAYMENT_FAILED = 18
    const val CHECKOUT_FAILED = 19
    const val STRIPE_PAYMENT_INTENT_FETCH_FAILED = 20
    const val STRIPE_PAYMENT_FAILED = 21
    const val STRIPE_DISCONNECT_FAILED = 22
    const val EARNING_FETCH_FAILED = 23
    const val LOGOUT_FAILED = 24
    const val REVIEW_SUBMIT_FAILED = 25
    const val CLEAR_CART_FAILED = 26
    const val GET_SHIPMENT_FAILED = 27
    const val GET_CATEGORY_LIST_FAILED = 28
    const val GET_USER_DETAIL_FAILED = 29
    const val AUTH_KEY_NOT_AVAILABLE = 30
    const val FEEDBACK_FAILED = 31
    const val STRIPE_STATUS_FAILED = 32
    const val STRIPE_LOGIN_LINK_FAILED = 33
    const val ACCOUNT_ACTIVATION = 34
    const val ACCOUNT_DE_ACTIVATION = 35
    const val SUBSCRIPTION_TRIGGER_MAIL_FAILED = 36
    const val NOT_ABLE_TO_FETCH_ORDERS_ON_CANCEL_ORDER = 37


    //API Error code
    const val CODE_USER_ALREADY_EXISTS = 101
    const val CODE_USER_NOT_REGISTERED = 102
    const val CODE_INVALID_CREDENTIALS = 103
    const val CODE_VERIFY_CODE_EXPIRED = 104
    const val CODE_VERIFY_CODE_INVALID = 105
    const val CODE_PICKUP_ADDRES_NOT_SET = 141
    const val CODE_MISSING_PARAMETERS = 301
    const val CODE_ACTION_NOT_ALLOWED = 302
    const val CODE_STORE_NOT_FOUND = 330
    const val CODE_CATEGORY_NOT_FOUND = 331
    const val CODE_MISSING_ATTRIBUTES = 332
    const val CODE_INVALID_ATTRIBUTES = 333
    const val CODE_UNAUTHORIZED = 401
    const val CODE_TECH_ISSUES = 402
    const val INVALID_TENANT_ID = 805
    const val API_TECH_ISSUES = 751
    const val INVALID_OR_MISSING_PARAMETERS = 752
    const val ACTION_NOT_ALLOWED = 753
    const val MULTI_SELLER_CART_SUPPORT = 480
    const val SUBSCRIPTION_NOT_ENABLED = 560

}