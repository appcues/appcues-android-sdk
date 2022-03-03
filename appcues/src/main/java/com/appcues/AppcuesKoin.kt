package com.appcues

import com.appcues.data.AppcuesRepository
import com.appcues.di.KoinScopePlugin
import com.appcues.logging.Logcues
import com.appcues.statemachine.StateMachine
import org.koin.dsl.ScopeDSL

internal object AppcuesKoin : KoinScopePlugin {

    override fun installIn(koinScope: ScopeDSL, config: AppcuesConfig) {
        with(koinScope) {
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
            scoped { StateMachine() }

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
        }
    }
}
