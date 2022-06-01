package tradly.social.common.base

import android.app.Activity
import java.lang.ref.WeakReference

object ActivityHelper {

    private var currentActivityInstance: WeakReference<Activity>?=null

    fun setCurrentActivityInstance(weakReference:  WeakReference<Activity>?){
        currentActivityInstance = weakReference
    }

    fun getCurrentActivityInstance():Activity?{
        currentActivityInstance?.get()?.let { activity->
            if(!activity.isFinishing){
                return activity
            }
        }
        return null
    }
}