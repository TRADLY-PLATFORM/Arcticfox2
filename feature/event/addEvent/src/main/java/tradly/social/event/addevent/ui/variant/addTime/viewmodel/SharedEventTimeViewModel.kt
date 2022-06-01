package tradly.social.event.addevent.ui.variant.addTime.viewmodel

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import tradly.social.common.base.BaseViewModel
import tradly.social.common.base.SingleEvent

class SharedEventTimeViewModel:BaseViewModel() {

    private val _onEventTimeLiveData by lazy { MutableLiveData<Pair<Long,Long>>() }
    val onEventTimeLiveData:LiveData<Pair<Long,Long>> = _onEventTimeLiveData

    private val _onEventTimeSelectionLiveData by lazy { MutableLiveData<SingleEvent<Pair<Long,Long>>>() }
    val onEventTimeSelectionLiveData:LiveData<SingleEvent<Pair<Long,Long>>> = _onEventTimeSelectionLiveData

    private val _onFinishLiveData by lazy { MutableLiveData<SingleEvent<Bundle>>() }
    val onFinishLiveData:LiveData<SingleEvent<Bundle>> = _onFinishLiveData

    fun setTime(startMillis:Long,endMillis:Long){
        _onEventTimeLiveData.value = Pair(startMillis,endMillis)
    }

    fun onTimeSelected(tartMillis:Long,endMillis:Long){
        _onEventTimeSelectionLiveData.value = SingleEvent(Pair(tartMillis,endMillis))
    }

    fun setFinish(bundle: Bundle){
        this._onFinishLiveData.value = SingleEvent(bundle)
    }

}