package tradly.social.common.base

import androidx.lifecycle.Observer


open class SingleEvent<out T>(private val content: T) {

    private var isObserved = false

    fun isObserved(): T? =
        if (isObserved) null
        else {
            isObserved = true
            content
        }

    fun getValue() = content
}

open class SingleEventObserver<T>(private val eventObserver:(T)->Unit): Observer<SingleEvent<T>> {
    override fun onChanged(t: SingleEvent<T>) {
        t.isObserved()?.let { eventData->
            eventObserver(eventData)
        }
    }
}