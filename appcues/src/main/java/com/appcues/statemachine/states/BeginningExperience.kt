package com.appcues.statemachine.states

import android.content.Context
import com.appcues.data.model.Experience
import com.appcues.statemachine.Action
import com.appcues.statemachine.Action.StartStep
import com.appcues.statemachine.State
import com.appcues.ui.AppcuesActivity
import org.koin.core.component.inject

internal class BeginningExperience(
    override val scopeId: String,
    override val experience: Experience
) : State {
    private val context by inject<Context>()

    override fun handleAction(action: Action): State {
        return when (action) {
            is StartStep -> {
                // trait packaging?...

                // render it...
                // eventually: will be rendering a step or set of steps ("step container") - not entire experience
                // idea: don't need to pass the experience through directly here, could let activity reference back
                //    to state machine / state somehow to get the experience and/or steps to render
                //    experience then does not need to be Parcelable
                context.startActivity(AppcuesActivity.getIntent(context, scopeId))

                // transition to begin step
                BeginningStep(scopeId, experience, action.step)
            }
            else -> this
        }
    }
}
