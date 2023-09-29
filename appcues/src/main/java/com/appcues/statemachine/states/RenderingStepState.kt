package com.appcues.statemachine.states

import com.appcues.data.model.Experience
import com.appcues.data.model.StepReference.StepOffset
import com.appcues.statemachine.Action
import com.appcues.statemachine.Action.EndExperience
import com.appcues.statemachine.Action.StartStep
import com.appcues.statemachine.Error.StepError
import com.appcues.statemachine.State
import com.appcues.statemachine.Transition
import com.appcues.statemachine.effects.AwaitEffect
import com.appcues.statemachine.effects.ContinuationEffect

internal data class RenderingStepState(
    val experience: Experience,
    val flatStepIndex: Int,
    val isFirst: Boolean,
    val metadata: Map<String, Any?>
) : State {

    override val currentExperience: Experience
        get() = experience
    override val currentStepIndex: Int
        get() = flatStepIndex

    override fun take(action: Action): Transition? {
        return when (action) {
            is StartStep -> if (shouldEndExperience(action)) {
                take(EndExperience(markComplete = true, destroyed = false))
            } else {
                toEndingStep(action)
            }
            is EndExperience -> {
                toEndingExperience(action)
            }
            else -> null
        }
    }

    private fun shouldEndExperience(action: StartStep): Boolean {
        val isLastStep = flatStepIndex == experience.flatSteps.size - 1
        val isContinuingToNextStep = action.stepReference is StepOffset && action.stepReference.offset == 1

        return isLastStep && isContinuingToNextStep
    }

    private fun toEndingStep(action: StartStep): Transition {
        return action.stepReference.getIndex(experience, flatStepIndex).let { nextStepIndex ->
            if (experience.isValidStepIndex(nextStepIndex)) {
                val markComplete = nextStepIndex > flatStepIndex
                if (experience.areStepsFromDifferentGroup(flatStepIndex, nextStepIndex)) {
                    // in different groups we want to wait for StartStep action from AppcuesViewModel
                    val awaitEffect = AwaitEffect(action)

                    next(EndingStepState(experience, flatStepIndex, markComplete, awaitEffect::complete), awaitEffect)
                } else {
                    // in same group we can continue to StartStep internally
                    next(EndingStepState(experience, flatStepIndex, markComplete, null), ContinuationEffect(action))
                }
            } else {
                // next step index is out of bounds error
                keep(StepError(experience, flatStepIndex, "Step at ${action.stepReference} does not exist"))
            }
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

            next(EndingStepState(experience, flatStepIndex, action.markComplete, awaitEffect::complete), awaitEffect)
        }
    }
}
