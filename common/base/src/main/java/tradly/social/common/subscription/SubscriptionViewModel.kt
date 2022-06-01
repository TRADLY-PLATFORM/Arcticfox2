package tradly.social.common.subscription
import android.app.Activity
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.*
import kotlinx.coroutines.launch
import tradly.social.common.base.AppController
import tradly.social.common.base.BaseViewModel
import tradly.social.common.base.SingleEvent
import tradly.social.common.base.UIState
import tradly.social.common.cache.AppCache
import tradly.social.common.cache.CurrencyCache
import tradly.social.common.network.NetworkError
import tradly.social.common.network.RequestID
import tradly.social.common.network.feature.subscription.datasource.SubscriptionDataSourceImpl
import tradly.social.data.model.CoroutinesManager
import tradly.social.domain.entities.AppError
import tradly.social.domain.entities.Currency
import tradly.social.domain.entities.Store
import tradly.social.domain.entities.SubscriptionProduct
import tradly.social.domain.repository.SubscriptionRepository
import tradly.social.domain.usecases.ConfirmSubscriptionProductUc
import tradly.social.domain.usecases.GetSubscriptionProducts
import java.util.*

class SubscriptionViewModel:BaseViewModel(),PurchasesUpdatedListener {

    private val subscriptionRepository by lazy { SubscriptionRepository(SubscriptionDataSourceImpl()) }
    private val getSubscriptionProducts = GetSubscriptionProducts(subscriptionRepository)
    private val confirmSubscriptionUc = ConfirmSubscriptionProductUc(subscriptionRepository)
    private val _productList:MutableLiveData<List<SubscriptionProduct>> by lazy { MutableLiveData() }
    val productList:LiveData<List<SubscriptionProduct>> = _productList
    private val _onBillingClientConnectLiveData:MutableLiveData<SingleEvent<Unit>> by lazy { MutableLiveData() }
    val onBillingClientConnectLiveData:LiveData<SingleEvent<Unit>> = _onBillingClientConnectLiveData
    private val _skuDetailList:MutableLiveData<List<SkuDetails>> by lazy { MutableLiveData() }
    val skuDetailList:LiveData<List<SkuDetails>> = _skuDetailList
    private val _purchaseSuccessLiveData:MutableLiveData<SingleEvent<Unit>> by lazy { MutableLiveData() }
    val purchaseSuccessLiveData:LiveData<SingleEvent<Unit>> = _purchaseSuccessLiveData
    lateinit var billingClient:BillingClient
    private val store:Store = AppCache.getUserStore()!!
    private var purchaseProduct:SubscriptionProduct?=null

    private fun buildBillingClient(context:Context): BillingClient =
        BillingClient.newBuilder(context).enablePendingPurchases()
            .setListener(this).build()

    fun connectToBillingClient(context:Context){
        showAPIProgress(true,RequestID.CONNECT_TO_BILLING_CLIENT)
        billingClient = buildBillingClient(context)
        billingClient.startConnection(billingStatusListener)
    }

    fun getPurchaseList(){
        billingClient.queryPurchasesAsync(BillingClient.SkuType.SUBS,object:PurchasesResponseListener{
            override fun onQueryPurchasesResponse(p0: BillingResult, p1: MutableList<Purchase>) {
                val s = ""
            }

        })
    }

    fun getProductList(){
        getApiResult(viewModelScope,{getSubscriptionProducts.getSubscriptionProducts(store.id.toInt())},{ result->
           _productList.value = result
        },true,RequestID.GET_SUBSCRIPTION_PRODUCTS)
    }


    fun getSubscriptionProductList(list: List<SubscriptionProduct>){
        CoroutinesManager.ioThenMain(viewModelScope,{querySkuDetails(list)},{ skuDetailsResult->
            if (skuDetailsResult.billingResult.responseCode == BillingClient.BillingResponseCode.OK){
                _skuDetailList.value = skuDetailsResult.skuDetailsList
            }
        })
    }

    private suspend fun querySkuDetails(list: List<SubscriptionProduct>):SkuDetailsResult{
        val skuList = list.map { it.sku.toLowerCase(Locale.ENGLISH) }
        val params = SkuDetailsParams.newBuilder().setType(BillingClient.SkuType.SUBS).setSkusList(
            skuList).build()
        return billingClient.querySkuDetails(params)
    }

    fun launchBillingFlow(activity:Activity,subscriptionProduct: SubscriptionProduct){
        this.purchaseProduct = subscriptionProduct
        _skuDetailList.value?.find { it.sku == subscriptionProduct.sku.toLowerCase() }?.let { skuDetails ->
            val param = BillingFlowParams.newBuilder().setSkuDetails(skuDetails).build()
            val billingResult = billingClient.launchBillingFlow(activity,param)
            if (billingResult.responseCode != BillingClient.BillingResponseCode.OK){
                // show Error message
            }
        }
    }

    private val billingStatusListener = object :BillingClientStateListener{
        override fun onBillingSetupFinished(billingResult: BillingResult) {
            if (billingResult.responseCode ==  BillingClient.BillingResponseCode.OK) {
                _onBillingClientConnectLiveData.postValue(SingleEvent(Unit))
            }
        }

        override fun onBillingServiceDisconnected() {
            showAPIProgress(false,RequestID.CONNECT_TO_BILLING_CLIENT)
            onFailure(AppError(errorType = NetworkError.UNKNOWN_ERROR))
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        if(billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null){
            handlePurchases(purchases)
        }
        else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            // Handle an error caused by a user cancelling the purchase flow.
        } else {
            // Handle any other error codes.
        }
    }

    private fun handlePurchases(purchases: MutableList<Purchase>){
        purchases.getOrNull(0)?.let {
            if (it.purchaseState == Purchase.PurchaseState.PURCHASED){
                confirmPurchase(it.purchaseToken)
            }
        }
    }

    private fun confirmPurchase(purchaseToken:String){
        getApiResult(viewModelScope,{confirmSubscriptionUc.confirmSubscription(store.id.toInt(),purchaseProduct!!.id,purchaseToken)},{ result->
            getProductList()
            _purchaseSuccessLiveData.value = SingleEvent(Unit)
        },true,RequestID.CONFIRM_SUBSCRIPTION)
    }

    override fun showAPIProgress(show: Boolean, apiId: Int) {
        _uiState.value = SingleEvent(UIState.Loading(show,apiId))
    }

    override fun onFailure(apiError: AppError) {
        showAPIProgress(false,apiError.apiId)
        _uiState.value = SingleEvent(UIState.Failure(apiError))
    }
}