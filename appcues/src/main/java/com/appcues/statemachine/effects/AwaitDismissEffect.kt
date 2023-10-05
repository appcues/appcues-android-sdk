package com.appcues.statemachine.effects

import com.appcues.action.ActionProcessor
import com.appcues.statemachine.Action
import com.appcues.statemachine.SideEffect
import kotlinx.coroutines.CompletableDeferred
import org.jetbrains.annotations.VisibleForTesting

internal data class AwaitDismissEffect(private val action: Action) : SideEffect {

    @VisibleForTesting
    var task: CompletableDeferred<Unit> = CompletableDeferred()

    fun dismissed() = task.complete(Unit)

    override suspend fun launch(processor: ActionProcessor): Action {
        task.await()

        return action
    }
}
