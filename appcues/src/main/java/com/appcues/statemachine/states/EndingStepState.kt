package com.appcues.statemachine.states

import com.appcues.data.model.Experience
import com.appcues.statemachine.Action
import com.appcues.statemachine.Action.EndExperience
import com.appcues.statemachine.Action.Reset
import com.appcues.statemachine.Action.StartStep
import com.appcues.statemachine.Error.StepError
import com.appcues.statemachine.State
import com.appcues.statemachine.Transition
import com.appcues.statemachine.effects.ContinuationEffect
import com.appcues.statemachine.effects.PresentationEffect

// onUiDismissed works as a ContinuationSideEffect that AppcuesViewModel will
// send to the state machine once it's done dismissing the current container
// the presence of a non-null value is what tells the UI to dismiss the current container,
// and it should be set to null if a dismiss is not requested (i.e. moving to next step in same container)
internal data class EndingStepState(
    val experience: Experience,
    val flatStepIndex: Int,
    val markComplete: Boolean,
    val onUiDismissed: (() -> Unit)?,
) : State {

    override val currentExperience: Experience
        get() = experience
    override val currentStepIndex: Int
        get() = flatStepIndex

    override fun take(action: Action): Transition? {
        return when (action) {
            is EndExperience -> toEndingExperience(action)
            is StartStep -> toBeginningStep(action)
            else -> null
        }
    }

    private fun toEndingExperience(action: EndExperience): Transition {
        return next(
            state = EndingExperienceState(experience, flatStepIndex, action.markComplete, action.trackAnalytics),
            sideEffect = ContinuationEffect(Reset)
        )
    }

    private fun toBeginningStep(action: StartStep): Transition {
        // get next step index
        return action.stepReference.getIndex(experience, flatStepIndex).let { nextStepIndex ->
            // check if next step index is valid for this experience
            if (experience.isValidStepIndex(nextStepIndex)) {
                // check if current step and next step are from different step container
                // find step container index or return step error
                val stepContainerIndex = experience.groupLookup[nextStepIndex]
                    ?: return keep(StepError(experience, flatStepIndex, "StepContainer for nextStepIndex $nextStepIndex not found"))

                val shouldPresent = experience.areStepsFromDifferentGroup(flatStepIndex, nextStepIndex)

                return next(
                    state = BeginningStepState(experience, nextStepIndex, false),
                    sideEffect = PresentationEffect(experience, nextStepIndex, stepContainerIndex, shouldPresent)
                )
            } else {
                // next step index is out of bounds error
                keep(StepError(experience, flatStepIndex, "Step at ${action.stepReference} does not exist"))
            }
        }
    }
}
