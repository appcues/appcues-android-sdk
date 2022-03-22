package com.appcues.analytics

import com.appcues.AppcuesConfig
import com.appcues.SessionMonitor
import com.appcues.analytics.storage.ActivityStoring
import com.appcues.analytics.storage.room.ActivityRoomStorage
import com.appcues.di.KoinScopePlugin
import org.koin.dsl.ScopeDSL

internal object AnalyticsKoin : KoinScopePlugin {

    override fun ScopeDSL.install(config: AppcuesConfig) {
        scoped { SessionMonitor(config = get(), storage = get(), scope = this) }
        scoped { AutoPropertyDecorator(context = get(), config = get(), storage = get(), sessionMonitor = get()) }
        scoped { ActivityRequestBuilder(config = get(), storage = get(), decorator = get()) }
        scoped { ExperienceLifecycleTracker(scope = this) }
        scoped< ActivityStoring> { ActivityRoomStorage(context = get()) }
        scoped { ActivityProcessor(repository = get(), storage = get(), gson = get()) }
        scoped {
            AnalyticsTracker(
                appcuesCoroutineScope = get(),
                experienceRenderer = get(),
                sessionMonitor = get(),
                activityBuilder = get(),
                experienceLifecycleTracker = get(),
                activityProcessor = get(),
            )
        }
    }
}
