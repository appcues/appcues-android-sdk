package com.appcues

import com.appcues.action.ActionRegistry
import com.appcues.analytics.ActivityScreenTracking
import com.appcues.analytics.AnalyticsTracker
import com.appcues.debugger.AppcuesDebuggerManager
import com.appcues.logging.Logcues
import com.appcues.mocks.storageMockk
import com.appcues.statemachine.StateMachine
import com.appcues.trait.TraitRegistry
import com.appcues.ui.ExperienceRenderer
import com.appcues.util.LinkOpener
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.module
import org.koin.mp.KoinPlatformTools
import java.util.UUID

// modeled after KoinTestRule, but adapted for our Scoped dependency concept
// https://github.com/InsertKoinIO/koin/blob/main/core/koin-test-junit4/src/main/kotlin/org/koin/test/KoinTestRule.kt
//
// also handles coroutine usage of main dispatcher in unit test https://stackoverflow.com/a/70865589 via
// Dispatchers.setMain and Dispatchers.resetMain calls below
@OptIn(ExperimentalCoroutinesApi::class)
class AppcuesKoinTestRule : TestWatcher() {

    lateinit var scope: Scope

    override fun starting(description: Description) {
        // close any existing instance
        KoinPlatformTools.defaultContext().getOrNull()?.close()

        Dispatchers.setMain(StandardTestDispatcher())

        startKoin {
            val scopeId = UUID.randomUUID().toString()
            scope = koin.getOrCreateScope(scopeId = scopeId, qualifier = named(scopeId))
            modules(
                module {
                    scope(named(scope.id)) {
                        scoped { AppcuesConfig("00000", "123") }
                        scoped { scope }
                        scoped { AppcuesCoroutineScope(get()) }
                        scoped { mockk<AnalyticsTracker>(relaxed = true) }
                        scoped { mockk<SessionMonitor>(relaxed = true) }
                        scoped { mockk<Logcues>(relaxed = true) }
                        scoped { mockk<ExperienceRenderer>(relaxed = true) }
                        scoped { mockk<AppcuesDebuggerManager>(relaxed = true) }
                        scoped { mockk<ActivityScreenTracking>(relaxed = true) }
                        scoped { mockk<TraitRegistry>(relaxed = true) }
                        scoped { mockk<ActionRegistry>(relaxed = true) }
                        scoped { mockk<DeeplinkHandler>(relaxed = true) }
                        scoped { mockk<StateMachine>(relaxed = true) }
                        scoped { mockk<LinkOpener>(relaxed = true) }
                        scoped { storageMockk() }
                        scoped { mockk<Appcues>(relaxed = true) }
                    }
                }
            )
        }
    }

    override fun finished(description: Description) {
        stopKoin()
        Dispatchers.resetMain()
    }
}
