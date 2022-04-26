package com.appcues.debugger

import com.appcues.AppcuesConfig
import com.appcues.di.KoinScopePlugin
import org.koin.dsl.ScopeDSL

internal object DebuggerKoin : KoinScopePlugin {

    override fun ScopeDSL.install(config: AppcuesConfig) {
        scoped {
            DebuggerStatusManager(
                storage = get(),
                appcuesConfig = get(),
                appcuesRemoteSource = get(),
                contextResources = get(),
            )
        }

        scoped {
            DebuggerRecentEventsManager(
                contextResources = get()
            )
        }
    }
}
