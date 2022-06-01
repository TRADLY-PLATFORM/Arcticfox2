package tradly.social.data.model.payments.stripe

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.stripe.android.*
import com.stripe.android.model.ConfirmPaymentIntentParams
import com.stripe.android.model.PaymentMethod
import tradly.social.R
import tradly.social.common.base.AppController
import tradly.social.common.base.ErrorHandler
import tradly.social.common.network.CustomError
import tradly.social.domain.entities.AppError
import tradly.social.common.base.BaseActivity
import tradly.social.ui.base.StripeView
import tradly.social.ui.payment.PaymentActivity

class StripeHelper(var ctx: Context,val view: StripeView?){

    interface SecretKeyListener{
        fun getSecretKey(paymentId: String,orderReference: String)
    }

    private lateinit var paymentSession:PaymentSession
    private lateinit var stripe:Stripe

    fun initStripe(pubKey:String){
        PaymentConfiguration.init(AppController.appContext,pubKey)
        stripe = Stripe(ctx,pubKey)
    }

    fun initCustomerSession(ephemeralKey:String) = CustomerSession.initCustomerSession(ctx,StripeEphemeralKeyProvider(ephemeralKey))

    fun initPaymentSession(orderReference:String,secretKeyListener: SecretKeyListener){
        val sessionBuilder = PaymentSessionConfig.Builder()
            .setShippingInfoRequired(false)
            .setShippingMethodsRequired(false)
            .setShouldPrefetchCustomer(false)
            .setPaymentMethodTypes(listOf(PaymentMethod.Type.Card))
            .build()

        paymentSession = PaymentSession(ctx as PaymentActivity,sessionBuilder)
        paymentSession.init(object :PaymentSession.PaymentSessionListener{
            override fun onCommunicatingStateChanged(isCommunicating: Boolean) {
                if(isCommunicating){
                    view?.showProgressDialog(R.string.please_wait)
                }
                else{
                    view?.hideProgressDialog()
                }
            }

            override fun onError(errorCode: Int, errorMessage: String) {
                ErrorHandler.handleError(exception = AppError(message =errorMessage))
            }

            override fun onPaymentSessionDataChanged(data: PaymentSessionData) {
                if(data.isPaymentReadyToCharge){
                    data.paymentMethod?.id?.let {
                        secretKeyListener.getSecretKey(it,orderReference)
                    }
                }
            }

        })
        paymentSession.presentPaymentMethodSelection()
    }

    fun confirmPayment(paymentId:String,secret:String){
        stripe.confirmPayment(ctx as BaseActivity, ConfirmPaymentIntentParams.createWithPaymentMethodId(paymentId,secret))
    }

    fun confirmPayment(activity:Activity?,paymentId:String,secret:String){
        activity?.let {
            stripe.confirmPayment(activity, ConfirmPaymentIntentParams.createWithPaymentMethodId(paymentId,secret))
        }
    }


    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent){
        if(::paymentSession.isInitialized){
            paymentSession.handlePaymentData(requestCode,resultCode,data)
            stripe.onPaymentResult(requestCode,data,object :ApiResultCallback<PaymentIntentResult>{
                override fun onSuccess(result: PaymentIntentResult) {
                    result.intent.status?.code?.let {
                        if("succeeded"==it){
                            view?.hideProgressDialog()
                            paymentSession.onCompleted()
                            view?.onCheckOutStatus(isSuccess = true)
                        }
                        else{
                            ErrorHandler.handleError(exception = AppError(code = CustomError.STRIPE_PAYMENT_FAILED))
                            view?.hideProgressDialog()
                        }
                    }
                }

                override fun onError(e: Exception) {
                    ErrorHandler.handleError(exception = AppError(code = CustomError.STRIPE_PAYMENT_FAILED))
                    view?.hideProgressDialog()
                }
            })
        }
    }

    companion object{
        fun endCustomerSession() = CustomerSession.endCustomerSession()
    }
}