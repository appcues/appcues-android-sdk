package com.appcues

import com.appcues.action.ActionRegistry
import com.appcues.action.appcues.AppcuesCloseAction
import com.appcues.action.appcues.AppcuesLinkAction
import com.appcues.data.AppcuesRepository
import com.appcues.di.KoinModule
import com.appcues.logging.Logcues
import com.appcues.statemachine.StateMachine
import com.appcues.trait.TraitRegistry
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

internal object AppcuesModule : KoinModule {

    override fun install(scopeId: String, config: AppcuesConfig): Module = module {
        scope(named(scopeId)) {
            scoped {
                Appcues(
                    logcues = get(),
                    appcuesScope = get(),
                    actionRegistry = get(),
                    traitRegistry = get(),
                )
            }

            scoped { AppcuesSession() }
            scoped { Logcues(config.loggingLevel) }
            scoped { StateMachine(scopeId = scopeId) }

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
                    experienceMapper = get(),
                )
            }

            scoped {
                ActionRegistry(
                    scopeId = scopeId,
                    logcues = get(),
                )
            }

            factory { params ->
                AppcuesCloseAction(
                    config = params.getOrNull(),
                    stateMachine = get()
                )
            }

            factory { params ->
                AppcuesLinkAction(
                    config = params.getOrNull(),
                    context = get()
                )
            }

            scoped {
                TraitRegistry(
                    logcues = get()
                )
            }
        }
    }
}
