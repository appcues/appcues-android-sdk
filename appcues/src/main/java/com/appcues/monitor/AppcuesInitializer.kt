package com.appcues.monitor

import android.app.Application
import android.content.Context
import androidx.startup.Initializer

@Suppress("unused")
internal class AppcuesInitializer : Initializer<AppcuesActivityMonitor> {

    override fun create(context: Context): AppcuesActivityMonitor {
        return AppcuesActivityMonitor.initialize(context.applicationContext as Application)
    }

    override fun dependencies() = emptyList<Class<out Initializer<*>>>()
}
