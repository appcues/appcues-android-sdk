package com.appcues.statemachine

import com.appcues.action.ActionProcessor
import com.appcues.action.ExperienceAction
import kotlinx.coroutines.CompletableDeferred

internal sealed class SideEffect {
    data class ContinuationEffect(val action: Action) : SideEffect()
    data class PresentContainerEffect(val presentContainer: suspend (ActionProcessor) -> Action) : SideEffect()
    data class ReportErrorEffect(val error: Error) : SideEffect()
    data class AwaitEffect(val effect: SideEffect) : SideEffect() {

        private val task = CompletableDeferred<Unit>()

        suspend fun await() = task.await()
        fun complete() = task.complete(Unit)
    }

    data class ProcessActions(val actions: List<ExperienceAction>) : SideEffect()
}
