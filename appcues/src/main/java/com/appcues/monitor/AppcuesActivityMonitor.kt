package com.appcues.monitor

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.appcues.ui.AppcuesActivity
import java.lang.ref.WeakReference

internal object AppcuesActivityMonitor : Application.ActivityLifecycleCallbacks {

    private var activityWeakReference: WeakReference<Activity>? = null

    val activity: Activity?
        get() = activityWeakReference?.get()

    fun initialize(application: Application) = apply {
        application.registerActivityLifecycleCallbacks(this)
    }

    override fun onActivityResumed(activity: Activity) {
        if (activity !is AppcuesActivity) {
            this.activityWeakReference = WeakReference(activity)
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
    override fun onActivityStarted(activity: Activity) = Unit
    override fun onActivityPaused(activity: Activity) = Unit
    override fun onActivityStopped(activity: Activity) = Unit
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
    override fun onActivityDestroyed(activity: Activity) = Unit
}
