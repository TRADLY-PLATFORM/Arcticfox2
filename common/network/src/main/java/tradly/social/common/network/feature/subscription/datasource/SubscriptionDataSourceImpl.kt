package tradly.social.common.network.feature.subscription.datasource

import tradly.social.common.network.NetworkConstant
import tradly.social.common.network.NetworkError
import tradly.social.common.network.base.BaseService
import tradly.social.common.network.converters.SubscriptionConverter
import tradly.social.common.network.retrofit.SubscriptionAPI
import tradly.social.domain.dataSource.SubscriptionDataSource
import tradly.social.domain.entities.AppError
import tradly.social.domain.entities.Result
import tradly.social.domain.entities.SubscriptionProduct

class SubscriptionDataSourceImpl:SubscriptionDataSource,BaseService() {

    val apiService = getRetrofitService(SubscriptionAPI::class.java)

    override suspend fun getSubscriptionProducts(accountId: Int): Result<List<SubscriptionProduct>> {
       return when(val result = apiCall(apiService.getSubscriptionProducts(hashMapOf(NetworkConstant.QueryParam.ACCOUNT_ID to accountId)))){
            is Result.Success->{
                if (result.data.status){
                    Result.Success(SubscriptionConverter.mapFromList(result.data.data.subscriptionProducts))
                }
                else{
                    Result.Error(AppError(errorType = NetworkError.API_UNKNOWN_ERROR))
                }
            }
            is Result.Error-> result
        }
    }

    override suspend fun confirmSubscription(
        accountId: Int,
        subscriptionProductId: Int,
        purchaseToken: String
    ):Result<Boolean> {
        return when(val result = apiCall(apiService.confirmSubscriptionProduct(getQueryParam(accountId, subscriptionProductId, purchaseToken)))){
            is Result.Success-> Result.Success(result.data.status)
            is Result.Error->result
        }
    }

    private fun getQueryParam(accountId: Int, subscriptionProductId: Int, purchaseToken: String) =
        hashMapOf<String,Any?>(
            NetworkConstant.QueryParam.ACCOUNT_ID to accountId,
            NetworkConstant.QueryParam.SUBSCRIPTION_PRODUCT_ID to subscriptionProductId,
            NetworkConstant.QueryParam.PURCHASE_TOKEN to purchaseToken
        )


}