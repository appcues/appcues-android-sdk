package com.appcues.analytics

import com.appcues.Appcues
import com.appcues.AppcuesConfig
import com.appcues.AppcuesCoroutineScope
import com.appcues.DeepLinkHandler
import com.appcues.SessionMonitor
import com.appcues.action.ActionProcessor
import com.appcues.action.ActionRegistry
import com.appcues.debugger.AppcuesDebuggerManager
import com.appcues.logging.Logcues
import com.appcues.mocks.mockExperience
import com.appcues.mocks.storageMockk
import com.appcues.rules.MainDispatcherRule
import com.appcues.statemachine.Action.EndExperience
import com.appcues.statemachine.Action.StartStep
import com.appcues.statemachine.State
import com.appcues.statemachine.State.EndingStep
import com.appcues.statemachine.State.RenderingStep
import com.appcues.statemachine.StateMachine
import com.appcues.statemachine.StepReference.StepOffset
import com.appcues.trait.TraitRegistry
import com.appcues.ui.ExperienceRenderer
import com.appcues.util.LinkOpener
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.module
import org.koin.mp.KoinPlatformTools
import org.koin.test.KoinTest
import java.util.UUID

@ExperimentalCoroutinesApi
internal class ExperienceLifecycleTrackerTest : KoinTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    @After
    fun shutdown() {
        stopKoin()
    }

    @Test
    fun `RenderingStep SHOULD track step_completed WHEN action is StartStep`() = runTest {
        // GIVEN
        val experience = mockExperience()
        val initialState = RenderingStep(experience, 0, true)
        val action = StartStep(StepOffset(1))
        val scope = initScope(initialState)
        val stateMachine: StateMachine = scope.get()
        val analyticsTracker: AnalyticsTracker = scope.get()

        // WHEN
        stateMachine.handleAction(action)

        // THEN
        verify { analyticsTracker.track("appcues:v2:step_completed", any(), any(), any()) }
    }

    @Test
    fun `RenderingStep SHOULD NOT track step_completed WHEN action is EndExperience markComplete=false AND not on last step`() = runTest {
        // GIVEN
        val experience = mockExperience()
        val initialState = RenderingStep(experience, 0, true)
        val action = EndExperience(destroyed = false, markComplete = false)
        val scope = initScope(initialState)
        val stateMachine: StateMachine = scope.get()
        val analyticsTracker: AnalyticsTracker = scope.get()

        // WHEN
        stateMachine.handleAction(action)

        // THEN
        verify(exactly = 0) { analyticsTracker.track("appcues:v2:step_completed", any(), any(), any()) }
    }

    @Test
    fun `RenderingStep SHOULD track step_completed WHEN action is EndExperience markComplete=true on last step`() = runTest {
        // GIVEN
        val experience = mockExperience()
        val initialState = RenderingStep(experience, 3, false)
        val action = EndExperience(destroyed = false, markComplete = true)
        val scope = initScope(initialState)
        val stateMachine: StateMachine = scope.get()
        val analyticsTracker: AnalyticsTracker = scope.get()

        // WHEN
        stateMachine.handleAction(action)

        // THEN
        verify { analyticsTracker.track("appcues:v2:step_completed", any(), any(), any()) }
    }

    @Test
    fun `RenderingStep SHOULD track step_completed WHEN action is EndExperience markComplete=true AND not on last step`() = runTest {
        // GIVEN
        val experience = mockExperience()
        val initialState = RenderingStep(experience, 0, true)
        val action = EndExperience(destroyed = false, markComplete = true)
        val scope = initScope(initialState)
        val stateMachine: StateMachine = scope.get()
        val analyticsTracker: AnalyticsTracker = scope.get()

        // WHEN
        stateMachine.handleAction(action)

        // THEN
        verify { analyticsTracker.track("appcues:v2:step_completed", any(), any(), any()) }
    }

    // Helpers
    private suspend fun initScope(state: State): Scope {
        val scopeId = UUID.randomUUID().toString()
        val app = createKoinApp(scopeId, state)
        val scope = app.koin.getScope(scopeId)
        val machine: StateMachine = scope.get()
        val coroutineScope: AppcuesCoroutineScope = scope.get()

        coroutineScope.launch {
            // this collect on the stateFlow simulates the function of the UI
            // that is required to progress the state machine forward on UI present/dismiss
            machine.stateFlow.collect {
                when (it) {
                    is EndingStep -> {
                        it.dismissAndContinue?.invoke()
                    }
                    // ignore other state changes
                    else -> Unit
                }
            }
        }

        coroutineScope.launch {
            scope.get<ExperienceLifecycleTracker>().start(machine, {}, UnconfinedTestDispatcher())
        }

        return scope
    }

    private fun createKoinApp(scopeId: String, state: State): KoinApplication {
        // close any existing instance
        KoinPlatformTools.defaultContext().getOrNull()?.close()

        return startKoin {
            val scope = koin.getOrCreateScope(scopeId = scopeId, qualifier = named(scopeId))
            modules(
                module {
                    scope(named(scopeId)) {
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
                        scoped { mockk<DeepLinkHandler>(relaxed = true) }
                        scoped { mockk<LinkOpener>(relaxed = true) }
                        scoped { storageMockk() }
                        scoped { mockk<Appcues>(relaxed = true) }
                        scoped { mockk<ActionProcessor>(relaxed = true) }
                        factory { ExperienceLifecycleTracker(scope) }
                        scoped { StateMachine(get(), get(), get(), get(), state) }
                    }
                }
            )
        }
    }
}
