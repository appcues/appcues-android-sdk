package com.appcues.di

import android.content.Context
import com.appcues.Appcues
import com.appcues.AppcuesConfig
import com.appcues.data.remote.di.AppcuesRemoteSourceModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.Koin
import org.koin.core.KoinApplication
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.koinApplication
import java.util.UUID

internal object AppcuesKoinContext {

    private lateinit var koinApp: KoinApplication

    private var isStarted = false

    val koin: Koin get() = koinApp.koin

    fun getScope(scopeId: String): Scope = koin.getOrCreateScope(scopeId = scopeId, qualifier = named(scopeId))

    fun createAppcues(context: Context, appcuesConfig: AppcuesConfig): Appcues {
        startKoin(context)

        return createScope(appcuesConfig).get()
    }

    private fun createScope(appcuesConfig: AppcuesConfig): Scope {
        val scopeId: String = UUID.randomUUID().toString()

        koin.loadModules(
            arrayListOf(
                AppcuesModule.install(scopeId = scopeId, config = appcuesConfig),
                AppcuesRemoteSourceModule.install(scopeId = scopeId, config = appcuesConfig),
            )
        )

        return getScope(scopeId)
    }

    private fun startKoin(context: Context) {
        if (isStarted.not()) {
            koinApp = koinApplication {
                androidContext(context.applicationContext)
            }
            isStarted = true
        }
    }
}
