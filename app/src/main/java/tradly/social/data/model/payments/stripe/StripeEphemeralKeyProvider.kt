package tradly.social.data.model.payments.stripe

import com.stripe.android.CustomerSession
import com.stripe.android.EphemeralKeyProvider
import com.stripe.android.EphemeralKeyUpdateListener
import tradly.social.common.base.AppController

class StripeEphemeralKeyProvider(private val ephemeralKey:String): EphemeralKeyProvider {

    override fun createEphemeralKey(apiVersion: String, keyUpdateListener: EphemeralKeyUpdateListener){
        keyUpdateListener.onKeyUpdate(ephemeralKey)
    }
}

class StripeApiVersionPrefetch(var callback:(apiVersion:String)->Unit){
    init {
        CustomerSession.initCustomerSession(AppController.appContext,object :EphemeralKeyProvider{
            override fun createEphemeralKey(
                apiVersion: String,
                keyUpdateListener: EphemeralKeyUpdateListener
            ) {
                callback(apiVersion)
            }
        })
    }
}