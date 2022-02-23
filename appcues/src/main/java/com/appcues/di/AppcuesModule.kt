package com.appcues.di

import com.appcues.Appcues
import com.appcues.AppcuesConfig
import com.appcues.AppcuesScope
import com.appcues.AppcuesSession
import com.appcues.action.ActionRegistry
import com.appcues.action.appcues.AppcuesCloseAction
import com.appcues.data.AppcuesRepository
import com.appcues.logging.Logcues
import com.appcues.statemachine.StateMachine
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

internal object AppcuesModule {

    fun install(scopeId: String, config: AppcuesConfig): Module = module {
        scope(named(scopeId)) {
            scoped {
                Appcues(
                    logcues = get(),
                    appcuesScope = get(),
                    actionRegistry = get(),
                )
            }

            scoped { AppcuesSession() }
            scoped { Logcues(config.loggingLevel) }

            scoped {
                StateMachine(
                    logger = get(),
                    scopeId = scopeId
                )
            }

            scoped {
                AppcuesScope(
                    logcues = get(),
                    repository = get(),
                    stateMachine = get(),
                )
            }

            scoped {
                AppcuesRepository(
                    appcuesRemoteSource = get(),
                )
            }

            scoped {
                ActionRegistry(
                    scopeId = scopeId,
                    logcues = get(),
                )
            }

            factory { params ->
                AppcuesCloseAction(params.get(), get())
            }
        }
    }
}
