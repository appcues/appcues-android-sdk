package com.appcues.monitor

import android.content.Context
import androidx.startup.Initializer
import com.appcues.AppcuesProviderService

@Suppress("unused")
internal class AppcuesInitializer : Initializer<Unit> {

    override fun create(context: Context) {
        AppcuesProviderService.start(context)
    }

    override fun dependencies() = emptyList<Class<out Initializer<*>>>()
}
