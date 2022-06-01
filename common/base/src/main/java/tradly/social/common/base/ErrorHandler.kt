package tradly.social.common.base

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import tradly.social.common.network.CustomError
import tradly.social.common.network.NetworkError
import tradly.social.common.resources.R
import tradly.social.domain.entities.AppError


class ErrorHandler {
    companion object {
        const val TRADLY_LOG = "tradly.log"
        fun handleError(ctx: Context? = null, exception: AppError?) {
            exception?.let {
                ActivityHelper.getCurrentActivityInstance()?.apply {
                    when (exception.code) {
                        CustomError.CODE_USER_ALREADY_EXISTS -> showToast(this,getString(R.string.signup_user_already_registered))
                        CustomError.CODE_INVALID_CREDENTIALS -> showToast(this,getString(R.string.login_invalid_credentials))
                        CustomError.CODE_USER_NOT_REGISTERED -> showToast(this,getString(R.string.login_no_user_found))
                        CustomError.CODE_VERIFY_CODE_INVALID -> showToast(this,getString(R.string.otp_invalid_otp))
                        CustomError.CODE_VERIFY_CODE_EXPIRED -> showToast(this,getString(R.string.otp_expired))
                        CustomError.INVALID_TENANT_ID -> showToast(this,getString(R.string.splash_invalid_tenant_id))
                        CustomError.ADD_GROUP_ERROR -> showToast(this,getString(R.string.group_could_not_add_group))
                        CustomError.DELETE_PRODUCT_ERROR -> showToast(this,getString(R.string.product_error_alert_product_delete))
                        CustomError.DELETE_CART_ERROR -> showToast(this,getString(R.string.cart_could_not_delete_cart_item))
                        CustomError.TIME_OUT_EXCEPTION -> showToast(this,getString(R.string.timeout_message))
                        CustomError.UNKNOWN_ERROR -> showToast(this,getString(R.string.something_went_wrong))
                        CustomError.SET_PASSWORD_FAILED -> showToast(this,getString(R.string.opt_could_not_set_password))
                        CustomError.UPDATE_STATUS_FAILED -> showToast(this,getString(R.string.something_went_wrong))
                        CustomError.CHAT_ROOM_FETCH_FAILED -> showToast(this,getString(R.string.chat_fetch_error_msg))
                        CustomError.STRIPE_CONNECT_FAILED -> showToast(this,getString(R.string.payments_stripe_connect_error_msg))
                        CustomError.CHECKOUT_FAILED -> showToast(this,getString(R.string.payment_not_able_to_checkout_msg))
                        CustomError.LOGOUT_FAILED -> showToast(this,getString(R.string.settings_something_while_logging_out))
                        CustomError.INIT_PAYMENT_FAILED -> showToast(this, String.format(getString(R.string.payments_init_failed),exception.message))
                        CustomError.STRIPE_PAYMENT_INTENT_FETCH_FAILED ->{}
                        CustomError.STRIPE_PAYMENT_FAILED ->{}
                        CustomError.CLEAR_CART_FAILED -> showToast(this,getString(R.string.product_alert_message_cart_clear_failed))
                        CustomError.REVIEW_SUBMIT_FAILED -> showToast(this,getString(R.string.addreview_review_error_message))
                        CustomError.STRIPE_DISCONNECT_FAILED -> showToast(this,getString(R.string.payments_stripe_disconnect_error_msg))
                        CustomError.GET_CATEGORY_LIST_FAILED -> showToast(this,getString(R.string.category_alert_message_category_list_fetch_failed))
                        CustomError.FEEDBACK_FAILED -> showToast(this,getString(R.string.feedback_alert_message_feedback_failed))
                        CustomError.STRIPE_STATUS_FAILED -> showToast(this,getString(R.string.payments_alert_message_stripe_status_failed))
                        CustomError.ACCOUNT_ACTIVATION -> showToast(this, String.format(getString(R.string.storedetail_alert_message_account_activate),getString(R.string.storedetail_active)))
                        CustomError.NOT_ABLE_TO_FETCH_ORDERS_ON_CANCEL_ORDER -> showToast(this,getString(R.string.alert_message_cancel_order_failed))
                        CustomError.ACCOUNT_DE_ACTIVATION -> showToast(this, String.format(getString(R.string.storedetail_alert_message_account_activate),getString(R.string.storedetail_inactive)))
                        CustomError.CODE_CATCH_PART ->{}
                        CustomError.BRANCH_URL_GENERATION_ERROR ->{
                            EventHelper.logEvent(EventHelper.Event.APP_ERROR, EventHelper.EventParam.BRANCH_IO_ERROR,exception.message)
                        }
                        else-> { }
                    }
                }

                printLog(exception.message)
            }
        }

        fun handleError(appError: AppError){
            ActivityHelper.getCurrentActivityInstance()?.let {
                when(appError.errorType){
                    NetworkError.NO_NETWORK-> showToast(it,it.getString(R.string.no_internet_warning_message))
                    NetworkError.NETWORK_ERROR -> showToast(it,it.getString(R.string.timeout_message))
                    NetworkError.API_ERROR-> showToast(it,appError.message)
                    NetworkError.API_UNKNOWN_ERROR,
                    NetworkError.UNKNOWN_ERROR-> showToast(it,it.getString(R.string.something_went_wrong))
                }
            }
        }
        private fun showToast(context: Context?,message:String?){
            context?.let {
                if(Looper.myLooper()!= Looper.getMainLooper()){
                    Handler(Looper.getMainLooper()).post{
                        Toast.makeText(context,message,Toast.LENGTH_SHORT).show()
                    }
                }
                else{
                    Toast.makeText(context,message,Toast.LENGTH_SHORT).show()
                }
            }
        }

        fun printLog(message:String?){
            message?.let {
                if(BuildConfig.DEBUG){
                    Log.e(TRADLY_LOG,message)
                }
            }
        }

        fun logAPIError(appError: AppError){
            EventHelper.logSentryEvent(appError.message.orEmpty(),appError.code,appError.url,appError.payload.orEmpty())
        }

        fun getErrorInfo(appError: AppError?) = appError?:AppError()
    }
}