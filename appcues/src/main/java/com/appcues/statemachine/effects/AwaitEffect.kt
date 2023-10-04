package com.appcues.statemachine.effects

import com.appcues.action.ActionProcessor
import com.appcues.statemachine.Action
import com.appcues.statemachine.SideEffect
import kotlinx.coroutines.CompletableDeferred

internal class AwaitEffect(
    private val action: Action,
    private val task: CompletableDeferred<Unit> = CompletableDeferred()
) : SideEffect {

    fun complete() = task.complete(Unit)

    override suspend fun launch(processor: ActionProcessor): Action {
        task.await()

        return action
    }
}
