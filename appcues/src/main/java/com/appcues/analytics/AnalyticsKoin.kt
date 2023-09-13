package com.appcues.analytics

import com.appcues.analytics.AnalyticsQueue.DefaultQueueScheduler
import com.appcues.di.KoinScopePlugin
import com.appcues.experiences.Experiences
import com.appcues.qualification.DefaultQualificationService
import com.appcues.rendering.DefaultRenderingService
import com.appcues.session.DefaultSessionService
import org.koin.dsl.ScopeDSL

internal object AnalyticsKoin : KoinScopePlugin {

    override fun ScopeDSL.install() {
        scoped { SessionRandomizer() }

        scoped { ActivityScreenTracking(context = get(), analytics = get(), logcues = get()) }

        scoped {
            Analytics(
                coroutineScope = get(),
                logcues = get(),
                queue = get(),
                qualificationService = get(),
                renderingService = get(),
                sessionService = get(),
                activityBuilder = get(),
            )
        }

        scoped {
            Experiences(
                remote = get(),
                experienceMapper = get(),
                sessionService = get(),
                renderingService = get(),
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
            DefaultRenderingService(
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
