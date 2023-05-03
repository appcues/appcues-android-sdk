package com.appcues.monitor

import android.app.Activity
import android.app.Application
import android.os.Bundle
import java.lang.ref.WeakReference

internal object AppcuesActivityMonitor : Application.ActivityLifecycleCallbacks {

    interface ActivityMonitorListener {

        fun onActivityChanged(activity: Activity)
    }

    private var activityWeakReference: WeakReference<Activity>? = null

    val activity: Activity?
        get() = activityWeakReference?.get()

    private val activityMonitorListener: HashSet<ActivityMonitorListener> = hashSetOf()

    fun subscribe(listener: ActivityMonitorListener) {
        activityMonitorListener.add(listener)
    }

    fun unsubscribe(listener: ActivityMonitorListener) {
        activityMonitorListener.remove(listener)
    }

    fun initialize(application: Application) = apply {
        application.registerActivityLifecycleCallbacks(this)
    }

    override fun onActivityResumed(activity: Activity) {
        if (this.activity != activity) {
            this.activityWeakReference = WeakReference(activity)

            // notifies all subscribers that activity has changed
            activityMonitorListener.forEach { it.onActivityChanged(activity) }
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
    override fun onActivityStarted(activity: Activity) = Unit
    override fun onActivityPaused(activity: Activity) = Unit
    override fun onActivityStopped(activity: Activity) = Unit
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
    override fun onActivityDestroyed(activity: Activity) = Unit
}
