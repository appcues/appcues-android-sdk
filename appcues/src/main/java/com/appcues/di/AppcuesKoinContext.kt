package com.appcues.di

import android.content.Context
import com.appcues.Appcues
import com.appcues.AppcuesKoin
import com.appcues.action.ActionKoin
import com.appcues.analytics.AnalyticsKoin
import com.appcues.data.local.DataLocalKoin
import com.appcues.data.mapper.DataMapperKoin
import com.appcues.data.remote.DataRemoteKoin
import com.appcues.debugger.DebuggerKoin
import com.appcues.trait.TraitKoin
import org.koin.android.ext.koin.androidContext
import org.koin.core.Koin
import org.koin.core.KoinApplication
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import java.util.UUID

internal object AppcuesKoinContext {

    private lateinit var koinApp: KoinApplication

    private var isStarted = false

    val koin: Koin get() = koinApp.koin

    private val koinPlugins = arrayListOf(
        AppcuesKoin,
        AnalyticsKoin,
        ActionKoin,
        TraitKoin,
        DataRemoteKoin,
        DataMapperKoin,
        DataLocalKoin,
        DebuggerKoin
    )

    private fun getScope(scopeId: String): Scope = koin.getOrCreateScope(scopeId = scopeId, qualifier = named(scopeId))

    fun createAppcuesScope(context: Context, appcues: Appcues): Scope {
        startKoin(context)
        return createScope(appcues)
    }

    private fun startKoin(context: Context) {
        if (isStarted.not()) {
            koinApp = koinApplication {
                androidContext(context.applicationContext)
            }
            isStarted = true
        }
    }

    private fun createScope(appcues: Appcues): Scope {
        return generateNewScopeId().let { scopeId ->
            getScope(scopeId).also {
                koinPlugins.installModule(it, appcues)
            }
        }
    }

    private fun generateNewScopeId() = UUID.randomUUID().toString()

    private fun List<KoinScopePlugin>.installModule(scope: Scope, appcues: Appcues) {
        koinApp.modules(
            module {
                scope(named(scope.id)) {
                    scoped { appcues }
                    scoped { appcues.config }
                    scoped { scope }
                    // run install for each KoinScopePlugin
                    forEach {
                        with(it) { install() }
                    }
                }
            }
        )
    }
}
