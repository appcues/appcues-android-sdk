package com.appcues.analytics

import com.appcues.Appcues
import com.appcues.AppcuesConfig
import com.appcues.AppcuesCoroutineScope
import com.appcues.DeepLinkHandler
import com.appcues.SessionMonitor
import com.appcues.action.ActionProcessor
import com.appcues.action.ActionRegistry
import com.appcues.data.model.StepReference.StepOffset
import com.appcues.debugger.AppcuesDebuggerManager
import com.appcues.di.AppcuesModule
import com.appcues.di.Bootstrap
import com.appcues.di.scope.AppcuesScope
import com.appcues.di.scope.AppcuesScopeDSL
import com.appcues.di.scope.get
import com.appcues.logging.Logcues
import com.appcues.mocks.mockExperience
import com.appcues.mocks.mockLocalizedExperience
import com.appcues.mocks.storageMockk
import com.appcues.rules.MainDispatcherRule
import com.appcues.statemachine.Action.EndExperience
import com.appcues.statemachine.Action.StartExperience
import com.appcues.statemachine.Action.StartStep
import com.appcues.statemachine.State
import com.appcues.statemachine.StateMachine
import com.appcues.statemachine.states.EndingStepState
import com.appcues.statemachine.states.IdlingState
import com.appcues.statemachine.states.RenderingStepState
import com.appcues.trait.TraitRegistry
import com.appcues.ui.ExperienceRenderer
import com.appcues.util.LinkOpener
import com.google.common.truth.Truth.assertThat
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
internal class ExperienceLifecycleTrackerTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    @Test
    fun `RenderingStep SHOULD track step_completed WHEN action is StartStep`() = runTest {
        // GIVEN
        val experience = mockExperience()
        val initialState = RenderingStepState(experience, 0, true, mutableMapOf())
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
        val initialState = RenderingStepState(experience, 0, true, mutableMapOf())
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
        val initialState = RenderingStepState(experience, 0, true, mutableMapOf())
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
        val initialState = RenderingStepState(experience, 0, true, mutableMapOf())
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
    fun `Idling SHOULD track events WITH locale properties WHEN action is StartExperience`() = runTest {
        // verify locale properties from context are coming through
        // GIVEN
        val experience = mockLocalizedExperience("France", "b636348b-648f-4e0d-a06a-6ebb2fe2b7f8")
        val initialState = IdlingState
        val action = StartExperience(experience)
        val scope = initScope(initialState)
        val stateMachine: StateMachine = scope.get()
        val analyticsTracker: AnalyticsTracker = scope.get()

        // WHEN
        stateMachine.handleAction(action)

        // THEN
        val properties = mutableListOf<Map<String, Any>>()
        verify { analyticsTracker.track(any(), capture(properties), any(), any()) }
        assertThat(properties).isNotEmpty()
        properties.forEach {
            // there are multiple flow events during the experience start (experience start and step start)
            // and all should have the specified locale information
            assertThat("France").isEqualTo(it["localeName"])
            assertThat("b636348b-648f-4e0d-a06a-6ebb2fe2b7f8").isEqualTo(it["localeId"])
        }
    }

    // Helpers
    private suspend fun initScope(state: State): AppcuesScope {
        val scope = createScope(state)
        val machine: StateMachine = scope.get()
        val coroutineScope: AppcuesCoroutineScope = scope.get()

        coroutineScope.launch {
            // this collect on the stateFlow simulates the function of the UI
            // that is required to progress the state machine forward on UI present/dismiss
            machine.stateFlow.collect {
                when (it) {
                    is EndingStepState -> {
                        it.onUiDismissed?.invoke()
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

    private fun createScope(state: State): AppcuesScope {
        return Bootstrap.start(
            modules = arrayListOf(object : AppcuesModule {
                override fun AppcuesScopeDSL.install() {
                    scoped { AppcuesConfig("00000", "123") }
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
                    scoped { StateMachine(get(), get(), state, mockk(relaxed = true)) }
                }
            })
        )
    }
}
