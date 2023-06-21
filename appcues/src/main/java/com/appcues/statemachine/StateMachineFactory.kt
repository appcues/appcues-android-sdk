package com.appcues.statemachine

import com.appcues.AppcuesCoroutineScope
import com.appcues.action.ActionProcessor
import com.appcues.data.model.RenderContext
import com.appcues.statemachine.StateMachine.StateMachineListener

internal class StateMachineFactory(
    private val appcuesCoroutineScope: AppcuesCoroutineScope,
    private val actionProcessor: ActionProcessor,
) {

    fun create(renderContext: RenderContext, listener: StateMachineListener): StateMachine {
        return StateMachine(renderContext, appcuesCoroutineScope, actionProcessor).also { it.listener = listener }
    }
}
