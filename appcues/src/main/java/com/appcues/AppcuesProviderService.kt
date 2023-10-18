package com.appcues

import android.app.Activity
import android.app.Application
import android.content.Context
import com.appcues.monitor.AppcuesActivityMonitor

/**
 * Allows programmatic access to start and stop services normally controlled
 * through an androidx.startup.InitializationProvider. In most typical use cases,
 * this access is not expected to be necessary, and these services will start
 * automatically.
 */
public object AppcuesProviderService {

    /**
     * Start Appcues services.
     *
     * It is not normally expected to call this function, as services are started
     * automatically using an androidx.startup.InitializationProvider. However, if necessary
     * to have programmatic control to stop and start services, this function can be used.
     *
     * The [context] parameter should be the currently active Activity, if possible, to ensure
     * that the Appcues SDK has this Activity available to present content.
     *
     * @param context The Android Context used by the host application.
     */
    public fun start(context: Context) {
        // stop any existing services first before starting new
        stop(context)

        val application = context.applicationContext as Application
        AppcuesActivityMonitor.initialize(application)
        (context as? Activity)?.let {
            AppcuesActivityMonitor.onActivityResumed(it)
        }
    }

    /**
     * Stop Appcues services.
     *
     * It is not normally expected to call this function, as services are started
     * automatically using an androidx.startup.InitializationProvider. However, if necessary
     * to have programmatic control to stop and start services, this function can be used.
     *
     * @param context The Android Context used by the host application.
     */
    public fun stop(context: Context) {
        val application = context.applicationContext as Application
        AppcuesActivityMonitor.reset(application)
    }
}
