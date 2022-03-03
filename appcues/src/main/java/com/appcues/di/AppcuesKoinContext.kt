package com.appcues.di

import android.content.Context
import com.appcues.Appcues
import com.appcues.AppcuesConfig
import com.appcues.AppcuesKoin
import com.appcues.action.ActionKoin
import com.appcues.data.mapper.DataMapperKoin
import com.appcues.data.remote.DataRemoteKoin
import com.appcues.trait.TraitKoin
import org.koin.android.ext.koin.androidContext
import org.koin.core.Koin
import org.koin.core.KoinApplication
import org.koin.core.module.Module
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
        ActionKoin,
        TraitKoin,
        DataRemoteKoin,
        DataMapperKoin,
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
            koinApp.modules(koinPlugins.scopedModule(scopeId, appcuesConfig))

            getScope(scopeId)
        }
    }

    private fun generateNewScopeId() = UUID.randomUUID().toString()

    private fun List<KoinScopePlugin>.scopedModule(scopeId: String, appcuesConfig: AppcuesConfig): Module {
        return module {
            scope(named(scopeId)) {
                forEach { it.installIn(this, appcuesConfig) }
            }
        }
    }
}
