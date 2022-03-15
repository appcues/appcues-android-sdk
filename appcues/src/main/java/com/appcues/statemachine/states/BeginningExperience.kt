package com.appcues.statemachine.states

import com.appcues.data.model.Experience
import com.appcues.statemachine.Action
import com.appcues.statemachine.Action.StartExperience
import com.appcues.statemachine.Action.StartStep
import com.appcues.statemachine.State
import com.appcues.statemachine.Transition

internal class BeginningExperience(
    override val experience: Experience
) : State {

    override fun handleAction(action: Action): Transition? {
        return when (action) {
            is StartExperience -> Transition.ExperienceActiveError(experience)
            is StartStep -> {
                // trait packaging?...

                // render it...
                // eventually: will be rendering a step or set of steps ("step container") - not entire experience
                // we will get this from the experience

                // figure out if its current container or next
                experience.stepContainer[action.step].presentingTrait.presentExperience()
                // how do we control the current step container?
                // this should run only ONCE per stepContainer, is this the right place?
                // maybe we need a StartStepContainer, EndStepContainer to wrap all step lifecycle

                // transition to begin step
                Transition(BeginningStep(experience, action.step))
            }
            else -> null
        }
    }
}
