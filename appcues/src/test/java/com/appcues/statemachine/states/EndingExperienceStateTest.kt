package com.appcues.statemachine.states

import com.appcues.action.ExperienceAction
import com.appcues.data.model.Experience
import com.appcues.statemachine.Action.Reset
import com.appcues.statemachine.State
import com.appcues.statemachine.effects.ExperienceActionEffect
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import org.junit.Test

internal class EndingExperienceStateTest {

    @Test
    fun `EndingExperienceState SHOULD be instance of STATE`() {
        // GIVEN
        val experience = mockk<Experience>()
        val state = EndingExperienceState(experience, 0, markComplete = true)
        // THEN
        assertThat(state).isInstanceOf(State::class.java)
        assertThat(state.currentExperience).isEqualTo(experience)
        assertThat(state.currentStepIndex).isEqualTo(0)
    }

    @Test
    fun `take SHOULD ignore listed actions`() {
        // GIVEN
        val experience = mockk<Experience>()
        val state = EndingExperienceState(experience, 0, markComplete = true)
        // THEN
        state.assertIgnoredActions(
            listOf(
                MockActions.StartExperience,
                MockActions.StartStep,
                MockActions.MoveToStep,
                MockActions.RenderStep,
                MockActions.EndExperience,
                MockActions.ReportError,
                MockActions.Retry,
            )
        )
    }

    @Test
    fun `take Reset SHOULD transition to IdlingState AND no sideEffects WHEN markedComplete is false`() {
        // GIVEN
        val experience = mockk<Experience>()
        val state = EndingExperienceState(experience, 0, markComplete = false)
        // WHEN
        val transition = state.take(Reset)
        // THEN
        transition.assertState(IdlingState)
        transition.assertEffect(null)
    }

    @Test
    fun `take Reset SHOULD transition to IdlingState AND ExperienceActionEffect WHEN markedComplete is true`() {
        // GIVEN
        val actions = listOf<ExperienceAction>(mockk(), mockk())
        val experience = mockk<Experience> {
            every { completionActions } returns actions
        }
        val state = EndingExperienceState(experience, 0, markComplete = true)
        // WHEN
        val transition = state.take(Reset)
        // THEN
        transition.assertState(IdlingState)
        transition.assertEffect(ExperienceActionEffect(actions))
    }
}
