package com.appcues.analytics

import com.appcues.SessionMonitor
import com.appcues.analytics.AnalyticsQueue.DefaultQueueScheduler
import com.appcues.analytics.AnalyticsQueueProcessor.AnalyticsQueueScheduler
import com.appcues.analytics.AnalyticsQueueProcessor.QueueScheduler
import com.appcues.di.KoinScopePlugin
import com.appcues.qualification.DefaultQualificationService
import com.appcues.rendering.ExperienceRendering
import com.appcues.session.DefaultSessionService
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
        factory { ExperienceLifecycleTracker(scope = this) }
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
                sessionMonitor = get(),
                analyticsQueueProcessor = get(),
            )
        }

        scoped {
            Analytics(
                coroutineScope = get(),
                queue = get(),
                qualificationService = get(),
                renderingService = get(),
                sessionService = get(),
                activityBuilder = get(),
            )
        }

        scoped {
            AnalyticsQueue(
                scheduler = DefaultQueueScheduler()
            )
        }

        scoped<QualificationService> {
            DefaultQualificationService(
                appcuesRemoteSource = get(),
                appcuesLocalSource = get(),
                experienceMapper = get(),
                config = get(),
                logcues = get(),
            )
        }

        scoped<RenderingService> {
            ExperienceRendering(
                config = get(),
                stateMachineDirectory = get(),
                stateMachineFactory = get(),
            )
        }

        scoped<SessionService> {
            DefaultSessionService(
                config = get(),
                sessionLocalSource = get(),
                sessionRandomizer = get(),
            )
        }

        scoped<ActivityBuilder> {
            DefaultActivityBuilder(
                config = get(),
                contextResources = get(),
            )
        }
    }
}
