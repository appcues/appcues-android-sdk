package com.appcues.monitor

import android.app.Activity
import android.app.Application
import android.os.Bundle

internal class ActivityMonitor(application: Application) : Application.ActivityLifecycleCallbacks {

    init {
        application.registerActivityLifecycleCallbacks(this)
    }

    private var _activity: Activity? = null

    val activity: Activity?
        get() = _activity

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        this._activity = activity
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
        // do nothing
    }
}