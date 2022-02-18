package com.appcues.di

import com.appcues.Appcues
import com.appcues.AppcuesConfig
import com.appcues.AppcuesScope
import com.appcues.AppcuesSession
import com.appcues.action.ActionRegistry
import com.appcues.data.AppcuesRepository
import com.appcues.experience.container.DialogModalPresenter
import com.appcues.logging.Logcues
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
                DialogModalPresenter(
                    scopeId = scopeId,
                    context = get()
                )
            }

            scoped {
                AppcuesScope(
                    logcues = get(),
                    repository = get(),
                    presenter = get(),
                )
            }

            scoped {
                AppcuesRepository(
                    appcuesRemoteSource = get(),
                )
            }

            scoped {
                ActionRegistry(
                    logcues = get(),
                )
            }
        }
    }
}
