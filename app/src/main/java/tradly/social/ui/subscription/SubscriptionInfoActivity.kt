package tradly.social.ui.subscription

import android.os.Bundle
import kotlinx.android.synthetic.main.activity_subscription_info.*
import kotlinx.android.synthetic.main.toolbar.*
import tradly.social.R
import tradly.social.common.base.DateTimeHelper
import tradly.social.common.base.AppController
import tradly.social.common.cache.AppCache
import tradly.social.common.base.setGone
import tradly.social.common.base.setVisible
import tradly.social.data.model.CoroutinesManager
import tradly.social.common.network.feature.common.datasource.StoreDataSourceImpl
import tradly.social.domain.entities.Result
import tradly.social.domain.repository.StoreRepository
import tradly.social.domain.usecases.GetStores
import tradly.social.common.base.BaseActivity

class SubscriptionInfoActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_subscription_info)
        setToolbar(toolbar,R.string.purchases,R.drawable.ic_back)
        syncData()
    }

    private fun syncData(){
        val parseStoreDataSource = StoreDataSourceImpl()
        val storeRepository = StoreRepository(parseStoreDataSource)
        val store = GetStores(storeRepository)
        progressBar.setVisible()
        CoroutinesManager.ioThenMain({store.syncUserStore(AppCache.getCacheUser()?.id)},{ result->
            progressBar.setGone()
            when(result){
                is Result.Success->{setData()}
                is Result.Error->{}
            }
        })
    }

    private fun setData(){
        AppController.appController.getUserStore()?.let {
            cvGroup?.setVisible()
            tvMembershipSinceValue?.text = DateTimeHelper.getDateFromTimeMillis(it.subscription.subscriptionStarted,
                DateTimeHelper.FORMAT_DATE_MMM_DD_YYYY)
            tvMembershipValue?.text = getString(R.string.appname_subscription,getString(R.string.app_name))
            val subscriptionEndDate = DateTimeHelper.getDateFromTimeMillis(it.subscription.subscriptionEnd,
                DateTimeHelper.FORMAT_DATE_MMM_DD_YYYY)
            tvBillingStatusValue?.text = getString(R.string.billing_charging_date,subscriptionEndDate,getString(R.string.app_name))
        }
    }
}