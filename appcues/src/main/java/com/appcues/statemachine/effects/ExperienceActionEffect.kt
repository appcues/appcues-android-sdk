package com.appcues.statemachine.effects

import com.appcues.action.ActionProcessor
import com.appcues.action.ExperienceAction
import com.appcues.statemachine.Action
import com.appcues.statemachine.SideEffect

internal data class ExperienceActionEffect(private val actions: List<ExperienceAction>) : SideEffect {

    override suspend fun launch(processor: ActionProcessor): Action? {
        // invoking processPostFlowActions ensures the current transition will complete
        // regardless of the actions being processed here.
        // This is necessary to avoid a possible deadlock between the actions and the state machine.
        processor.processPostFlowActions(actions)

        return null
    }
}
