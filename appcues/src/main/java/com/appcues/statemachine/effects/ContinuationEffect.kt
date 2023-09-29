package com.appcues.statemachine.effects

import com.appcues.action.ActionProcessor
import com.appcues.statemachine.Action
import com.appcues.statemachine.SideEffect

internal class ContinuationEffect(private val action: Action) : SideEffect {

    override suspend fun launch(processor: ActionProcessor): Action = action
}
