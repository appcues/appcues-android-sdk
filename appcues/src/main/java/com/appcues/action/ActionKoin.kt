package com.appcues.action

import com.appcues.AppcuesConfig
import com.appcues.action.appcues.AppcuesCloseAction
import com.appcues.action.appcues.AppcuesLinkAction
import com.appcues.di.KoinScopePlugin
import com.appcues.trait.appcues.AppcuesSkippableTrait
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
            AppcuesCloseAction(
                config = params.getOrNull(),
                stateMachine = get(),
            )
        }

        factory { params ->
            AppcuesLinkAction(
                config = params.getOrNull(),
                context = get(),
            )
        }

        factory { params ->
            AppcuesSkippableTrait(
                config = params.getOrNull(),
                stateMachine = get(),
            )
        }
    }
}
