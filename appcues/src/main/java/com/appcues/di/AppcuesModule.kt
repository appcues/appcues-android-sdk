package com.appcues.di

import android.app.Application
import com.appcues.AppcuesConfig
import com.appcues.logging.Logcues
import com.appcues.monitor.ActivityMonitor
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

internal fun appcuesModule(config: AppcuesConfig) = module {
    single { config }
    single { Logcues(get<AppcuesConfig>().loggingLevel) }

    factory { ActivityMonitor(androidContext() as Application) }
}
