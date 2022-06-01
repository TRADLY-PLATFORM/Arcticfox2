package tradly.social.common.base

import android.app.Activity
import android.app.Application
import android.os.Bundle
import java.lang.ref.WeakReference

class ActivityLifecycle: Application.ActivityLifecycleCallbacks {

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {

    }

    override fun onActivityResumed(activity: Activity) {
        ActivityHelper.setCurrentActivityInstance(WeakReference(activity))
    }

    override fun onActivityPaused(activity: Activity) {
        ActivityHelper.setCurrentActivityInstance(null)
    }

    override fun onActivityStarted(activity: Activity) {

    }

    override fun onActivityDestroyed(activity: Activity) {
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {

    }

    override fun onActivityStopped(activity: Activity) {

    }
}