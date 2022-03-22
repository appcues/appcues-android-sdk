package com.appcues.statemachine

import com.appcues.data.model.Experience
import com.appcues.data.model.areStepsFromDifferentContainers
import com.appcues.data.model.getStepContainerIndex
import com.appcues.statemachine.Action.EndExperience
import com.appcues.statemachine.Action.RenderStep
import com.appcues.statemachine.Action.Reset
import com.appcues.statemachine.Action.StartExperience
import com.appcues.statemachine.Action.StartStep
import com.appcues.statemachine.Error.ExperienceError
import com.appcues.statemachine.Error.StepError
import com.appcues.statemachine.SideEffect.Continuation
import com.appcues.statemachine.SideEffect.PresentContainer
import com.appcues.statemachine.SideEffect.ReportError
import com.appcues.statemachine.State.BeginningExperience
import com.appcues.statemachine.State.BeginningStep
import com.appcues.statemachine.State.EndingStep
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

internal sealed class State(open val experience: Experience?) {

    data class Idling(override val experience: Experience? = null) : State(experience)
    data class BeginningExperience(override val experience: Experience) : State(experience)
    data class BeginningStep(override val experience: Experience, val step: Int) : State(experience)
    data class RenderingStep(override val experience: Experience, val step: Int) : State(experience)
    data class EndingStep(override val experience: Experience, val step: Int, val dismiss: Boolean) : State(experience)
    data class EndingExperience(override val experience: Experience, val step: Int) : State(experience)

    fun transition(action: Action): Transition? = when {
        this is Idling && action is StartExperience ->
            Transition.fromIdlingToBeginningExperience(action.experience)
        this is BeginningExperience && action is StartStep ->
            Transition.fromBeginningExperienceToBeginningStep(experience)
        this is BeginningStep && action is RenderStep ->
            Transition(RenderingStep(experience, step))
        this is RenderingStep && action is StartStep ->
            Transition.fromRenderingStepToEndingStep(experience, step, action.stepReference)
        this is RenderingStep && action is EndExperience ->
            Transition(EndingStep(experience, step, true), Continuation(EndExperience))
        this is EndingStep && action is EndExperience ->
            Transition(EndingExperience(experience, step), Continuation(Reset))
        this is EndingStep && action is StartStep ->
            Transition.fromEndingStepToBeginningStep(experience, step, action.stepReference)
        this is EndingExperience && action is Reset ->
            Transition(Idling())

        // error cases
        action is StartExperience ->
            Transition(null, ReportError(ExperienceError(action.experience, "Experience already active")))
        action is Action.ReportError ->
            Transition(null, ReportError(action.error))

        // undefined transition - no-op
        else -> null
    }
}

private fun Transition.Companion.fromIdlingToBeginningExperience(experience: Experience): Transition {
    return if (experience.stepContainers.isNotEmpty()) {
        Transition(BeginningExperience(experience), Continuation(StartStep(StepReference.StepIndex(0))))
    } else {
        // empty experience error
        Transition(null, ReportError(ExperienceError(experience, "Experience has 0 steps")))
    }
}

private fun Transition.Companion.fromBeginningExperienceToBeginningStep(experience: Experience): Transition {
    // not sure yet if we'll eventually do any trait composition here
    return Transition(BeginningStep(experience, 0), PresentContainer(experience, 0))
}

private fun Transition.Companion.fromEndingStepToBeginningStep(
    experience: Experience,
    currentStepIndex: Int,
    nextStepReference: StepReference
): Transition {
    // get next step index
    return nextStepReference.getIndex(experience, currentStepIndex).let { nextStepIndex ->
        // check if next step index is valid for this experience
        if (isValidStepIndex(nextStepIndex, experience)) {
            // check if current step and next step are from different step container
            transitionOfEndingToBeginning(experience, currentStepIndex, nextStepIndex, nextStepReference)
        } else {
            // next step index is out of bounds error
            transitionOfError(experience, currentStepIndex, "Step at $nextStepReference does not exist")
        }
    }
}

private fun Transition.Companion.transitionOfEndingToBeginning(
    experience: Experience,
    currentStepIndex: Int,
    nextStepIndex: Int,
    nextStepReference: StepReference
) = if (experience.areStepsFromDifferentContainers(currentStepIndex, nextStepIndex)) {
    // given that steps are from different container, we now get step container index to present
    experience.getStepContainerIndex(nextStepIndex)?.let { stepContainerIndex ->
        Transition(BeginningStep(experience, nextStepIndex), PresentContainer(experience, stepContainerIndex))
    } ?: run {
        // this should never happen at this point. but better to safe guard anyways
        transitionOfError(experience, currentStepIndex, "StepContainer for nextStepIndex $nextStepIndex not found")
    }
} else {
    // else we just move to rendering step
    Transition(BeginningStep(experience, nextStepIndex), Continuation(StartStep(nextStepReference)))
}

private fun Transition.Companion.fromRenderingStepToEndingStep(
    experience: Experience,
    currentStepIndex: Int,
    nextStepReference: StepReference
): Transition {
    return nextStepReference.getIndex(experience, currentStepIndex).let { nextStepIndex ->
        if (isValidStepIndex(nextStepIndex, experience)) {
            Transition(
                state = EndingStep(
                    experience,
                    currentStepIndex,
                    experience.areStepsFromDifferentContainers(currentStepIndex, nextStepIndex)
                ),
                sideEffect = Continuation(StartStep(nextStepReference)),
            )
        } else {
            // next step index is out of bounds error
            transitionOfError(experience, currentStepIndex, "Step at $nextStepReference does not exist")
        }
    }
}

@OptIn(ExperimentalContracts::class)
private fun Transition.Companion.isValidStepIndex(stepIndex: Int?, experience: Experience): Boolean {
    contract {
        returns() implies (stepIndex != null)
    }

    return stepIndex != null && stepIndex >= 0 && stepIndex < experience.flatSteps.size
}

private fun Transition.Companion.transitionOfError(experience: Experience, currentStepIndex: Int, message: String): Transition {
    return Transition(
        state = null,
        sideEffect = ReportError(StepError(experience, currentStepIndex, message))
    )
}
