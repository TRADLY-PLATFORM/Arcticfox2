package tradly.social.event.addevent.ui.variant.addTime

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import tradly.social.common.base.getViewModel
import tradly.social.common.base.observeEvent
import tradly.social.common.base.getIntentExtra
import tradly.social.common.resources.CommonResourceDrawable
import tradly.social.common.resources.CommonResourceString
import tradly.social.event.addevent.R
import tradly.social.event.addevent.databinding.ActivityEventHostBinding
import tradly.social.event.addevent.ui.variant.addTime.viewmodel.SharedEventTimeViewModel
import tradly.social.common.base.BaseActivity

class EventTimeHostActivity : BaseActivity() {

    lateinit var activityEventHostBinding: ActivityEventHostBinding
    lateinit var sharedEventTimeViewModel: SharedEventTimeViewModel

    companion object{
        private const val EXTRAS_FROM_MILLIS = "fromMillis"
        private const val EXTRAS_TO_MILLIS = "toMillis"

        fun newIntent(context:Context,fromMillis:Long,toMillis:Long) = Intent(context,EventTimeHostActivity::class.java).apply {
            putExtra(EXTRAS_FROM_MILLIS,fromMillis)
            putExtra(EXTRAS_TO_MILLIS,toMillis)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityEventHostBinding = DataBindingUtil.setContentView(this,R.layout.activity_event_time_host)
        sharedEventTimeViewModel = getViewModel { SharedEventTimeViewModel() }
        setToolbar(activityEventHostBinding.toolbar.toolbar,title = CommonResourceString.add_time_title,backNavIcon = CommonResourceDrawable.ic_back)
        observeLiveData()
        sharedEventTimeViewModel.setTime( getIntentExtra(EXTRAS_FROM_MILLIS), getIntentExtra(EXTRAS_TO_MILLIS))
        launchFragment(
            EventTimeCreateFragment(),
            EventTimeCreateFragment.TAG,
            false
        )
    }

    private fun launchFragment(fragment:Fragment,tag:String,addToBackStack:Boolean){
        with(supportFragmentManager.beginTransaction()){
            add(activityEventHostBinding.fragmentContainer.id,fragment, tag)
            commit()
        }
    }

    private fun observeLiveData(){
        observeEvent(sharedEventTimeViewModel.onFinishLiveData,this::onFinish)
    }

    private fun onFinish(bundle: Bundle){
        val intent = Intent().apply { putExtras(bundle) }
        setResult(Activity.RESULT_OK,intent)
        finish()
    }
}