package com.appcues

import android.app.Activity
import com.appcues.monitor.AppcuesActivityMonitor

/**
 * Debug only test helper that allows our test suite to initialize the AppcuesActivityMonitor
 * and set the given [activity] to the current activity.
 *
 * Tools like Roborazzi/Roboelectric will not execute the androidx.startup.Initializer,
 * AppcuesInitializer, like a normal device build would, so this helper works around that.
 */
public fun initializeActivityMonitor(activity: Activity) {
    AppcuesActivityMonitor.onActivityResumed(activity)
}
