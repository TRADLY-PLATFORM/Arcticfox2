package tradly.social.common.network

class Header {
    companion object{
        const val CONTENT_TYPE = "Content-Type"
        const val THUMBNAIL_IMAGE = "x-goog-meta-thumbnail"
        const val TENANT_KEY = "tenant_key"
        const val AUTHORIZATION = "Authorization"
        const val BEARER = "Bearer " // <- don't remove space at last
        const val AUTH_KEY = "X-Auth-Key"
        const val REFRESH_KEY = "X-Refresh-Key"
        const val LANGUAGE = "X-Language"
        const val CURRENCY = "X-Currency"
        const val TRADLY_USER_AGENT = "x-tradly-agent"
        const val API_ID = "x-api-id"

        //headers value
        const val CONTENT_TYPE_JSON = "application/json"
        const val TRADLY_AGENT_VALUE = "2"
        const val API_ID_VALUE = "2"
    }
}