package com.appcues.statemachine.effects

import com.appcues.action.ActionProcessor
import com.appcues.statemachine.Action
import com.appcues.statemachine.SideEffect
import kotlinx.coroutines.CompletableDeferred
import java.util.Objects

internal class AwaitEffect(
    private val action: Action,
    private val task: CompletableDeferred<Unit> = CompletableDeferred()
) : SideEffect {

    fun complete() = task.complete(Unit)

    override suspend fun launch(processor: ActionProcessor): Action {
        task.await()

        return action
    }

    // custom equals and hash to ignore task so its easier to compare while testing
    override fun equals(other: Any?): Boolean {
        // compare reference
        if (this === other) return true

        return other is AwaitEffect &&
            action == other.action
    }

    override fun hashCode(): Int {
        return Objects.hash(action)
    }
}
