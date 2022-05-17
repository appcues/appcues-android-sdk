package com.appcues.statemachine

import com.appcues.AppcuesConfig
import com.appcues.AppcuesCoroutineScope
import com.appcues.LoggingLevel.NONE
import com.appcues.logging.Logcues
import com.appcues.statemachine.Action.EndExperience
import com.appcues.statemachine.Action.StartExperience
import com.appcues.statemachine.Action.StartStep
import com.appcues.statemachine.State.BeginningStep
import com.appcues.statemachine.State.EndingStep
import com.appcues.statemachine.State.Idling
import com.appcues.statemachine.State.RenderingStep
import com.appcues.statemachine.StepReference.StepOffset
import com.appcues.util.doIfFailure
import com.appcues.util.doIfSuccess
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class StateMachineTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val logcues: Logcues = Logcues(NONE)
    private val scope = AppcuesCoroutineScope(logcues)
    private val config: AppcuesConfig = AppcuesConfig("00000", "123")

    @After
    fun tearDown() {
        scope.coroutineContext.cancelChildren()
    }

    @Test
    fun `initial state should be Idling`() {
        // GIVEN
        val stateMachine = StateMachine(scope, config)

        // THEN
        assertThat(stateMachine.state).isEqualTo(Idling)
    }

    // Standard Transitions

    @Test
    fun `when state is Idling the StartExperience action should transition to RenderingStep state`() = runTest {
        // GIVEN
        var presented = false
        val experience = mockExperience { presented = true }
        val stateMachine = initMachine(Idling)
        val action = StartExperience(experience)
        val targetState = RenderingStep(experience, 0, true)

        // WHEN
        val result = stateMachine.handleAction(action)

        // THEN
        result.doIfSuccess { assertThat(it).isEqualTo(targetState) }
        result.doIfFailure { assert(false) }
        assertThat(presented).isTrue()
        assertThat(stateMachine.state).isEqualTo(targetState)
    }

    @Test
    fun `when state is RenderingStep the StartStep action should transition to RenderingStep state in same group`() = runTest {
        // GIVEN
        var presented = false
        val experience = mockExperience { presented = true }
        val stateMachine = initMachine(RenderingStep(experience, 0, false))
        val action = StartStep(StepOffset(1))
        val targetState = RenderingStep(experience, 1, false)

        // WHEN
        val result = stateMachine.handleAction(action)

        // THEN
        result.doIfSuccess { assertThat(it).isEqualTo(targetState) }
        result.doIfFailure { assert(false) }
        assertThat(presented).isFalse() // same container, no present
        assertThat(stateMachine.state).isEqualTo(targetState)
    }

    @Test
    fun `when state is RenderingStep the StartStep action should transition to RenderingStep state in new group`() = runTest {
        // GIVEN
        var presented = false
        val experience = mockExperience { presented = true }
        val stateMachine = initMachine(RenderingStep(experience, 2, false))
        val action = StartStep(StepOffset(1))
        val targetState = RenderingStep(experience, 3, false)

        // WHEN
        val result = stateMachine.handleAction(action)

        // THEN
        result.doIfSuccess { assertThat(it).isEqualTo(targetState) }
        result.doIfFailure { assert(false) }
        assertThat(presented).isTrue() // new container
        assertThat(stateMachine.state).isEqualTo(targetState)
    }

    @Test
    fun `when state is RenderingStep the EndExperience action should transition to Idling`() = runTest {
        // the @appcues/close action would do this

        // GIVEN
        val experience = mockExperience()
        val stateMachine = initMachine(RenderingStep(experience, 1, false))
        val action = EndExperience(destroyed = false, markComplete = false)
        val targetState = Idling

        // WHEN
        val result = stateMachine.handleAction(action)

        // THEN
        result.doIfSuccess { assertThat(it).isEqualTo(targetState) }
        result.doIfFailure { assert(false) }
        assertThat(stateMachine.state).isEqualTo(targetState)
    }

    @Test
    fun `when state is RenderingStep a destroy of the UI should transition to Idling`() = runTest {
        // the experience Activity being destroyed externally would do this - ex: deeplink elsewhere

        // GIVEN
        val experience = mockExperience()
        val stateMachine = initMachine(RenderingStep(experience, 1, false))
        val action = EndExperience(destroyed = true, markComplete = false)
        val targetState = Idling

        // WHEN
        val result = stateMachine.handleAction(action)

        // THEN
        result.doIfSuccess { assertThat(it).isEqualTo(targetState) }
        result.doIfFailure { assert(false) }
        assertThat(stateMachine.state).isEqualTo(targetState)
    }

    // Helpers
    private fun initMachine(state: State): StateMachine {
        val machine = StateMachine(scope, config, state)
        // this collect on the stateFlow simulates the function of the UI
        // that is required to progress the state machine forward on UI present/dismiss
        scope.launch {
            machine.stateFlow.collectLatest { result ->
                when (result) {
                    is BeginningStep -> {
                        result.presentationComplete.invoke()
                    }
                    is EndingStep -> {
                        result.dismissAndContinue?.invoke()
                    }
                    // ignore other state changes
                    else -> Unit
                }
            }
        }
        return machine
    }
}
