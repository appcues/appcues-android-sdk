package com.appcues.statemachine

import com.appcues.action.ActionProcessor

internal interface SideEffect {

    suspend fun launch(processor: ActionProcessor): Action?
}
