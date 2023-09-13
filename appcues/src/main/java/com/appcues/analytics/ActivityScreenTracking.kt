package com.appcues.analytics

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import com.appcues.logging.Logcues
import com.appcues.util.getActivityInfoCompat

internal class ActivityScreenTracking(
    private val context: Context,
    private val analytics: Analytics,
    private val logcues: Logcues,
) : Application.ActivityLifecycleCallbacks {

    fun trackScreens() {
        val application = context.applicationContext as Application
        application.registerActivityLifecycleCallbacks(this)

        logcues.info("Automatic screen tracking enabled")
    }

    fun stop() {
        val application = context.applicationContext as Application
        application.unregisterActivityLifecycleCallbacks(this)

        logcues.info("Automatic screen tracking disabled")
    }

    override fun onActivityStarted(activity: Activity) {
        val packageManager = activity.packageManager
        try {
            val info = packageManager.getActivityInfoCompat(activity.componentName, PackageManager.GET_META_DATA)
            val activityLabel = info.loadLabel(packageManager)
            analytics.screen(activityLabel.toString(), isInternal = true)
        } catch (ex: PackageManager.NameNotFoundException) {
            logcues.error(ex)
        }
    }

    override fun onActivityCreated(activity: Activity, bundle: Bundle?) = Unit
    override fun onActivityResumed(activity: Activity) = Unit
    override fun onActivityPaused(activity: Activity) = Unit
    override fun onActivityStopped(activity: Activity) = Unit
    override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) = Unit
    override fun onActivityDestroyed(activity: Activity) = Unit
}
