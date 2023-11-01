package com.appcues.statemachine.states

import com.appcues.mocks.mockExperience
import com.appcues.statemachine.Action.Retry
import com.appcues.statemachine.Action.StartExperience
import com.appcues.statemachine.State
import com.appcues.statemachine.effects.ContinuationEffect
import com.appcues.statemachine.effects.PresentationEffect
import com.appcues.statemachine.states.IdlingState.next
import com.google.common.truth.Truth.assertThat
import org.junit.Test

internal class FailingStateTest {

    @Test
    fun `FailingState SHOULD be instance of STATE`() {
        // GIVEN
        val experience = mockExperience()
        val stateAtFailure = BeginningStepState(experience, 3, false)
        val retryEffect = PresentationEffect(experience, 3, 1, true)
        val state = FailingState(stateAtFailure, retryEffect)
        // THEN
        assertThat(state).isInstanceOf(State::class.java)
        assertThat(state.currentExperience).isEqualTo(experience)
        assertThat(state.currentStepIndex).isEqualTo(3)
    }

    @Test
    fun `take SHOULD ignore listed actions`() {
        // GIVEN
        val experience = mockExperience()
        val stateAtFailure = BeginningStepState(experience, 3, false)
        val retryEffect = PresentationEffect(experience, 3, 1, true)
        val state = FailingState(stateAtFailure, retryEffect)
        // THEN
        state.assertIgnoredActions(
            listOf(
                MockActions.StartStep,
                MockActions.MoveToStep,
                MockActions.RenderStep,
                MockActions.Reset,
                MockActions.ReportError,
            )
        )
    }

    @Test
    fun `take StartExperience SHOULD transition to Idling AND continue to StartExperience`() {
        // GIVEN
        val failingExperience = mockExperience()
        val stateAtFailure = BeginningStepState(failingExperience, 3, false)
        val retryEffect = PresentationEffect(failingExperience, 3, 1, true)
        val state = FailingState(stateAtFailure, retryEffect)
        val experience = mockExperience()
        val action = StartExperience(experience)
        // WHEN
        val transition = state.take(action)
        // THEN
        transition.assertState(IdlingState)
        transition.assertEffect(ContinuationEffect(action))
    }

    @Test
    fun `take Retry SHOULD transition to stored value`() {
        // GIVEN
        val failingExperience = mockExperience()
        val stateAtFailure = BeginningStepState(failingExperience, 3, false)
        val retryEffect = PresentationEffect(failingExperience, 3, 1, true)
        val state = FailingState(stateAtFailure, retryEffect)
        val action = Retry
        // WHEN
        val transition = state.take(action)
        // THEN
        assertThat(transition).isEqualTo(state.next(stateAtFailure, retryEffect))
    }
}
