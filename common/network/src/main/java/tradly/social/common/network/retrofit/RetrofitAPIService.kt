package tradly.social.common.network.retrofit

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*
import tradly.social.common.network.entity.*
import tradly.social.common.network.APIEndPoints

interface RetrofitAPIService {

    @GET(APIEndPoints.TENANT_CONFIG)
    fun getParseConfig(@Path("tenantKey") tenantKey: String): Call<AppConfigResponse>

    @JvmSuppressWildcards
    @POST(APIEndPoints.APP_LOGIN)
    fun login(@Body map: Map<String, Any?>): Call<ResponseBody>

    @JvmSuppressWildcards
    @POST(APIEndPoints.VERIFY_USER)
    fun verifyUser(@Body map: Map<String, Any?>): Call<UserDetailResponse>

    @JvmSuppressWildcards
    @POST(APIEndPoints.APP_SIGN_UP)
    fun signUp(@Body map: Map<String, Any?>): Call<VerificationResponse>

    @JvmSuppressWildcards
    @POST(APIEndPoints.APP_SIGN_UP)
    fun signUpNoOTP(@Body map: Map<String, Any?>): Call<UserDetailResponse>

    @JvmSuppressWildcards
    @POST(APIEndPoints.SOCIAL_LOGIN)
    fun socialLogin(@Body map: Map<String, Any>): Call<UserDetailResponse>

    @GET(APIEndPoints.GET_COUNTRIES)
    fun getCountries(): Call<CountryResponse>

    @JvmSuppressWildcards
    @GET(APIEndPoints.GET_HOME)
    fun getHome(@QueryMap map: Map<String,Any?>): Call<HomeResponse>


    @Headers(
        tradly.social.common.network.Header.CONTENT_TYPE.plus(":").plus(
            tradly.social.common.network.Header.CONTENT_TYPE_JSON
        ))
    @GET(APIEndPoints.REFRESH_TOKEN)
    fun refreshToken(): Call<UserDetailResponse>

    @GET(APIEndPoints.GET_ADDRESS)
    fun getAddressByLocation(@Path("lat") lat: String, @Path("lng") lng: String): Call<AddressResponse>

    @JvmSuppressWildcards
    @PATCH(APIEndPoints.UPDATE_PROFILE)
    fun updateUserDetail(@Path("userId") userId:String, @Body map: Map<String, Any?>): Call<UserDetailResponse>

    @GET(APIEndPoints.UPDATE_PROFILE)
    fun getUserDetail(@Path("userId") userId:String): Call<UserDetailResponse>


    @GET(APIEndPoints.GET_GROUP_TYPES)
    fun getGroupTypes(): Call<GroupResponse>

    @JvmSuppressWildcards
    @GET(APIEndPoints.SEARCH_TAG)
    fun searchTag(@QueryMap map:Map<String,Any?>):Call<TagResponse>

    @JvmSuppressWildcards
    @POST(APIEndPoints.ADD_GROUP)
    fun addGroup(@Body map: Map<String, Any?>): Call<GroupDetailResponse>

    @GET(APIEndPoints.GET_CURRENCIES)
    fun getCurrencies():Call<CurrencyResponse>

    @GET(APIEndPoints.CART_LIST)
    fun getCartList(@Query("shipping_method_id") shippingMethodId:String?):Call<CartResponse>

    @JvmSuppressWildcards
    @POST(APIEndPoints.CART_LIST)
    fun addToCart(@Body map:Map<String,Any?>):Call<ResponseStatus>

    @JvmSuppressWildcards
    @HTTP(method = "PATCH" , path = APIEndPoints.CART_LIST, hasBody = true)
    fun deleteCart(@Body map:Map<String,Any?>):Call<ResponseStatus>

    @JvmSuppressWildcards
    @GET(APIEndPoints.ADDRESS_LIST)
    fun getAddressList(@Query("type") type:String):Call<ShippingAddressResponse>

    @JvmSuppressWildcards
    @POST(APIEndPoints.ADDRESS_LIST)
    fun addShippingAddress(@Body map:Map<String,Any?>):Call<ShippingAddressResponse>

    @JvmSuppressWildcards
    @PUT(APIEndPoints.UPDATE_ADDRESS)
    fun updateShippingAddress(@Body map:Map<String,Any?>,@Path("addressId")addressId:String):Call<ShippingAddressResponse>

    @GET(APIEndPoints.GET_PAYMENT_METHODS)
    fun getPaymentTypes():Call<PaymentTypeResponse>

    @JvmSuppressWildcards
    @POST(APIEndPoints.PASSWORD_RECOVERY)
    fun doPasswordRecovery(@Body map: Map<String, Any?>):Call<VerificationResponse>

    @JvmSuppressWildcards
    @POST(APIEndPoints.PASSWORD_SET)
    fun passwordReset(@Body map: Map<String, Any?>):Call<ResponseStatus>

    @GET(APIEndPoints.PAYOUT_CONNECT_OAUTH)
    fun connectOAuth():Call<PayoutResponse>

    @DELETE(APIEndPoints.PAYOUT_CONNECT_OAUTH)
    fun disconnectOAuth():Call<ResponseStatus>

    @GET
    fun verifyStripeOAuth(@Url url:String):Call<ResponseStatus>

    @JvmSuppressWildcards
    @POST(APIEndPoints.STRIPE_ACCOUNT_LINK)
    fun getAccountLink(@Body map: Map<String, Any?>):Call<PayoutResponse>

    @GET(APIEndPoints.STRIPE_CONNECT_STATUS)
    fun getStripeStatus(@Query("account_id") accountId:String):Call<PayoutResponse>

    @JvmSuppressWildcards
    @POST(APIEndPoints.STRIPE_LOGIN_LINK)
    fun getStripeLoginLink(@Body map:Map<String,Any?>):Call<PayoutResponse>

    @GET(APIEndPoints.APP_CONFIG)
    fun getAppConfig(@Query("key") key:String):Call<ResponseBody>

    @GET(APIEndPoints.APP_CONFIG)
    fun getAppGroupConfig(@Query("key_group") key:String):Call<ResponseBody>





    @GET(APIEndPoints.ACTIVITIES)
    fun getNotifications(@Query("page") page:Int):Call<ActivitiesResponse>

    @GET(APIEndPoints.EARNINGS)
    fun getEarnings(@Query("account_id") accountId:String):Call<EarningResponse>

    @JvmSuppressWildcards
    @GET(APIEndPoints.TRANSACTION)
    fun getTransactions(@QueryMap map:Map<String,Any?>):Call<TransactionResponse>

    @GET(APIEndPoints.SEARCH_LOCATION)
    fun searchLocation(@Query("key") key:String):Call<AddressResponse>

    @JvmSuppressWildcards
    @POST(APIEndPoints.LOGOUT)
    fun logout(@Body map:Map<String,Any?>):Call<ResponseStatus>

    @JvmSuppressWildcards
    @GET(APIEndPoints.REVIEW_LIST)
    fun getReviewList(@QueryMap map:Map<String,Any?>):Call<ReviewListResponse>

    @JvmSuppressWildcards
    @POST(APIEndPoints.ADD_REVIEW)
    fun addReview(@Body map:Map<String,Any?>):Call<ResponseStatus>

    @GET(APIEndPoints.SHIPPING_METHODS)
    fun getShippingMethods():Call<ShippingMethodResponse>

    @GET(APIEndPoints.SHIPPING_METHODS_BY_ACCOUNT)
    fun getShippingMethodsByAccount(@Query("account_id") accountId: String?):Call<ShippingMethodResponse>


    @DELETE(APIEndPoints.CLEAR_CART)
    fun clearCart():Call<ResponseStatus>

    @JvmSuppressWildcards
    @PATCH(APIEndPoints.UPDATE_PICKUP_ADDRESS)
    fun updatePickupAddress(@Path("orderId")  orderId:String,@Body map: Map<String, Any?>):Call<ResponseStatus>

    @JvmSuppressWildcards
    @PATCH(APIEndPoints.LIKE_REVIEW)
    fun likeReview(@Path("reviewId") reviewId:String, @Body map: Map<String, Any?>):Call<ResponseStatus>

    @GET(APIEndPoints.FEEDBACK_CATEGORY)
    fun getFeedbackCategories():Call<FeedbackCategoryResponse>

    @JvmSuppressWildcards
    @POST(APIEndPoints.SEND_FEEDBACK)
    fun sendFeedback( @Body map:Map<String,Any?>):Call<ResponseStatus>



    @JvmSuppressWildcards
    @POST(APIEndPoints.MAKE_NEGOTIATION)
    fun makeNegotiation(@Path("productId") productId:String,@Body map: Map<String, Any?>):Call<NegotiationResponse>

    @JvmSuppressWildcards
    @PATCH(APIEndPoints.UPDATE_NEGOTIATION)
    fun updateNegotiation(@Path("productId") productId: String, @Path("negotiationId") negotiationId:String , @Body map: Map<String, Any?>):Call<ResponseStatus>

}