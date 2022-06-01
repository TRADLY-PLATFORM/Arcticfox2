package tradly.social.common.network.parseHelper

class ParseConstant {
    companion object {

        const val PIN_COUNTRY = "COUNTRY"
        const val CREATED_AT = "createdAt"
        //device detail
        const val DEVICE_SOURCE = "device_source"
        const val DEVICE_ID = "device_id"
        const val FCM_TOKEN = "fcm_token"
        const val DEVICE_NAME = "device_name"
        const val APP_VERSION = "app_version"
        const val OS_VERSION = "os_version"

        //user
        const val EMAIL_VERIFIED: String = "emailVerified"
        const val ACTIVE: String = "active"
        const val NAME: String = "name"
        const val PROFILE_PIC: String = "profile_pic"
        const val USERNAME: String = "username"
        const val PASSWORD: String = "password"
        const val EMAIL = "email"
        const val USER_TYPE = "user_type"
        const val MOBILE = "mobile"
        const val COUNTRY = "country"
        const val CITY = "city"
        const val USER = "user"
        const val WEB_ADDRESS = "web_address"
        const val LOCATION = "location"
        const val STORE_ADDRESS = "store_address"
        const val TYPE = "type"
        const val DESCRIPTION = "description"
        const val NEIGHBOURHOOD = "neighbourhood"
        const val FOLLOWERS_COUNT = "followers_count"
        const val LISTINGS_COUNT = "listings_count"
        const val DEVICE_INFO = "device_info"


        //country
        const val CODE2: String = "code2"
        const val CODE3: String = "code3"
        const val CURRENCY_LOCALE = "currency_locale"
        const val FLAG: String = "flag"
        const val MOBILE_NUMBER_REGEX: String = "mobile_number_regex"
        const val CURRENCY: String = "currency"
        const val LOCALE: String = "locale"
        const val DIAL_CODE = "dial_code"
        const val MOBILE_NUMBER_LENGTH = "mobile_number_length"

        //promoBanners
        const val PROMO_TYPE = "type"
        const val CONTENT_ID = "content_id"

        //category
        const val ORDER_BY = "order_by"
        const val TRANSLATION = "translations"
        const val IMAGE_PATH = "image_path"
        const val PARENT = "parent"

        //category_translation
        const val TEXT = "text"
        const val CATEGORY = "category"

        //brands
        const val STORE_TYPE = "store_type"
        //brand_followers
        const val BRAND = "brand"
        //group
        const val CREATED_BY = "created_by"
        const val MEMBERS_COUNT = "members_count"
        const val ARCHEIVED = "archived"
        const val BLOCKED = "blocked"
        const val GROUP_ADDRESS = "groupAddress"
        const val LISTINGS = "listings"
        const val LISTING = "listing"
        const val GROUP_TYPE = "group_type"
        const val SET = "set"
        const val VARIANT = "variant"

        //groupType
        const val GROUP_TYPE_IMAGE = "groupTypeImage"
        //group followers
        const val GROUP = "group"
        //store
        const val STORE = "store"
        // shipping address
        const val PHONE_NUMBER = "phone_number"
        const val STREET = "street"
        const val ZIP_CODE = "zip_code"
        const val PROVINCE = "province"
        const val LANDMARK = "landmark"
        const val STATE = "state"
        const val DEFAULT_ADDRESS = "default_address"

        //cart details
        const val ID_ = "id_"
        const val QUANTITY = "quantity"
        //Product
        const val TITLE = "title"
        const val OFFER_PERCENT = "offer_percent"
        const val OFFER_PRICE = "offer_price"
        const val IMAGES = "images"
        const val PRICE_TYPE = "price_type"
        const val DELIVERY_DETAILS = "delivery_details"
        const val CONDITION = "condition"
        const val LIST_PRICE = "list_price"
    }


    object UserType {
        const val ADMIN = 1
        const val BUYER = 2
        const val SELLER = 3
    }

    object AuthType{
        const val EMAIL = 1
        const val MOBILE = 2
        const val MOBILE_PASSWORD = 3
        const val MOBILE_PASSWORD_NO_OTP = 5
    }

    object HOME_SCOPE{
        const val STORE  =1
        const val GROUP = 2
        const val INVITE_FRIENDS = 3
        const val FEATURED_PRODUCTS = 4
        const val TRENDING_PRODUCTS =5
    }
}