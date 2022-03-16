package com.appcues.statemachine

import com.appcues.data.model.Experience
import com.appcues.statemachine.Action.EndExperience
import com.appcues.statemachine.Action.RenderStep
import com.appcues.statemachine.Action.Reset
import com.appcues.statemachine.Action.StartExperience
import com.appcues.statemachine.Action.StartStep
import com.appcues.statemachine.Error.ExperienceError
import com.appcues.statemachine.SideEffect.Continuation
import com.appcues.statemachine.SideEffect.PresentContainer
import com.appcues.statemachine.SideEffect.ReportError
import com.appcues.statemachine.State.BeginningExperience
import com.appcues.statemachine.State.BeginningStep
import com.appcues.statemachine.State.EndingStep

internal abstract class State {
    abstract val experience: Experience?

    class BeginningExperience(override val experience: Experience) : State()
    class BeginningStep(override val experience: Experience, val step: Int) : State()
    class EndingExperience(override val experience: Experience, val step: Int) : State()
    class EndingStep(override val experience: Experience, val step: Int, val dismiss: Boolean) : State()
    class Idling(override val experience: Experience? = null) : State()
    class RenderingStep(override val experience: Experience, val step: Int) : State()

    fun transition(action: Action): Transition? =
        when {
            this is Idling && action is StartExperience ->
                Transition.fromIdlingToBeginningExperience(action.experience)
            this is BeginningExperience && action is StartStep ->
                Transition.fromBeginningExperienceToBeginningStep(experience)
            this is BeginningStep && action is RenderStep ->
                Transition(RenderingStep(experience, step))
            this is RenderingStep && action is StartStep ->
                Transition.fromRenderingStepToEndingStep(experience, step, action.step)
            this is RenderingStep && action is EndExperience ->
                Transition(EndingStep(experience, step, true), Continuation(EndExperience()))
            this is EndingStep && action is EndExperience ->
                Transition(EndingExperience(experience, step), Continuation(Reset()))
            this is EndingStep && action is StartStep ->
                Transition.fromEndingStepToBeginningStep(experience, step)
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

internal fun Transition.Companion.fromIdlingToBeginningExperience(experience: Experience): Transition {
    if (experience.stepContainer.isEmpty()) {
        return Transition(null, ReportError(ExperienceError(experience, "Experience has 0 steps")))
    }
    return Transition(BeginningExperience(experience), Continuation(StartStep(0)))
}

internal fun Transition.Companion.fromBeginningExperienceToBeginningStep(experience: Experience): Transition {
    // not sure yet if we'll eventually do any trait composition here
    return Transition(BeginningStep(experience, 0), PresentContainer(experience, 0))
}

internal fun Transition.Companion.fromRenderingStepToEndingStep(experience: Experience, currentStep: Int, nextStep: Int): Transition {
    // this is where we'll eventually need to see if we are moving to another step in the same
    // group/container OR moving to a new step group that needs a dismiss -> present.
    // for now, just treating as moving to a new group/container.
    return Transition(EndingStep(experience, currentStep, true), Continuation(StartStep(nextStep)))
}

internal fun Transition.Companion.fromEndingStepToBeginningStep(experience: Experience, nextStep: Int): Transition {
    // here is where we need logic to resolve the step to move to from a "step reference" rather than just
    // a hardcoded next step (a continue action for example) -- and error if not a valid step
    return Transition(BeginningStep(experience, nextStep), PresentContainer(experience, nextStep))
}
