@file:Suppress("unused", "UNUSED_PARAMETER")

package com.appcues.statemachine

import com.appcues.data.model.Experience
import com.appcues.statemachine.Action.EndExperience
import com.appcues.statemachine.Action.RenderStep
import com.appcues.statemachine.Action.Reset
import com.appcues.statemachine.Action.StartExperience
import com.appcues.statemachine.Action.StartStep
import com.appcues.statemachine.Error.ExperienceError
import com.appcues.statemachine.Error.StepError
import com.appcues.statemachine.SideEffect.ContinuationEffect
import com.appcues.statemachine.SideEffect.PresentContainerEffect
import com.appcues.statemachine.SideEffect.ReportErrorEffect
import com.appcues.statemachine.State.BeginningExperience
import com.appcues.statemachine.State.BeginningStep
import com.appcues.statemachine.State.EndingExperience
import com.appcues.statemachine.State.EndingStep
import com.appcues.statemachine.State.Idling
import com.appcues.statemachine.State.RenderingStep
import com.appcues.statemachine.StepReference.StepIndex
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

internal interface Transitions {

    fun Idling.fromIdlingToBeginningExperience(action: StartExperience): Transition {
        return if (action.experience.stepContainers.isNotEmpty()) {
            Transition(BeginningExperience(action.experience), ContinuationEffect(StartStep(StepIndex(0))))
        } else {
            // empty experience error
            Transition(null, ReportErrorEffect(ExperienceError(action.experience, "Experience has 0 steps")))
        }
    }

    fun BeginningExperience.fromBeginningExperienceToBeginningStep(action: StartStep): Transition {
        // not sure yet if we'll eventually do any trait composition here
        return Transition(BeginningStep(experience, 0), PresentContainerEffect(experience, 0))
    }

    fun BeginningStep.fromBeginningStepToRenderingStep(action: RenderStep): Transition {
        return Transition(RenderingStep(experience, flatStepIndex))
    }

    fun RenderingStep.fromRenderingStepToEndingStep(action: StartStep): Transition {
        return action.stepReference.getIndex(experience, flatStepIndex).let { nextStepIndex ->
            if (isValidStepIndex(nextStepIndex, experience)) {
                if (experience.areStepsFromDifferentGroup(flatStepIndex, nextStepIndex)) {
                    // in different groups we want to wait for StartStep action from AppcuesViewModel
                    Transition(
                        state = EndingStep(experience, flatStepIndex, nextStepIndex, true),
                        sideEffect = null,
                    )
                } else {
                    // in same group we can continue to StartStep internally
                    Transition(
                        state = EndingStep(experience, flatStepIndex, nextStepIndex, false),
                        sideEffect = ContinuationEffect(StartStep(action.stepReference)),
                    )
                }
            } else {
                // next step index is out of bounds error
                errorTransition(experience, flatStepIndex, "Step at ${action.stepReference} does not exist")
            }
        }
    }

    fun RenderingStep.fromRenderingStepToEndingStep(action: EndExperience): Transition {
        return Transition(EndingStep(experience, flatStepIndex, null, true), ContinuationEffect(EndExperience))
    }

    fun EndingStep.fromEndingStepToEndingExperience(action: EndExperience): Transition {
        return Transition(EndingExperience(experience, flatStepIndex), ContinuationEffect(Reset))
    }

    fun EndingStep.fromEndingStepToBeginningStep(action: StartStep): Transition {
        // get next step index
        return action.stepReference.getIndex(experience, flatStepIndex).let { nextStepIndex ->
            // check if next step index is valid for this experience
            if (isValidStepIndex(nextStepIndex, experience)) {
                // check if current step and next step are from different step container
                transitionsToBeginningStep(experience, flatStepIndex, nextStepIndex, action.stepReference)
            } else {
                // next step index is out of bounds error
                errorTransition(experience, flatStepIndex, "Step at ${action.stepReference} does not exist")
            }
        }
    }

    fun EndingExperience.fromEndingExperienceToIdling(action: Reset): Transition {
        return Transition(Idling())
    }

    companion object : Transitions
}

@OptIn(ExperimentalContracts::class)
private fun isValidStepIndex(stepIndex: Int?, experience: Experience): Boolean {
    contract {
        returns(true) implies (stepIndex != null)
    }

    return stepIndex != null && stepIndex >= 0 && stepIndex < experience.flatSteps.size
}

private fun transitionsToBeginningStep(
    experience: Experience,
    currentStepIndex: Int,
    nextStepIndex: Int,
    nextStepReference: StepReference
) = if (experience.areStepsFromDifferentGroup(currentStepIndex, nextStepIndex)) {
    // given that steps are from different container, we now get step container index to present
    experience.groupLookup[nextStepIndex]?.let { stepContainerIndex ->
        Transition(BeginningStep(experience, nextStepIndex), PresentContainerEffect(experience, stepContainerIndex))
    } ?: run {
        // this should never happen at this point. but better to safe guard anyways
        errorTransition(experience, currentStepIndex, "StepContainer for nextStepIndex $nextStepIndex not found")
    }
} else {
    // else we just move to rendering step
    Transition(BeginningStep(experience, nextStepIndex), ContinuationEffect(StartStep(nextStepReference)))
}

private fun Experience.areStepsFromDifferentGroup(stepIndexOne: Int, stepIndexTwo: Int): Boolean {
    return groupLookup[stepIndexOne] != groupLookup[stepIndexTwo]
}

private fun errorTransition(experience: Experience, currentStepIndex: Int, message: String): Transition {
    return Transition(
        state = null,
        sideEffect = ReportErrorEffect(StepError(experience, currentStepIndex, message))
    )
}
