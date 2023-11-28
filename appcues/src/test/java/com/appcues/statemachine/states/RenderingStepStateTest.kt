package com.appcues.statemachine.states

import com.appcues.data.model.Experience
import com.appcues.data.model.StepReference
import com.appcues.statemachine.Action.EndExperience
import com.appcues.statemachine.Action.MoveToStep
import com.appcues.statemachine.Action.StartStep
import com.appcues.statemachine.Error.StepError
import com.appcues.statemachine.State
import com.appcues.statemachine.effects.AwaitDismissEffect
import com.appcues.statemachine.effects.ContinuationEffect
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import org.junit.Test

internal class RenderingStepStateTest {

    @Test
    fun `RenderingStepState SHOULD be instance of STATE`() {
        // GIVEN
        val experience = mockk<Experience>()
        val flatStepIndex = 30
        val state = RenderingStepState(experience, flatStepIndex, mapOf())
        // THEN
        assertThat(state).isInstanceOf(State::class.java)
        assertThat(state.currentExperience).isEqualTo(experience)
        assertThat(state.currentStepIndex).isEqualTo(flatStepIndex)
    }

    @Test
    fun `take SHOULD ignore listed actions`() {
        // GIVEN
        val experience = mockk<Experience>()
        val state = RenderingStepState(experience, 0, mapOf())
        // THEN
        state.assertIgnoredActions(
            listOf(
                MockActions.StartStep,
                MockActions.StartExperience,
                MockActions.RenderStep,
                MockActions.ReportError,
            )
        )
    }

    @Test
    fun `take EndExperience SHOULD transition to EndingStepState AND Continue to EndExperience WHEN destroyed true`() {
        // GIVEN
        val experience = mockk<Experience>()
        val flatStepIndex = 2
        val markComplete = true
        val state = RenderingStepState(experience, flatStepIndex, mapOf())
        val action = EndExperience(markComplete, destroyed = true)
        // WHEN
        val transition = state.take(action)
        // THEN
        transition.assertState(EndingStepState(experience, flatStepIndex, markComplete, null))
        transition.assertEffect(ContinuationEffect(action))
    }

    @Test
    fun `take EndExperience SHOULD transition to EndingStepState AND Await to EndExperience WHEN destroyed false`() {
        // GIVEN
        val experience = mockk<Experience>()
        val flatStepIndex = 2
        val markComplete = true
        val state = RenderingStepState(experience, flatStepIndex, mapOf())
        val action = EndExperience(markComplete, destroyed = false)
        // WHEN
        val transition = state.take(action)
        // THEN
        val awaitDismissEffect = AwaitDismissEffect(action)
        transition.assertState(EndingStepState(experience, flatStepIndex, markComplete, awaitDismissEffect))
        transition.assertEffect(awaitDismissEffect)
    }

    @Test
    fun `take MoveToStep SHOULD transition to EndingStep AND continue WHEN current and new step are from same group`() {
        // GIVEN
        val experience = mockk<Experience> {
            every { areStepsFromDifferentGroup(any(), any()) } returns false
            every { flatSteps } returns listOf(mockk(), mockk())
            every { groupLookup } returns mapOf(0 to 0, 1 to 0)
        }
        val currentIndex = 0
        val nextIndex = 1
        val state = RenderingStepState(experience, currentIndex, mapOf())
        val action = MoveToStep(StepReference.StepIndex(nextIndex))
        val nextAction = StartStep(nextIndex, 0)
        // WHEN
        val transition = state.take(action)
        // THEN
        transition.assertState(EndingStepState(experience, currentIndex, true, null))
        transition.assertEffect(ContinuationEffect(nextAction))
    }

    @Test
    fun `take MoveToStep SHOULD transition to EndingStep AND markComplete false WHEN new step is lower than current`() {
        // GIVEN
        val experience = mockk<Experience> {
            every { areStepsFromDifferentGroup(any(), any()) } returns false
            every { flatSteps } returns listOf(mockk(), mockk())
            every { groupLookup } returns mapOf(0 to 0, 1 to 0)
        }
        val currentIndex = 1
        val nextIndex = 0
        val state = RenderingStepState(experience, currentIndex, mapOf())
        val action = MoveToStep(StepReference.StepIndex(nextIndex))
        val nextAction = StartStep(nextIndex, 0)
        // WHEN
        val transition = state.take(action)
        // THEN
        transition.assertState(EndingStepState(experience, currentIndex, false, null))
        transition.assertEffect(ContinuationEffect(nextAction))
    }

    @Test
    fun `take MoveToStep SHOULD transition to EndingStep AND await WHEN current and new step are from different group`() {
        // GIVEN
        val experience = mockk<Experience> {
            every { areStepsFromDifferentGroup(any(), any()) } returns true
            every { flatSteps } returns listOf(mockk(), mockk())
            every { groupLookup } returns mapOf(0 to 0, 1 to 1)
        }
        val currentIndex = 0
        val nextIndex = 1
        val state = RenderingStepState(experience, currentIndex, mapOf())
        val action = MoveToStep(StepReference.StepIndex(nextIndex))
        val nextAction = StartStep(nextIndex, 1)
        // WHEN
        val transition = state.take(action)
        // THEN
        val awaitDismissEffect = AwaitDismissEffect(nextAction)
        transition.assertState(EndingStepState(experience, currentIndex, true, awaitDismissEffect))
        transition.assertEffect(awaitDismissEffect)
    }

    @Test
    fun `take MoveToStep SHOULD keep StepError WHEN stepReference is invalid`() {
        // GIVEN
        val experience = mockk<Experience> {
            every { areStepsFromDifferentGroup(any(), any()) } returns true
            every { flatSteps } returns listOf(mockk(), mockk())
        }
        val currentIndex = 0
        val nextIndex = 2
        val state = RenderingStepState(experience, currentIndex, mapOf())
        val action = MoveToStep(StepReference.StepIndex(nextIndex))
        // WHEN
        val transition = state.take(action)
        // THEN
        transition.assertState(state)
        transition.assertError(StepError(experience, currentIndex, "Step at ${action.stepReference} does not exist"))
    }

    @Test
    fun `take MoveToStep SHOULD keep StepError WHEN stepContainer for nextStepIndex is invalid`() {
        // GIVEN
        val experience = mockk<Experience> {
            every { areStepsFromDifferentGroup(any(), any()) } returns true
            every { flatSteps } returns listOf(mockk(), mockk())
            // should never happen
            every { groupLookup } returns mapOf(0 to 0)
        }
        val currentIndex = 0
        val nextIndex = 1
        val state = RenderingStepState(experience, currentIndex, mapOf())
        val action = MoveToStep(StepReference.StepIndex(nextIndex))
        // WHEN
        val transition = state.take(action)
        // THEN
        transition.assertState(state)
        transition.assertError(StepError(experience, currentIndex, "StepContainer for nextStepIndex $nextIndex not found"))
    }

    @Test
    fun `take MoveToStep SHOULD transition to EndExperience WHEN action is MoveToStep Offset 1 on the last step`() {
        // GIVEN
        val experience = mockk<Experience> {
            every { areStepsFromDifferentGroup(any(), any()) } returns true
            every { flatSteps } returns listOf(mockk(), mockk())
        }
        val currentIndex = 1
        val state = RenderingStepState(experience, currentIndex, mapOf())
        val action = MoveToStep(StepReference.StepOffset(1))
        // WHEN
        val transition = state.take(action)
        // THEN we actually change from MoveToStep to EndExperience Action
        val awaitDismissEffect = AwaitDismissEffect(EndExperience(markComplete = true, destroyed = false))
        transition.assertState(EndingStepState(experience, currentIndex, true, awaitDismissEffect))
        transition.assertEffect(awaitDismissEffect)
    }
}
