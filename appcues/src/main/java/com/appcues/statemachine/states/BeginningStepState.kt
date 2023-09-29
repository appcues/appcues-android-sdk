package com.appcues.statemachine.states

import com.appcues.data.model.Experience
import com.appcues.statemachine.Action
import com.appcues.statemachine.Action.RenderStep
import com.appcues.statemachine.State
import com.appcues.statemachine.Transition

internal data class BeginningStepState(
    val experience: Experience,
    val flatStepIndex: Int,
    val isFirst: Boolean,
) : State {

    override val currentExperience: Experience
        get() = experience
    override val currentStepIndex: Int
        get() = flatStepIndex

    override fun take(action: Action): Transition? {
        return if (action is RenderStep) {
            toRenderingStep(action)
        } else null
    }

    private fun toRenderingStep(action: RenderStep): Transition {
        return next(RenderingStepState(experience, flatStepIndex, isFirst, action.metadata))
    }
}
