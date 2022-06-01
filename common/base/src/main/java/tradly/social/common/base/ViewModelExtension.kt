package tradly.social.common.base

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.*


class BaseViewModelFactory<T>(val creator: () -> T) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return creator() as T
    }
}

inline fun <reified T : ViewModel> Fragment.getViewModel(noinline creator: (() -> T)? = null): T {
    return if (creator == null)
        ViewModelProvider(this).get(T::class.java)
    else
        ViewModelProvider(this, BaseViewModelFactory(creator)).get(T::class.java)
}

inline fun <reified T : ViewModel> FragmentActivity.getViewModel(noinline creator: (() -> T)? = null): T {
    return if (creator == null)
        ViewModelProvider(this).get(T::class.java)
    else
        ViewModelProvider(this, BaseViewModelFactory(creator)).get(T::class.java)
}

fun <Type : Any, L : LiveData<Type>> LifecycleOwner.observe(liveData: L, observer: (Type?) -> Unit) =
    liveData.observe(this, Observer(observer))

fun <Type : Any, L : LiveData<SingleEvent<Type>>> LifecycleOwner.observeEvent(eventLiveData : L, observer: (Type) -> Unit = {}) =
    eventLiveData.observe(this, SingleEventObserver(observer))