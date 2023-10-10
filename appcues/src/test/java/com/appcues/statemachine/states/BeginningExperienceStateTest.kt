package com.appcues.statemachine.states

import com.appcues.data.model.Experience
import com.appcues.statemachine.Action.StartExperience
import com.appcues.statemachine.State
import com.appcues.statemachine.effects.PresentationEffect
import com.google.common.truth.Truth.assertThat
import io.mockk.mockk
import org.junit.Test

internal class BeginningExperienceStateTest {

    @Test
    fun `BeginningExperienceState SHOULD be instance of STATE`() {
        // GIVEN
        val experience = mockk<Experience>()
        val state = BeginningExperienceState(experience)
        // THEN
        assertThat(state).isInstanceOf(State::class.java)
        assertThat(state.currentExperience).isEqualTo(experience)
        assertThat(state.currentStepIndex).isNull()
    }

    @Test
    fun `take SHOULD ignore listed actions`() {
        // GIVEN
        val experience = mockk<Experience>()
        val state = BeginningExperienceState(experience)
        // THEN
        state.assertIgnoredActions(
            listOf(
                MockActions.StartStep,
                MockActions.MoveToStep,
                MockActions.RenderStep,
                MockActions.EndExperience,
                MockActions.Reset,
                MockActions.ReportError,
            )
        )
    }

    @Test
    fun `take StartExperience SHOULD transition to BeginningStepState`() {
        // GIVEN
        val experience = mockk<Experience>()
        val state = BeginningExperienceState(experience)
        // WHEN
        val transition = state.take(StartExperience(experience))
        // THEN
        transition.assertState(BeginningStepState(experience, 0, true))
        transition.assertEffect(PresentationEffect(experience, 0, 0, true))
    }
}
