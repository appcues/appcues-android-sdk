package com.appcues.analytics

import com.appcues.SessionMonitor
import com.appcues.analytics.AnalyticsQueue.AnalyticsQueueScheduler
import com.appcues.analytics.AnalyticsQueue.QueueScheduler
import com.appcues.di.KoinScopePlugin
import org.koin.dsl.ScopeDSL

internal object AnalyticsKoin : KoinScopePlugin {

    override fun ScopeDSL.install() {
        scoped { SessionMonitor(scope = this) }
        scoped { SessionRandomizer() }
        scoped { AutoPropertyDecorator(get(), get(), get(), get(), get()) }
        scoped { ActivityRequestBuilder(config = get(), storage = get(), decorator = get()) }
        scoped { ExperienceLifecycleTracker(scope = this) }
        scoped { AnalyticsPolicy(sessionMonitor = get()) }
        scoped { ActivityScreenTracking(context = get(), analyticsTracker = get(), logcues = get()) }
        scoped<QueueScheduler> { AnalyticsQueueScheduler() }
        scoped { AnalyticsQueue(scheduler = get()) }
        scoped { AnalyticsTracker(get(), get(), get(), get(), get(), get()) }
    }
}
