package com.appcues.analytics

import com.appcues.SessionMonitor
import com.appcues.analytics.AnalyticsQueueProcessor.AnalyticsQueueScheduler
import com.appcues.analytics.AnalyticsQueueProcessor.QueueScheduler
import com.appcues.di.AppcuesModule
import com.appcues.di.scope.AppcuesScopeDSL

internal object AnalyticsModule : AppcuesModule {

    override fun AppcuesScopeDSL.install() {
        scoped { SessionMonitor(scope = scope) }
        scoped { SessionRandomizer() }
        scoped {
            AutoPropertyDecorator(
                contextWrapper = get(),
                config = get(),
                storage = get(),
                sessionMonitor = get(),
                sessionRandomizer = get(),
            )
        }
        scoped { ActivityRequestBuilder(config = get(), storage = get(), decorator = get()) }
        factory { ExperienceLifecycleTracker(scope = scope) }
        scoped { ActivityScreenTracking(context = get(), analyticsTracker = get(), logcues = get()) }
        factory<QueueScheduler> { AnalyticsQueueScheduler() }
        scoped {
            AnalyticsQueueProcessor(
                appcuesCoroutineScope = get(),
                experienceRenderer = get(),
                repository = get(),
                analyticsQueueScheduler = get(),
                priorityQueueScheduler = get(),
            )
        }
        scoped {
            AnalyticsTracker(
                appcuesCoroutineScope = get(),
                activityBuilder = get(),
                sessionMonitor = get(),
                analyticsQueueProcessor = get(),
            )
        }
    }
}
