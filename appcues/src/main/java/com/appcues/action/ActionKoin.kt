package com.appcues.action

import com.appcues.AppcuesConfig
import com.appcues.action.appcues.CloseAction
import com.appcues.action.appcues.LinkAction
import com.appcues.action.appcues.TrackEventAction
import com.appcues.di.KoinScopePlugin
import org.koin.dsl.ScopeDSL

internal object ActionKoin : KoinScopePlugin {

    override fun ScopeDSL.install(config: AppcuesConfig) {
        scoped {
            ActionRegistry(
                scope = get(),
                logcues = get(),
            )
        }

        factory { params ->
            CloseAction(
                config = params.getOrNull(),
                stateMachine = get(),
            )
        }

        factory { params ->
            LinkAction(
                config = params.getOrNull(),
                context = get(),
            )
        }

        factory { params ->
            TrackEventAction(
                config = params.getOrNull()
            )
        }
    }
}
