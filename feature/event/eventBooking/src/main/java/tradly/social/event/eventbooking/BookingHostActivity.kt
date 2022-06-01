package tradly.social.event.eventbooking

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import tradly.social.common.base.AppConstant
import tradly.social.common.base.getViewModel
import tradly.social.common.base.observeEvent
import tradly.social.common.base.getIntentExtra
import tradly.social.common.base.getStringData
import tradly.social.common.navigation.Activities
import tradly.social.common.resources.CommonResourceDrawable
import tradly.social.common.resources.CommonResourceString
import tradly.social.common.util.parser.extension.toObject
import tradly.social.domain.entities.Event
import tradly.social.event.eventbooking.confirmbooking.ui.EventBookingConfirmFragment
import tradly.social.event.eventbooking.databinding.ActivityBookingHostBinding
import tradly.social.event.eventbooking.mybooking.BookingListFragment
import tradly.social.common.base.BaseActivity

class BookingHostActivity : BaseActivity() {

    lateinit var sharedEventBookingViewModel: SharedEventBookingViewModel
    lateinit var activityBookingHostBinding: ActivityBookingHostBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityBookingHostBinding = DataBindingUtil.setContentView(this,R.layout.activity_booking_host)
        setToolbar(activityBookingHostBinding.layoutAppToolbar.toolbar,CommonResourceString.event_confirm_booking_title,CommonResourceDrawable.ic_back)
        sharedEventBookingViewModel = getViewModel { SharedEventBookingViewModel() }
        setDataFromIntent()
        initFragment()
        observeLiveData()
        if(intent.getBooleanExtra(AppConstant.BundleProperty.IS_FROM_WEB_PAYMENT,false)){
            setAndShowPaymentStatusDialog()
        }
    }

    private fun observeLiveData(){
        observeEvent(sharedEventBookingViewModel.fragmentNavigationLiveData,this::onFragmentNavigation)
    }

    private fun onFragmentNavigation(tag:String){
        when(tag){
            EventBookingDetailFragment.TAG-> launchFragment(EventBookingDetailFragment(),true,tag)
        }
    }

    private fun setDataFromIntent(){
        sharedEventBookingViewModel.setEvent(getIntentExtra<String>(Activities.BookingHostActivity.EXTRAS_EVENT).toObject<Event>())
    }

    private fun setAndShowPaymentStatusDialog(){
        val orderReference = getStringData(AppConstant.BundleProperty.ORDER_REFERENCE)
        val selectedPaymentId = getStringData(AppConstant.BundleProperty.PAYMENT_METHOD_ID)
        val status = intent.getBooleanExtra(AppConstant.BundleProperty.STATUS,false)
        sharedEventBookingViewModel.setWebPaymentResult(orderReference,selectedPaymentId,status)
    }


    private fun initFragment(){
        with(supportFragmentManager.beginTransaction()){
            when(getIntentExtra<Int>(Activities.BookingHostActivity.EXTRAS_HOST_FRAGMENT_ID)){
                Activities.BookingHostActivity.HostFragment.BOOKING_CONFIRM-> {
                    add(activityBookingHostBinding.fragContainer.id,EventBookingConfirmFragment(),EventBookingConfirmFragment.TAG)
                }
                Activities.BookingHostActivity.HostFragment.BOOKING_LIST->{
                    add(activityBookingHostBinding.fragContainer.id,BookingListFragment(),BookingListFragment.TAG)
                }
            }
            commitNow()
        }
    }

    private fun launchFragment(fragment:Fragment,addToBackStack:Boolean,tag:String){
        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(R.id.fragContainer,fragment,tag)
        if (addToBackStack){
            transaction.addToBackStack(tag)
        }
        transaction.commit()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        data?.let {
            if(Activity.RESULT_OK==resultCode){
               sharedEventBookingViewModel.setStripeActivityResult(requestCode, resultCode, data)
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        this.intent = intent
        intent?.let {
            if(intent.getBooleanExtra(AppConstant.BundleProperty.IS_FROM_WEB_PAYMENT,false)) {
                setAndShowPaymentStatusDialog()
            }
        }
    }
}