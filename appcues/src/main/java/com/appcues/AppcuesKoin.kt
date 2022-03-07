package com.appcues

import com.appcues.analytics.AnalyticsTracker
import com.appcues.data.AppcuesRepository
import com.appcues.di.KoinScopePlugin
import com.appcues.logging.Logcues
import com.appcues.statemachine.StateMachine
import com.appcues.ui.ExperienceRenderer
import org.koin.dsl.ScopeDSL

internal object AppcuesKoin : KoinScopePlugin {

    override fun ScopeDSL.install(config: AppcuesConfig) {
        scoped { config }

        scoped {
            Appcues(
                koinScope = this
            )
        }

        scoped { Logcues(config.loggingLevel) }
        scoped { StateMachine() }

        scoped {
            Storage(
                context = get(),
                config = get()
            )
        }

        scoped {
            AnalyticsTracker(
                config = config,
                repository = get(),
                storage = get(),
                experienceRenderer = get()
            )
        }

        scoped {
            ExperienceRenderer(
                repository = get(),
                stateMachine = get()
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
