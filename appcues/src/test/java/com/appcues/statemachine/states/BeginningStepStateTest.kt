package com.appcues.statemachine.states

import com.appcues.data.model.Experience
import com.appcues.statemachine.Action.RenderStep
import com.appcues.statemachine.State
import com.google.common.truth.Truth.assertThat
import io.mockk.mockk
import org.junit.Test

internal class BeginningStepStateTest {

    @Test
    fun `BeginningStepState SHOULD be instance of STATE`() {
        // GIVEN
        val experience = mockk<Experience>()
        val flatStepIndex = 30
        val state = BeginningStepState(experience, flatStepIndex)
        // THEN
        assertThat(state).isInstanceOf(State::class.java)
        assertThat(state.currentExperience).isEqualTo(experience)
        assertThat(state.currentStepIndex).isEqualTo(flatStepIndex)
    }

    @Test
    fun `take SHOULD ignore listed actions`() {
        // GIVEN
        val experience = mockk<Experience>()
        val state = BeginningStepState(experience, 0)
        // THEN
        state.assertIgnoredActions(
            listOf(
                MockActions.MoveToStep,
                MockActions.StartExperience,
                MockActions.StartStep,
                MockActions.EndExperience,
                MockActions.Reset,
                MockActions.ReportError,
                MockActions.Retry,
            )
        )
    }

    @Test
    fun `take RenderStep SHOULD transition to RenderingStepState`() {
        // GIVEN
        val experience = mockk<Experience>()
        val state = BeginningStepState(experience, 0)
        val metadata = mapOf<String, Any?>("a" to "b")
        // WHEN
        val transition = state.take(RenderStep(metadata))
        // THEN
        transition.assertState(RenderingStepState(experience, 0, metadata))
        transition.assertEffect(null)
    }
}
