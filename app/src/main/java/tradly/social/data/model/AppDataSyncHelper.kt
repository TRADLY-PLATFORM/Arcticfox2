package tradly.social.data.model

import com.parse.ParseException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import tradly.social.common.base.Utils
import tradly.social.common.base.*
import tradly.social.data.model.dataSource.ParseConfigDataSource
import tradly.social.data.model.dataSource.ParseCurrencyDataSource
import tradly.social.common.network.feature.common.datasource.StoreDataSourceImpl
import tradly.social.common.network.base.ConfigAPIConstant
import tradly.social.domain.entities.Result
import tradly.social.domain.repository.CurrencyRepository
import tradly.social.domain.repository.ParseConfigRepository
import tradly.social.domain.repository.StoreRepository
import tradly.social.domain.usecases.GetAppConfig
import tradly.social.domain.usecases.GetCurrency
import tradly.social.domain.usecases.GetStores
import tradly.social.domain.usecases.GetTenantConfig

object AppDataSyncHelper{

    private var getTenantConfig:GetTenantConfig
    var getAppConfig:GetAppConfig
    private var getUserStore:GetStores?=null
    private var getCurrency:GetCurrency

    init {
        val parseStoreDataSource = StoreDataSourceImpl()
        val storeRepository = StoreRepository(parseStoreDataSource)
        val parseManagerConfigDataSource = ParseConfigDataSource()
        val configRepository = ParseConfigRepository(parseManagerConfigDataSource)
        val currencyDataSource = ParseCurrencyDataSource()
        val currencyRepository = CurrencyRepository(currencyDataSource)
        getTenantConfig = GetTenantConfig(configRepository)
        getAppConfig = GetAppConfig(configRepository)
        getUserStore = GetStores(storeRepository)
        getCurrency = GetCurrency(currencyRepository)
    }

    fun syncTenantConfig(){
        if(NetworkUtil.isConnectingToInternet()){
            GlobalScope.launch(Dispatchers.IO){ getTenantConfig.invoke(AppController.appController.getCurrentTenantID()) }
        }
    }

    fun syncCurrencies(){
        if(AppController.appController.getUser() != null && NetworkUtil.isConnectingToInternet()){
            GlobalScope.launch(Dispatchers.IO){ getCurrency.getCurrencies()}
        }
    }

    fun syncAppConfig(){
        if(NetworkUtil.isConnectingToInternet() && AppController.appController.getUser()!=null){
            GlobalScope.launch(Dispatchers.IO){ getAppConfig.syncAppGroupConfig(ConfigAPIConstant.getGroupList())}
        }
    }

    fun syncAppConfigByKey(keys: List<String>){
        if(NetworkUtil.isConnectingToInternet() && AppController.appController.getUser()!=null){
            GlobalScope.launch(Dispatchers.IO){ getAppConfig.getAppConfig(keys.joinToString(separator = ","))}
        }
    }

    inline fun <reified T>  getAppConfig(scope: CoroutineScope, key:String, crossinline callback:(any:T)->Unit){
        if(NetworkUtil.isConnectingToInternet(true)){
            CoroutinesManager.ioThenMain(scope,{ getAppConfig.getAppConfig(key)},{ result->
                when(result){
                   is Result.Success-> callback(AppConfigHelper.getConfigKey(key))
                   is Result.Error->{
                       ErrorHandler.handleError(exception = result.exception)
                       callback(AppConfigHelper.getConfigKey(key))
                   }
                }
            })
        }
    }

    fun syncUserStore(){
        if(AppController.appController.getUser() != null && NetworkUtil.isConnectingToInternet()){
            GlobalScope.launch(Dispatchers.IO){
                try{
                    getUserStore?.syncUserStore(AppController.appController.getUser()?.id)
                }catch (exception: ParseException){}
            }
        }
    }

    fun syncDeviceDetail(forceUpdate:Boolean = false){
        if(AppController.appController.getUser() != null && NetworkUtil.isConnectingToInternet()){
            Utils.updateDeviceDetail(forceUpdate)
        }
    }


}