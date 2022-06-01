package tradly.social.data.model.dataSource

import tradly.social.common.base.AppController
import tradly.social.data.model.network.retrofit.RetrofitManager
import tradly.social.common.network.CustomError
import tradly.social.domain.dataSource.PayoutDataSource
import tradly.social.domain.entities.AppError
import tradly.social.domain.entities.Payout
import tradly.social.domain.entities.Result

class ParsePayoutDataSource:PayoutDataSource{
    override fun initStripeConnect(): Result<Payout> =  when(val result = RetrofitManager.getInstance().connectOAuth()) {
        is Result.Success -> {
            if (result.data.status) {
                Result.Success(Payout(result.data.data.oAuthUrl))
            } else {
                Result.Error(AppError(code = CustomError.PAYOUT_SET_UP_FAILED))
            }
        }
        is Result.Error -> result
    }

    override fun getStripeAccountLink(accountId: String): Result<Payout> =
        when(val result = RetrofitManager.getInstance().getAccountLink(getRequestBody(accountId))){
            is Result.Success -> {
                if (result.data.status) {
                        Result.Success(Payout(result.data.data.accountLink))
                } else {
                    Result.Error(AppError(code = CustomError.PAYOUT_SET_UP_FAILED))
                }
            }
            is Result.Error -> result
        }


    override fun getStripeStatus(accountId: String): Result<Payout> {
        return when(val result = RetrofitManager.getInstance().getStripeStatus(accountId)){
            is Result.Success -> {
                if (result.data.status) {
                   with(result.data.data){
                       Result.Success(Payout(
                           isStripeOnBoardingConnected = isStripeOnBoardingConnected,
                           isPayoutEnabled = isPayoutEnabled,
                           errors = errors.map { it.reason }
                       ))
                   }
                } else {
                    Result.Error(AppError(code = CustomError.STRIPE_STATUS_FAILED))
                }
            }
            is Result.Error -> result
        }
    }

    override fun getStripeLoginLink(accountId:String): Result<Payout> {
        return when(val result = RetrofitManager.getInstance().getStripeLoginLink(getRequestBody(accountId))){
            is Result.Success -> {
                if (result.data.status) {
                    Result.Success(Payout(result.data.data.loginLink))
                } else {
                    Result.Error(AppError(code = CustomError.STRIPE_STATUS_FAILED))
                }
            }
            is Result.Error -> result
        }
    }

    override fun disConnectOAuth(): Result<Boolean> = when(val result = RetrofitManager.getInstance().disConnectOAuth()){
            is Result.Success->{
                if(result.data.status){
                    val user = AppController.appController.getUser()
                    user?.let {
                        user.isStripeConnected = false
                        AppController.appController.cacheUserData(user)
                    }
                    Result.Success(result.data.status)
                }
                else{
                    Result.Error(AppError(code = CustomError.STRIPE_DISCONNECT_FAILED))
                }
            }
            is Result.Error->result
        }

    override fun verifyStripeOAuth(url: String): Result<Boolean> =
        when(val result = RetrofitManager.getInstance().verifyStripeOAuth(url)){
            is Result.Success->{
                if(result.data.status){
                    val user = AppController.appController.getUser()
                    user?.let {
                        user.isStripeConnected = true
                        AppController.appController.cacheUserData(user)
                    }
                    Result.Success(result.data.status)
                }
                else{
                    Result.Error(exception = AppError(code = CustomError.STRIPE_CONNECT_FAILED))
                }
            }
            is Result.Error->result
        }

    private fun getRequestBody(accountId: String) = hashMapOf<String,Any?>("account_id" to accountId)
}