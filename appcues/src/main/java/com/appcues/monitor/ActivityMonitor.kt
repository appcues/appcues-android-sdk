package com.appcues.monitor

import android.app.Activity
import android.app.Application
import android.os.Bundle

internal class ActivityMonitor(application: Application) : Application.ActivityLifecycleCallbacks {

    private var _resumedActivity: Activity? = null

    val resumedActivity: Activity?
        get() = _resumedActivity

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        // do nothing
    }

    override fun onActivityStarted(activity: Activity) {
        // do nothing
    }

    override fun onActivityResumed(activity: Activity) {
        _resumedActivity = activity
    }

    override fun onActivityPaused(activity: Activity) {
        _resumedActivity = null
    }

    override fun onActivityStopped(activity: Activity) {
        // do nothing
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        // do nothing
    }

    override fun onActivityDestroyed(activity: Activity) {
        // do nothing
    }
}