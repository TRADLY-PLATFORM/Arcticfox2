package tradly.social.event.eventbooking

import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import tradly.social.common.base.BaseViewModel
import tradly.social.common.base.SingleEvent
import tradly.social.domain.entities.Event
import tradly.social.domain.entities.EventBooking

class SharedEventBookingViewModel:BaseViewModel() {

    private val _eventLiveData by lazy { MutableLiveData<Event?>() }
    val eventLiveData:LiveData<Event?> = _eventLiveData
    private val _eventBookingLiveData by lazy { MutableLiveData<EventBooking>() }
    val eventBookingLiveData:LiveData<EventBooking> = _eventBookingLiveData

    private val _fragmentNavigationLiveData by lazy { MutableLiveData<SingleEvent<String>>() }
    val fragmentNavigationLiveData:LiveData<SingleEvent<String>> = _fragmentNavigationLiveData
    private val _stripeActivityResult by lazy { MutableLiveData<SingleEvent<Triple<Int,Int,Intent>>>() }
    val stripeActivityResult:LiveData<SingleEvent<Triple<Int,Int,Intent>>> = _stripeActivityResult

    private val _webPaymentResultLiveData by lazy { MutableLiveData<SingleEvent<Triple<String,String,Boolean>>>() }
    val webPaymentResultLiveData:LiveData<SingleEvent<Triple<String,String,Boolean>>> = _webPaymentResultLiveData

    fun setWebPaymentResult(orderReference:String,paymentId:String,status:Boolean){
        _webPaymentResultLiveData.value = SingleEvent(Triple(orderReference,paymentId,status))
    }
    fun setStripeActivityResult(requestCode:Int,resultCode:Int,data:Intent){
        this._stripeActivityResult.value = SingleEvent(Triple(requestCode,resultCode,data))
    }

    fun setEvent(event: Event?){
        _eventLiveData.value = event
    }

    fun setEventBooking(eventBooking: EventBooking){
        _eventBookingLiveData.value = eventBooking
    }

    fun getEventBooking() = this.eventBookingLiveData.value

    fun getEvent() = this._eventLiveData.value

    fun setFragmentNavigation(tag:String){
        _fragmentNavigationLiveData.value = SingleEvent(tag)
    }
}