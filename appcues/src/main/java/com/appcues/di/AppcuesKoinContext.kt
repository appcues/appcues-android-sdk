package com.appcues.di

import android.content.Context
import com.appcues.Appcues
import com.appcues.AppcuesConfig
import com.appcues.AppcuesModule
import com.appcues.data.mapper.DataMapperModule
import com.appcues.data.remote.DataRemoteModule
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

    private val koinModules = arrayListOf(
        AppcuesModule,
        DataRemoteModule,
        DataMapperModule
    )

    fun getScope(scopeId: String): Scope = koin.getOrCreateScope(scopeId = scopeId, qualifier = named(scopeId))

    fun createAppcues(context: Context, appcuesConfig: AppcuesConfig): Appcues {
        startKoin(context)

        return createScope(appcuesConfig).get()
    }

    private fun startKoin(context: Context) {
        if (isStarted.not()) {
            koinApp = koinApplication {
                androidContext(context.applicationContext)
            }
            isStarted = true
        }
    }

    private fun createScope(appcuesConfig: AppcuesConfig): Scope {
        return generateNewScopeId().let { scopeId ->
            koinModules.loadIntoScope(scopeId, appcuesConfig)

            getScope(scopeId)
        }
    }

    private fun generateNewScopeId() = UUID.randomUUID().toString()

    private fun List<KoinModule>.loadIntoScope(scopeId: String, appcuesConfig: AppcuesConfig) {
        koin.loadModules(map { it.install(scopeId, appcuesConfig) })
    }
}
