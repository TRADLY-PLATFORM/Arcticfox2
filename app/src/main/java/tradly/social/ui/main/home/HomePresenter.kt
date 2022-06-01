package tradly.social.ui.main.home

import tradly.social.common.base.NetworkUtil
import tradly.social.data.model.dataSource.*
import tradly.social.domain.entities.AppError
import tradly.social.domain.entities.Home
import tradly.social.domain.entities.Result
import tradly.social.domain.repository.*
import tradly.social.domain.usecases.*
import kotlinx.coroutines.*
import tradly.social.common.base.AppConfigHelper
import tradly.social.common.base.LocationHelper
import tradly.social.common.network.feature.common.datasource.StoreDataSourceImpl
import tradly.social.data.model.AppDataSyncHelper
import kotlin.coroutines.CoroutineContext

class HomePresenter(var view: View?) : CoroutineScope {
    private var job: Job
    private var getGroups: GetGroups? = null
    private var getHome:GetHome?=null
    private var followStore:FollowStore?=null
    interface View {
        fun noInternet()
        fun netWorkError()
        fun showProgressLoader()
        fun hideProgressLoader()
        fun onLoadData(home: Home)
        fun onFailure(appError: AppError)
    }


    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    init {
        val parseStoreDataSource = StoreDataSourceImpl()
        val storeRepository = StoreRepository(parseStoreDataSource)
        val parseGroupDataSource = ParseGroupDataSource()
        val groupRepository = GroupRepository(parseGroupDataSource)
        val parseHomeDataSource = ParseHomeDataSource()
        val homeRepository = HomeRepository(parseHomeDataSource)
        followStore = FollowStore(storeRepository)
        getGroups = GetGroups(groupRepository)
        getHome = GetHome(homeRepository)
        job = SupervisorJob()
    }

    fun setInteractor(view: View?){
        this.view = view
    }
    fun loadData(shouldShowProgress:Boolean = false , homeLocationEnabled:Boolean) {
        if (NetworkUtil.isConnectingToInternet()) {
            if(shouldShowProgress){
                view?.showProgressLoader()
            }
            if(!shouldShowProgress){
                syncAndUpdate()
            }
            if(!homeLocationEnabled){ loadData(0.0,0.0) }
            else{
                LocationHelper.getInstance().getMyLocation { loadData(it.latitude,it.longitude) }
            }
        }
    }

    fun loadData(latitude:Double,longitude:Double){
        CoroutineScope(Dispatchers.Main+job).launch {
            val homeCall = async(Dispatchers.IO) { getHome?.invoke(latitude, longitude) }
            when(val result = homeCall.await()){
                is Result.Success->view?.onLoadData(result.data)
                is Result.Error->view?.onFailure(result.exception)
            }
            view?.hideProgressLoader()
        }
    }

    fun addUserToGroup(groupId:String){
        launch(Dispatchers.IO) {
            getGroups?.addUserToGroup(groupId)
        }
    }

    fun followStore(storeId:String){
        launch (Dispatchers.IO){ followStore?.followStore(storeId) }
    }

    fun syncAndUpdate(){
        AppDataSyncHelper.syncAppConfigByKey(AppConfigHelper.getKeyList())
        AppDataSyncHelper.syncAppConfig()
        AppDataSyncHelper.syncUserStore()
        AppDataSyncHelper.syncDeviceDetail()
        AppDataSyncHelper.syncCurrencies()
    }


    fun syncConfigs(){
        AppDataSyncHelper.syncAppConfig()
        AppDataSyncHelper.syncTenantConfig()
    }

    fun onDestroy() {
        job.cancelChildren()
        view = null
    }

}