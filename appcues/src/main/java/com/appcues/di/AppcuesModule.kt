package com.appcues.di

import android.app.Application
import com.appcues.AppcuesConfig
import com.appcues.logging.Logcues
import com.appcues.monitor.ActivityMonitor
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

internal object AppcuesModule {

    fun install(config: AppcuesConfig): Module = module {
        single { config }
        single { Logcues(get<AppcuesConfig>().loggingLevel) }

        factory { ActivityMonitor(androidContext() as Application) }
    }
}
