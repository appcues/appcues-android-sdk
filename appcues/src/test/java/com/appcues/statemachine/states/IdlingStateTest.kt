package com.appcues.statemachine.states

import com.appcues.data.model.Experience
import com.appcues.statemachine.Action.StartExperience
import com.appcues.statemachine.Error.ExperienceError
import com.appcues.statemachine.State
import com.appcues.statemachine.effects.ContinuationEffect
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import org.junit.Test

internal class IdlingStateTest {

    @Test
    fun `IdlingState SHOULD be instance of STATE`() {
        // GIVEN
        val state = IdlingState
        // THEN
        assertThat(state).isInstanceOf(State::class.java)
        assertThat(state.currentExperience).isNull()
        assertThat(state.currentStepIndex).isNull()
    }

    @Test
    fun `take SHOULD ignore listed actions`() {
        // GIVEN
        val state = IdlingState
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
    fun `take StartExperience SHOULD transition to BeginningExperienceState AND continue to Step 0`() {
        // GIVEN
        val state = IdlingState
        val experience = mockk<Experience> {
            every { error } returns null
            every { flatSteps } returns listOf(mockk(), mockk())
        }
        val action = StartExperience(experience)
        // WHEN
        val transition = state.take(action)
        // THEN
        transition.assertState(BeginningExperienceState(experience))
        transition.assertEffect(ContinuationEffect(action))
    }

    @Test
    fun `take StartExperience SHOULD transition to BeginningExperienceState AND continue to Step 0 WHEN error is empty`() {
        // GIVEN
        val state = IdlingState
        val experience = mockk<Experience> {
            every { error } returns ""
            every { flatSteps } returns listOf(mockk(), mockk())
        }
        val action = StartExperience(experience)
        // WHEN
        val transition = state.take(action)
        // THEN
        transition.assertState(BeginningExperienceState(experience))
        transition.assertEffect(ContinuationEffect(action))
    }

    @Test
    fun `take StartExperience SHOULD report error WHEN experience contains error`() {
        // GIVEN
        val state = IdlingState
        val experienceError = "experience error"
        val experience = mockk<Experience> {
            every { error } returns experienceError
            every { stepContainers } returns listOf(mockk(), mockk())
        }
        // WHEN
        val transition = state.take(StartExperience(experience))
        // THEN
        transition.assertState(IdlingState)
        transition.assertError(ExperienceError(experience, experienceError))
    }

    @Test
    fun `take StartExperience SHOULD report error WHEN flatSteps is empty`() {
        // GIVEN
        val state = IdlingState
        val experience = mockk<Experience> {
            every { error } returns null
            every { flatSteps } returns listOf()
        }
        // WHEN
        val transition = state.take(StartExperience(experience))
        // THEN
        transition.assertState(IdlingState)
        transition.assertError(ExperienceError(experience, "Experience has 0 steps"))
    }
}
