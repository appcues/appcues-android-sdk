package com.appcues

import com.appcues.analytics.ActivityRequestBuilder
import com.appcues.analytics.AnalyticsTracker
import com.appcues.analytics.AutoPropertyDecorator
import com.appcues.data.AppcuesRepository
import com.appcues.di.KoinScopePlugin
import com.appcues.logging.Logcues
import com.appcues.statemachine.StateMachine
import com.appcues.ui.ExperienceRenderer
import org.koin.dsl.ScopeDSL

internal object AppcuesKoin : KoinScopePlugin {

    override fun ScopeDSL.install(config: AppcuesConfig) {
        scoped { config }
        scoped { Appcues(koinScope = this) }
        scoped { AppcuesCoroutineScope(logcues = get()) }
        scoped { Logcues(config.loggingLevel) }
        scoped { StateMachine(appcuesCoroutineScope = get()) }
        scoped { Storage(context = get(), config = get()) }
        scoped { SessionMonitor(config = get(), storage = get(), scope = this) }
        scoped { AutoPropertyDecorator(context = get(), config = get(), storage = get(), sessionMonitor = get()) }
        scoped { ActivityRequestBuilder(config = get(), storage = get(), decorator = get()) }
        scoped { ExperienceRenderer(appcuesCoroutineScope = get(), repository = get(), stateMachine = get()) }
        scoped { AppcuesRepository(appcuesRemoteSource = get(), experienceMapper = get()) }
        scoped {
            AnalyticsTracker(
                appcuesCoroutineScope = get(),
                repository = get(),
                experienceRenderer = get(),
                sessionMonitor = get(),
                activityBuilder = get()
            )
        }
    }
}
