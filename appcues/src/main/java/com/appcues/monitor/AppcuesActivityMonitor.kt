package com.appcues.monitor

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import com.appcues.ui.AppcuesActivity
import java.lang.ref.WeakReference

internal object AppcuesActivityMonitor : Application.ActivityLifecycleCallbacks {

    private var activityWeakReference: WeakReference<Activity>? = null

    val activity: Activity?
        get() = activityWeakReference?.get()

    fun initialize(application: Application) {
        application.registerActivityLifecycleCallbacks(this)
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        if (activity !is AppcuesActivity) {
            this.activityWeakReference = WeakReference(activity)
        }
    }

    override fun onActivityStarted(activity: Activity) {
        // do nothing
    }

    override fun onActivityResumed(activity: Activity) {
        // do nothing
    }

    override fun onActivityPaused(activity: Activity) {
        // do nothing
    }

    override fun onActivityStopped(activity: Activity) {
        // do nothing
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        // do nothing
    }

    override fun onActivityDestroyed(activity: Activity) {
        activity.sendLocalBroadcast(Intent(activity.intentActionFinish()))
    }
}
