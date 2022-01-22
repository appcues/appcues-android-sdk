package com.appcues.di

import android.content.Context
import org.koin.android.ext.koin.androidContext
import org.koin.core.Koin
import org.koin.core.KoinApplication
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.koinApplication

internal object AppcuesKoinContext {

    private lateinit var koinApp: KoinApplication

    var isStarted = false

    fun startKoin(context: Context) {
        koinApp = koinApplication {
            androidContext(context.applicationContext)
        }
        isStarted = true
    }

    fun getKoin(): Koin = koinApp.koin

    fun getScope(scopeId: String): Scope = koinApp.koin.getOrCreateScope(scopeId = scopeId, qualifier = named(scopeId))
}
