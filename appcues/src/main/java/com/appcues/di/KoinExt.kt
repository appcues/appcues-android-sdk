package com.appcues.di

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModel
import com.appcues.Appcues
import com.appcues.AppcuesConfig
import com.appcues.data.remote.di.AppcuesRemoteSourceModule
import org.koin.androidx.viewmodel.ViewModelOwnerDefinition
import org.koin.androidx.viewmodel.scope.getViewModel
import org.koin.core.component.get
import java.util.UUID

internal inline fun <reified T : ViewModel> AppcuesKoinComponent.getOwnedViewModel(noinline owner: ViewModelOwnerDefinition): T {
    return scope.getViewModel(owner = owner)
}

internal fun AppcuesKoinComponent.getApplicationContext(): Application {
    return get<Context>() as Application
}

internal fun AppcuesKoinContext.startKoinOnce(context: Context) {
    if (isStarted.not()) {
        startKoin(context)
    }
}

internal fun AppcuesKoinContext.newAppcuesInstance(appcuesConfig: AppcuesConfig): Appcues {
    val scopeId: String = UUID.randomUUID().toString()

    getKoin().loadModules(
        arrayListOf(
            AppcuesModule.install(scopeId = scopeId, config = appcuesConfig),
            AppcuesRemoteSourceModule.install(scopeId = scopeId, config = appcuesConfig)

        )
    )

    return Appcues(
        logcues = getScope(scopeId).get(),
        activityMonitor = getScope(scopeId).get(),
    )
}
