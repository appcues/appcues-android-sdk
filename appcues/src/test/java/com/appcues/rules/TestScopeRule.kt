package com.appcues.rules

import com.appcues.Appcues
import com.appcues.AppcuesConfig
import com.appcues.AppcuesCoroutineScope
import com.appcues.DeepLinkHandler
import com.appcues.SessionMonitor
import com.appcues.action.ActionProcessor
import com.appcues.action.ActionRegistry
import com.appcues.analytics.ActivityScreenTracking
import com.appcues.analytics.AnalyticsTracker
import com.appcues.analytics.ExperienceLifecycleTracker
import com.appcues.debugger.AppcuesDebuggerManager
import com.appcues.di.AppcuesModule
import com.appcues.di.Bootstrap
import com.appcues.di.scope.AppcuesScope
import com.appcues.di.scope.AppcuesScopeDSL
import com.appcues.logging.LogcatDestination
import com.appcues.logging.Logcues
import com.appcues.mocks.storageMockk
import com.appcues.push.PushDeeplinkHandler
import com.appcues.push.PushOpenedProcessor
import com.appcues.statemachine.StateMachine
import com.appcues.trait.TraitRegistry
import com.appcues.ui.ExperienceRenderer
import com.appcues.util.ContextWrapper
import com.appcues.util.LinkOpener
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import org.junit.rules.TestWatcher
import org.junit.runner.Description

internal class TestScopeRule : TestWatcher() {

    lateinit var scope: AppcuesScope

    override fun starting(description: Description) {
        scope = Bootstrap.start(
            modules = arrayListOf(
                object : AppcuesModule {
                    override fun AppcuesScopeDSL.install() {
                        scoped { mockk<LogcatDestination>(relaxed = true) }
                        scoped { AppcuesConfig("00000", "123") }
                        scoped<CoroutineScope> { AppcuesCoroutineScope(get()) }
                        scoped { mockk<AnalyticsTracker>(relaxed = true) }
                        scoped { mockk<SessionMonitor>(relaxed = true) }
                        scoped { mockk<Logcues>(relaxed = true) }
                        scoped { mockk<ExperienceRenderer>(relaxed = true) }
                        scoped { mockk<AppcuesDebuggerManager>(relaxed = true) }
                        scoped { mockk<ActivityScreenTracking>(relaxed = true) }
                        scoped { mockk<TraitRegistry>(relaxed = true) }
                        scoped { mockk<ActionRegistry>(relaxed = true) }
                        scoped { mockk<DeepLinkHandler>(relaxed = true) }
                        scoped { mockk<PushDeeplinkHandler>(relaxed = true) }
                        scoped { mockk<PushOpenedProcessor>(relaxed = true) }
                        scoped { mockk<StateMachine>(relaxed = true) }
                        scoped { mockk<LinkOpener>(relaxed = true) }
                        scoped { mockk<ContextWrapper>(relaxed = true) }
                        scoped { storageMockk() }
                        scoped { mockk<Appcues>(relaxed = true) }
                        scoped { mockk<ActionProcessor>(relaxed = true) }
                        factory { mockk<ExperienceLifecycleTracker>(relaxed = true) }
                    }
                }
            )
        )
    }

    override fun finished(description: Description) = Unit
}
