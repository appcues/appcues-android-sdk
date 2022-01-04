package com.appcues.di

import android.content.Context
import com.appcues.AppcuesConfig
import org.koin.android.ext.koin.androidContext
import org.koin.core.KoinApplication
import org.koin.dsl.koinApplication

internal class DependencyProvider(context: Context, config: AppcuesConfig) {

    private val koinApplication: KoinApplication = koinApplication {
        androidContext(context.applicationContext)

        modules(appcuesModule(config))
    }

    inline fun <reified T : Any> get(): T {
        return koinApplication.koin.get()
    }

}