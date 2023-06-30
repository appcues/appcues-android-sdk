package com.appcues.statemachine

import com.appcues.AppcuesCoroutineScope
import com.appcues.action.ActionProcessor
import com.appcues.data.model.Experience

internal class StateMachineFactory(
    private val appcuesCoroutineScope: AppcuesCoroutineScope,
    private val actionProcessor: ActionProcessor,
) {

    fun create(experience: Experience): StateMachine {
        return StateMachine(appcuesCoroutineScope, actionProcessor, experience)
    }
}
