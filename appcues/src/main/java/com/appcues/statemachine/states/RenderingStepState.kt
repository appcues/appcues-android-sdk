package com.appcues.statemachine.states

import com.appcues.data.model.Experience
import com.appcues.data.model.StepReference.StepOffset
import com.appcues.statemachine.Action
import com.appcues.statemachine.Action.EndExperience
import com.appcues.statemachine.Action.MoveToStep
import com.appcues.statemachine.Action.StartStep
import com.appcues.statemachine.Error.StepError
import com.appcues.statemachine.State
import com.appcues.statemachine.Transition
import com.appcues.statemachine.effects.AwaitEffect
import com.appcues.statemachine.effects.ContinuationEffect

internal data class RenderingStepState(
    val experience: Experience,
    val flatStepIndex: Int,
    val metadata: Map<String, Any?>
) : State {

    override val currentExperience: Experience
        get() = experience
    override val currentStepIndex: Int
        get() = flatStepIndex

    override fun take(action: Action): Transition? {
        return when (action) {
            is MoveToStep -> if (shouldEndExperience(action)) {
                toEndingExperience(EndExperience(markComplete = true, destroyed = false))
            } else {
                toEndingStep(action)
            }
            is EndExperience -> {
                toEndingExperience(action)
            }
            else -> null
        }
    }

    private fun shouldEndExperience(action: MoveToStep): Boolean {
        val isLastStep = flatStepIndex == experience.flatSteps.size - 1
        val isContinuingToNextStep = action.stepReference is StepOffset && action.stepReference.offset == 1

        return isLastStep && isContinuingToNextStep
    }

    private fun toEndingStep(action: MoveToStep): Transition {
        val nextStepIndex = action.stepReference.getIndex(experience, flatStepIndex)
            ?: return keep(StepError(experience, flatStepIndex, "Step at ${action.stepReference} does not exist"))

        // should never happen but in case next step group lookup returns null we handle it here
        val stepContainerIndex = experience.groupLookup[nextStepIndex]
            ?: return keep(StepError(experience, flatStepIndex, "StepContainer for nextStepIndex $nextStepIndex not found"))

        // we want to mark complete when the next step is greater than current because
        // it means we are progressing forward.
        val markComplete = nextStepIndex > flatStepIndex
        return if (experience.areStepsFromDifferentGroup(flatStepIndex, nextStepIndex)) {
            // in different groups we want to wait ui to dismiss
            val nextAction = StartStep(nextStepIndex, stepContainerIndex, true)
            val awaitEffect = AwaitEffect(nextAction)

            next(EndingStepState(experience, flatStepIndex, markComplete, awaitEffect), awaitEffect)
        } else {
            val nextAction = StartStep(nextStepIndex, stepContainerIndex, false)
            // in same group we can continue to StartStep internally
            next(EndingStepState(experience, flatStepIndex, markComplete, null), ContinuationEffect(nextAction))
        }
    }

    private fun toEndingExperience(action: EndExperience): Transition {
        return if (action.destroyed) {
            // this means the activity hosting the experience was destroyed externally (i.e. deep link) and we should
            // immediately transition to EndingExperience - not rely on the UI to do it for us (it's gone)
            next(EndingStepState(experience, flatStepIndex, action.markComplete, null), ContinuationEffect(action))
        } else {
            // otherwise, its a natural end of experience from an in-experience action / dismiss
            // and should be communicated to the UI layer to dismiss itself.
            //
            // instead of using sideEffect we pass EndExperience on EndingStep
            // then AppcuesViewModel will continue to EndExperience when appropriate
            val awaitEffect = AwaitEffect(action)

            next(EndingStepState(experience, flatStepIndex, action.markComplete, awaitEffect), awaitEffect)
        }
    }
}
