package com.appcues

import android.app.Application
import android.content.Context
import androidx.startup.Initializer
import com.appcues.monitor.AppcuesActivityMonitor

@Suppress("unused")
internal class AppcuesInitializer : Initializer<AppcuesActivityMonitor> {

    override fun create(context: Context): AppcuesActivityMonitor {
        AppcuesActivityMonitor.initialize(context.applicationContext as Application)
        return AppcuesActivityMonitor
    }

    override fun dependencies() = emptyList<Class<out Initializer<*>>>()
}
