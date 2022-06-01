package tradly.social.common.network

class RequestID {
    companion object{
        const val NONE = 0
        const val GET_STRINGS = 1
        const val GET_APP_CONFIGS = 2
        const val GET_CURRENCY = 3
        const val GET_INTRO_CONFIG = 4
        const val GET_INTRO_RESOURCE = 5
        const val GET_EVENTS = 6
        const val GET_FILTERS = 7
        const val GET_CATEGORIES = 8
        const val GET_VARIANT_TYPES = 9
        const val UPLOAD_IMAGES = 10
        const val UPDATE_VARIANT = 11
        const val DELETE_VARIANT = 12
        const val GET_PAYMENT_TYPES = 13
        const val DIRECT_CHECKOUT = 14
        const val GET_BOOKING_EVENTS = 15
        const val GET_EVENT = 16
        const val ADD_VARIANT = 17
        const val GET_EPHEMERAL_KEY = 18
        const val GET_INTENT_SECRET = 19
        const val STRIPE_PAYMENT_RESULT = 20
        const val GET_BOOKING_DETAIL = 21
        const val GET_STORES = 22
        const val GET_PRODUCTS = 23
        const val FOLLOW_STORE = 24
        const val UN_FOLLOW_STORE = 25
        const val GET_SUBSCRIPTION_PRODUCTS = 26
        const val CONNECT_TO_BILLING_CLIENT = 27
        const val CONFIRM_SUBSCRIPTION = 28
        const val GET_ACCOUNT_FOLLOWERS = 29
    }
}