package com.appcues.statemachine.effects

import com.appcues.action.ActionProcessor
import com.appcues.action.ExperienceAction
import com.appcues.statemachine.Action
import com.appcues.statemachine.SideEffect

internal class ExperienceActionEffect(private val actions: List<ExperienceAction>) : SideEffect {

    override suspend fun launch(processor: ActionProcessor): Action? {
        processor.process(actions)

        return null
    }
}
