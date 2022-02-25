package com.appcues.statemachine.states

import com.appcues.data.model.Experience
import com.appcues.statemachine.Action
import com.appcues.statemachine.Action.StartStep
import com.appcues.statemachine.State
import com.appcues.statemachine.State.Transition
import com.appcues.trait.appcues.AppcuesModalTrait
import org.koin.core.component.get

internal class BeginningExperience(
    override val scopeId: String,
    override val experience: Experience
) : State {

    override fun handleAction(action: Action): Transition? {
        return when (action) {
            is StartStep -> {
                // trait packaging?...

                // render it...
                // eventually: will be rendering a step or set of steps ("step container") - not entire experience
                // we will get this from the experience
                AppcuesModalTrait(null, get(), get(), scopeId).presentExperience()

                // transition to begin step
                Transition(BeginningStep(scopeId, experience, action.step))
            }
            else -> null
        }
    }
}
