package tradly.social.common.network;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public abstract class APIRequest {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({RequestID.NONE,
            RequestID.GET_STRINGS,
            RequestID.GET_APP_CONFIGS,
            RequestID.GET_CURRENCY,
            RequestID.GET_INTRO_CONFIG,
            RequestID.GET_INTRO_RESOURCE,
            RequestID.GET_EVENTS,
            RequestID.GET_VARIANT_TYPES,
            RequestID.UPLOAD_IMAGES,
            RequestID.DELETE_VARIANT,
            RequestID.UPDATE_VARIANT,
            RequestID.GET_PAYMENT_TYPES,
            RequestID.DIRECT_CHECKOUT,
            RequestID.GET_CATEGORIES,
            RequestID.GET_FILTERS,
            RequestID.GET_BOOKING_EVENTS,
            RequestID.GET_EVENT,
            RequestID.ADD_VARIANT,
            RequestID.GET_EPHEMERAL_KEY,
            RequestID.GET_INTENT_SECRET,
            RequestID.GET_BOOKING_DETAIL,
            RequestID.GET_STORES,
            RequestID.GET_PRODUCTS,
            RequestID.FOLLOW_STORE,
            RequestID.UN_FOLLOW_STORE,
            RequestID.GET_SUBSCRIPTION_PRODUCTS,
            RequestID.CONNECT_TO_BILLING_CLIENT,
            RequestID.CONFIRM_SUBSCRIPTION,
            RequestID.GET_ACCOUNT_FOLLOWERS
    })
    public @interface ID {
    }
}
