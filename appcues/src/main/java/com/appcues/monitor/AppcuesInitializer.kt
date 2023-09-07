package com.appcues.monitor

import android.app.Application
import android.content.Context
import androidx.startup.Initializer

@Suppress("unused")
internal class AppcuesInitializer : Initializer<Unit> {

    override fun create(context: Context) {
        ApplicationMonitor.initialize()

        AppcuesActivityMonitor.initialize(context.applicationContext as Application)

        return
    }

    override fun dependencies() = emptyList<Class<out Initializer<*>>>()
}
