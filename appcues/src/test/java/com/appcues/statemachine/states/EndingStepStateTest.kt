package com.appcues.statemachine.states

import com.appcues.data.model.Experience
import com.appcues.statemachine.Action.EndExperience
import com.appcues.statemachine.Action.Reset
import com.appcues.statemachine.Action.StartStep
import com.appcues.statemachine.State
import com.appcues.statemachine.effects.ContinuationEffect
import com.appcues.statemachine.effects.PresentationEffect
import com.google.common.truth.Truth.assertThat
import io.mockk.mockk
import org.junit.Test

internal class EndingStepStateTest {

    @Test
    fun `EndingStepState SHOULD be instance of STATE`() {
        // GIVEN
        val experience = mockk<Experience>()
        val currentStep = 2
        val state = EndingStepState(experience, currentStep, true, null)
        // THEN
        assertThat(state).isInstanceOf(State::class.java)
        assertThat(state.currentExperience).isEqualTo(experience)
        assertThat(state.currentStepIndex).isEqualTo(currentStep)
    }

    @Test
    fun `take SHOULD ignore listed actions`() {
        // GIVEN
        val experience = mockk<Experience>()
        val currentStep = 2
        val state = EndingStepState(experience, currentStep, true, null)
        // THEN
        state.assertIgnoredActions(
            listOf(
                MockActions.MoveToStep,
                MockActions.StartExperience,
                MockActions.RenderStep,
                MockActions.Reset,
                MockActions.ReportError,
            )
        )
    }

    @Test
    fun `take EndExperience SHOULD transition to EndingExperienceState AND continue Reset`() {
        // GIVEN
        val experience = mockk<Experience>()
        val currentStep = 2
        val state = EndingStepState(experience, currentStep, true, null)
        val action = EndExperience(markComplete = true, destroyed = false, trackAnalytics = false)
        // WHEN
        val transition = state.take(action)
        // THEN
        transition.assertState(EndingExperienceState(experience, currentStep, action.markComplete, action.trackAnalytics))
        transition.assertEffect(ContinuationEffect(Reset))
    }

    @Test
    fun `take StartStep SHOULD transition to BeginningStepState AND Present WITH shouldPresent false WHEN null dismissEffect`() {
        // GIVEN
        val experience = mockk<Experience>()
        val currentStep = 2
        val nextStep = 3
        val state = EndingStepState(experience, currentStep, true, null)
        val action = StartStep(nextStep, 2)
        // WHEN
        val transition = state.take(action)
        // THEN
        transition.assertState(BeginningStepState(experience, nextStep))
        transition.assertEffect(
            PresentationEffect(
                experience,
                action.nextFlatStepIndex,
                action.nextStepContainerIndex,
                false
            )
        )
    }

    @Test
    fun `take StartStep SHOULD transition to BeginningStepState AND Present WITH shouldPresent true WHEN non-null dismissEffect`() {
        // GIVEN
        val experience = mockk<Experience>()
        val currentStep = 2
        val nextStep = 3
        val state = EndingStepState(experience, currentStep, true, mockk())
        val action = StartStep(nextStep, 2)
        // WHEN
        val transition = state.take(action)
        // THEN
        transition.assertState(BeginningStepState(experience, nextStep))
        transition.assertEffect(
            PresentationEffect(
                experience,
                action.nextFlatStepIndex,
                action.nextStepContainerIndex,
                true
            )
        )
    }
}
