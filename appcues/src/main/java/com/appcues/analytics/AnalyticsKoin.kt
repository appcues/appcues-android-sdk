package com.appcues.analytics

import com.appcues.SessionMonitor
import com.appcues.analytics.AnalyticsQueueProcessor.AnalyticsQueueScheduler
import com.appcues.analytics.AnalyticsQueueProcessor.QueueScheduler
import com.appcues.di.KoinScopePlugin
import org.koin.dsl.ScopeDSL

internal object AnalyticsKoin : KoinScopePlugin {

    override fun ScopeDSL.install() {
        scoped { SessionMonitor(scope = this) }
        scoped { SessionRandomizer() }
        scoped {
            AutoPropertyDecorator(
                contextResources = get(),
                config = get(),
                storage = get(),
                sessionMonitor = get(),
                sessionRandomizer = get(),
            )
        }
        scoped { ActivityRequestBuilder(config = get(), storage = get(), decorator = get()) }
        scoped { ExperienceLifecycleTracker(scope = this) }
        scoped { AnalyticsPolicy(sessionMonitor = get(), appcuesCoroutineScope = get(), experienceRenderer = get(), logcues = get()) }
        scoped { ActivityScreenTracking(context = get(), analyticsTracker = get(), logcues = get()) }
        scoped<QueueScheduler> { AnalyticsQueueScheduler() }
        scoped {
            AnalyticsQueueProcessor(
                appcuesCoroutineScope = get(),
                experienceRenderer = get(),
                repository = get(),
                analyticsQueueScheduler = get()
            )
        }
        scoped {
            AnalyticsTracker(
                appcuesCoroutineScope = get(),
                activityBuilder = get(),
                analyticsPolicy = get(),
                analyticsQueueProcessor = get(),
            )
        }
    }
}
