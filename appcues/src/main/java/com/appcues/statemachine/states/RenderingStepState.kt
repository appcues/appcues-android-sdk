package com.appcues.statemachine.states

import com.appcues.data.model.Experience
import com.appcues.data.model.StepReference.StepOffset
import com.appcues.statemachine.Action
import com.appcues.statemachine.Action.EndExperience
import com.appcues.statemachine.Action.MoveToStep
import com.appcues.statemachine.Action.Reset
import com.appcues.statemachine.Action.StartStep
import com.appcues.statemachine.Error.StepError
import com.appcues.statemachine.State
import com.appcues.statemachine.Transition
import com.appcues.statemachine.effects.AwaitDismissEffect
import com.appcues.statemachine.effects.ContinuationEffect
import com.appcues.statemachine.effects.PresentationEffect

internal data class RenderingStepState(
    val experience: Experience,
    val flatStepIndex: Int,
    val metadata: Map<String, Any?>,
    val isFirst: Boolean = false,
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
            is Reset -> {
                toBeginningStep()
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
            val nextAction = StartStep(nextStepIndex, stepContainerIndex)
            val awaitDismissEffect = AwaitDismissEffect(nextAction)

            next(EndingStepState(experience, flatStepIndex, markComplete, awaitDismissEffect), awaitDismissEffect)
        } else {
            val nextAction = StartStep(nextStepIndex, stepContainerIndex)
            // in same group we can continue to StartStep internally
            next(EndingStepState(experience, flatStepIndex, markComplete, null), ContinuationEffect(nextAction))
        }
    }

    private fun toBeginningStep(): Transition {
        val stepContainerIndex = experience.groupLookup[flatStepIndex]
            ?: return exit(StepError(experience, flatStepIndex, "StepContainer for stepIndex $flatStepIndex not found"))

        return next(
            state = BeginningStepState(experience, flatStepIndex, false),
            sideEffect = PresentationEffect(
                experience = experience,
                flatStepIndex = flatStepIndex,
                stepContainerIndex = stepContainerIndex,
                shouldPresent = true
            )
        )
    }

    private fun toEndingExperience(action: EndExperience): Transition {
        // passing in the effect on EndingStepState to ensure the UI will dismiss
        // even though we might not be waiting for the completion before moving to
        // the next step.
        val awaitDismissEffect = AwaitDismissEffect(action)
        return if (action.destroyed) {
            // this means the activity hosting the experience was destroyed externally (i.e. deep link) and we should
            // immediately transition to EndingExperience - not rely on the UI to do it for us (it's gone)
            next(EndingStepState(experience, flatStepIndex, action.markComplete, awaitDismissEffect), ContinuationEffect(action))
        } else {
            // otherwise, its a natural end of experience from an in-experience action / dismiss
            // and should be communicated to the UI layer to dismiss itself.
            //
            // instead of using sideEffect we pass EndExperience on EndingStep
            // then AppcuesViewModel will continue to EndExperience when appropriate
            next(EndingStepState(experience, flatStepIndex, action.markComplete, awaitDismissEffect), awaitDismissEffect)
        }
    }
}
