package com.appcues.analytics

import com.appcues.AppcuesConfig
import com.appcues.SessionMonitor
import com.appcues.di.KoinScopePlugin
import org.koin.dsl.ScopeDSL

internal object AnalyticsKoin : KoinScopePlugin {

    override fun ScopeDSL.install(config: AppcuesConfig) {
        scoped { SessionMonitor(scope = this) }
        scoped { AutoPropertyDecorator(context = get(), config = get(), storage = get(), sessionMonitor = get()) }
        scoped { ActivityRequestBuilder(config = get(), storage = get(), decorator = get()) }
        scoped { ExperienceLifecycleTracker(scope = this) }
        scoped {
            AnalyticsTracker(
                appcuesCoroutineScope = get(),
                experienceRenderer = get(),
                sessionMonitor = get(),
                activityBuilder = get(),
                experienceLifecycleTracker = get(),
                repository = get()
            )
        }
    }
}
