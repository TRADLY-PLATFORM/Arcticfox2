package tradly.social.common.network.retrofit

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*
import tradly.social.common.network.APIEndPoints
import tradly.social.common.network.entity.*
import tradly.social.common.network.feature.addEvent.datasource.model.VariantResponse
import tradly.social.common.network.feature.eventbooking.model.BookingDetailResponse
import tradly.social.common.network.feature.eventbooking.model.BookingResponse
import tradly.social.common.network.feature.exploreEvent.model.EventDetailResponse
import tradly.social.common.network.feature.exploreEvent.model.EventListResponse
import tradly.social.common.network.feature.subscription.model.SubscriptionProductResponse

interface EventAPI{
    @JvmSuppressWildcards
    @GET(APIEndPoints.GET_PRODUCTS)
    fun getEvents(@QueryMap map: Map<String, Any?>):Call<EventListResponse>

    @GET(APIEndPoints.GET_PRODUCT)
    fun getEvent(@Path("productId") productId:String): Call<ResponseBody>
}

interface CategoryAPI{
    @GET(APIEndPoints.GET_CATEGORIES)
    fun getCategories(@Query("parent") categoryId: String,@Query("type") type:String): Call<CategoryResponse>
}

interface DeviceAPI{
    @JvmSuppressWildcards
    @PUT(APIEndPoints.UPDATE_DEVICE)
    fun updateDeviceDetails(@Body map:Map<String,Any?>):Call<ResponseStatus>
}

interface VariantAPI{

    @JvmSuppressWildcards
    @POST(APIEndPoints.ADD_VARIANT)
    fun addVariant(@Body map: Map<String, Any?>,@Path("listingId") listingId:String): Call<VariantResponse>

    @GET(APIEndPoints.GET_VARIANT_TYPES)
    fun getVariantTypes():Call<VariantTypeResponse>

    @DELETE(APIEndPoints.DELETE_VARIANT)
    fun deleteVariant(@Path("listingId") listingId:String,@Path("variantId") variantId:String):Call<ResponseStatus>

    @JvmSuppressWildcards
    @PUT(APIEndPoints.UPDATE_VARIANT)
    fun updateVariant(@Body map:Map<String,Any?>,@Path("listingId") listingId:String,@Path("variantId") variantId:String):Call<ResponseStatus>
}

interface StoreAPI{

    @JvmSuppressWildcards
    @POST(APIEndPoints.ADD_ACCOUNT)
    fun addStore(@Body map: Map<String, Any?>): Call<StoreResponse>

    @JvmSuppressWildcards
    @PUT(APIEndPoints.EDIT_ACCOUNT)
    fun editStore(@Path("account_id") accountId:String,@Body map: Map<String, Any?>):Call<StoreResponse>

    @JvmSuppressWildcards
    @PATCH(APIEndPoints.ACCOUNT_ACTIVATE)
    fun accountActivation(@Path("account_id") accountId: String,@Body map:Map<String,Any?>):Call<ResponseStatus>

    @JvmSuppressWildcards
    @GET(APIEndPoints.GET_STORES_FEEDS)
    fun getStoreFeeds(@Path("sub_type") subtype: String,@Query("page") page: Int,@Query("type") type:String):Call<StoreResponse>

    @POST(APIEndPoints.FOLLOW_STORE)
    fun followStore(@Path("storeId") storeId: String?): Call<ResponseStatus>

    @DELETE(APIEndPoints.FOLLOW_STORE)
    fun unFollowStore(@Path("storeId") storeId: String?): Call<ResponseStatus>

    @JvmSuppressWildcards
    @GET(APIEndPoints.GET_STORE_LIST)
    fun syncUserStore(@QueryMap map: Map<String,Any?>): Call<StoreResponse>

    @GET(APIEndPoints.GET_STORE)
    fun getStore(@Path("id") id: String): Call<StoreResponse>

    @POST(APIEndPoints.BLOCK_STORE)
    fun blockStore(@Path("store_id") storeId: String):Call<ResponseStatus>


    @DELETE(APIEndPoints.BLOCK_STORE)
    fun unBlockStore(@Path("store_id") storeId: String):Call<ResponseStatus>

    @GET(APIEndPoints.GET_ACCOUNT_FOLLOWERS)
    fun getAccountFollowers(@Path("account_id") accountId: String,@Query("page")page: Int):Call<AccountFollowersResponse>

}

interface MediaAPI{
    @JvmSuppressWildcards
    @POST(APIEndPoints.SIGNED_URL)
    fun getSignedUrl(@Body map: Map<String, Any?>): Call<SignedURLResponse>

    @PUT
    fun uploadMedia(@Url url: String, @HeaderMap map: Map<String, String?>, @Body body: RequestBody): Call<Void>
}

interface PaymentAPI{
    @GET(APIEndPoints.GET_PAYMENT_METHODS)
    fun getPaymentTypes():Call<PaymentTypeResponse>

    @JvmSuppressWildcards
    @POST(APIEndPoints.EPHEMERAL_KEY)
    fun getEphemeralKey(@Body map:Map<String,Any?>):Call<ResponseBody>

    @JvmSuppressWildcards
    @POST(APIEndPoints.STRIPE_PAYMENT_INTENT)
    fun getPaymentIntent(@Body map:Map<String,Any?>):Call<ResponseBody>

    @JvmSuppressWildcards
    @POST(APIEndPoints.TRIGGER_EMAIL_SUBSCRIBE)
    fun subscription(@Body map:Map<String,Any>):Call<ResponseStatus>
}

interface OrderAPI{

    @JvmSuppressWildcards
    @GET(APIEndPoints.GET_ORDER_DETAILS)
    fun getOrderDetails(@Path("orderId") orderId: String,@Query("account_id") accountId: String?): Call<OrderDetailResponse>

    @JvmSuppressWildcards
    @GET(APIEndPoints.GET_ORDERS)
    fun getOrders(@QueryMap map:Map<String,Any?>): Call<OrderResponse>

    @JvmSuppressWildcards
    @PATCH(APIEndPoints.STATUS_UPDATE)
    fun updateStatus(@Path("order_id")orderId: String,@Body map:Map<String,Any?>):Call<ResponseStatus>

    @JvmSuppressWildcards
    @POST(APIEndPoints.CHECK_OUT_CART)
    fun checkOutCart(@Body map:Map<String,Any?>):Call<CheckoutResponse>
}

interface EventBookingAPI{
    @JvmSuppressWildcards
    @POST(APIEndPoints.DIRECT_CHECKOUT)
    fun checkOutCart(@Body map:Map<String,Any?>,@Path("eventId") eventId:String):Call<CheckoutResponse>

    @JvmSuppressWildcards
    @GET(APIEndPoints.GET_EVENT_BOOKINGS)
    fun getEventBookings(@QueryMap map:Map<String,Any?>): Call<BookingResponse>

    @JvmSuppressWildcards
    @GET(APIEndPoints.GET_ORDER_DETAILS)
    fun getBookingDetail(@Path("orderId") orderId: String,@Query("account_id") accountId: String?): Call<BookingDetailResponse>

}

interface ProductAPI{

    @JvmSuppressWildcards
    @POST(APIEndPoints.ADD_PRODUCT)
    fun addProduct(@Body map: Map<String, Any?>): Call<ProductDetailResponse>

    @JvmSuppressWildcards
    @PUT(APIEndPoints.EDIT_PRODUCT)
    fun editProduct(@Body map: Map<String, Any?>,@Path("productId") productId:String?): Call<ProductDetailResponse>

    @JvmSuppressWildcards
    @POST(APIEndPoints.ADD_VARIANTS)
    fun addVariants(@Body map: Map<String, Any?>,@Path("listingId") listingId:String): Call<ResponseStatus>

    @JvmSuppressWildcards
    @PATCH(APIEndPoints.EDIT_PRODUCT)
    fun updateProduct(@Path("productId") productId: String, @Body map: Map<String, Any?>): Call<ProductDetailResponse>

    @DELETE(APIEndPoints.DELETE_LISTING)
    fun deleteProduct(@Path("productId") productId: String):Call<ResponseStatus>

    @JvmSuppressWildcards
    @GET(APIEndPoints.GET_PRODUCTS)
    fun getProducts(@QueryMap map: Map<String, Any?>): Call<ProductListResponse>


    @JvmSuppressWildcards
    @GET(APIEndPoints.GET_SIMILAR_PRODUCTS)
    fun getSimilarProducts(@Path("productId") productId: String, @Query("page") pagination: Int, @Query("per_page") perPage:Int):Call<ProductListResponse>

    @GET(APIEndPoints.GET_PRODUCT)
    fun getProduct(@Path("productId") productId: String?, @Query("locale") locale: String): Call<ResponseBody>

    @POST(APIEndPoints.MARK_AS_SOLD)
    fun markAsSold(@Path("productId") productId: String):Call<ResponseStatus>

    @JvmSuppressWildcards
    @GET(APIEndPoints.GET_ATTRIBUTES)
    fun getAttribute(@QueryMap map: Map<String, Any?>): Call<ResponseBody>

    @JvmSuppressWildcards
    @GET(APIEndPoints.SOCIAL_FEEDS)
    fun getSocialFeeds(@QueryMap map: Map<String, Any?>): Call<ProductListResponse>

    @JvmSuppressWildcards
    @GET(APIEndPoints.GET_WISH_LIST)
    fun getWishList(@QueryMap map:Map<String,Any?>):Call<ProductListResponse>

    @JvmSuppressWildcards
    @POST(APIEndPoints.LIKE_PRODUCT)
    fun likeProduct(@Path("productId") productId:String):Call<ResponseStatus>

    @DELETE(APIEndPoints.DIS_LIKE_PRODUCT)
    fun disLikeProduct(@Path("productId") productId: String):Call<ResponseStatus>

}

interface SubscriptionAPI{

    @JvmSuppressWildcards
    @GET(APIEndPoints.GET_SUBSCRIPTION_PRODUCTS)
    fun getSubscriptionProducts(@QueryMap queryParam:HashMap<String,Any?>):Call<SubscriptionProductResponse>

    @JvmSuppressWildcards
    @POST(APIEndPoints.CONFIRM_SUBSCRIPTION_PRODUCTS)
    fun confirmSubscriptionProduct(@Body queryParam: Map<String, Any?>):Call<ResponseStatus>

}

