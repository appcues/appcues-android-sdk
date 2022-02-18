package com.appcues.statemachine.states

import com.appcues.statemachine.Action
import com.appcues.statemachine.Action.RenderStep
import com.appcues.statemachine.ExperiencePackage
import com.appcues.statemachine.State
import com.appcues.ui.AppcuesActivity

internal class BeginningStep(val experience: ExperiencePackage, val step: Int) : State {
    override fun handleAction(action: Action): State {
        return when (action) {
            is RenderStep -> {

                // render it...
                with(experience) {
                    context.startActivity(AppcuesActivity.getIntent(context, scopeId, experience))
                }

                // transition to rendering, rest.
                RenderingStep(experience, step)
            }
            else -> this
        }
    }
}
