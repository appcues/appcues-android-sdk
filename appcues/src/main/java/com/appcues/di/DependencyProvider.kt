package com.appcues.di

import android.content.Context
import androidx.lifecycle.ViewModel
import com.appcues.AppcuesConfig
import com.appcues.data.remote.di.AppcuesRemoteSourceModule
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.ViewModelOwnerDefinition
import org.koin.androidx.viewmodel.koin.getViewModel
import org.koin.core.KoinApplication
import org.koin.dsl.koinApplication

internal class DependencyProvider(
    context: Context,
    config: AppcuesConfig,
) {

    private val koinApplication: KoinApplication = koinApplication {
        androidContext(context.applicationContext)

        modules(
            AppcuesModule.install(config = config, dependencyProvider = this@DependencyProvider),
            AppcuesRemoteSourceModule.install(apiHostUrl = config.apiHostUrl)
        )
    }

    inline fun <reified T : Any> get(): T {
        return koinApplication.koin.get()
    }

    inline fun <reified T : ViewModel> getViewModel(noinline owner: ViewModelOwnerDefinition): T {
        return koinApplication.koin.getViewModel(owner = owner)
    }
}
