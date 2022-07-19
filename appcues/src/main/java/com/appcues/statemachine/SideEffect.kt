package com.appcues.statemachine

import com.appcues.action.ExperienceAction
import com.appcues.data.model.Experience
import com.appcues.util.ResultOf
import kotlinx.coroutines.CompletableDeferred

internal sealed class SideEffect {
    data class ContinuationEffect(val action: Action) : SideEffect()
    data class PresentContainerEffect(
        val experience: Experience,
        val containerIndex: Int,
        val completion: CompletableDeferred<ResultOf<State, Error>>,
    ) : SideEffect()

    data class ReportErrorEffect(val error: Error) : SideEffect()
    data class AwaitEffect(val completion: CompletableDeferred<ResultOf<State, Error>>) : SideEffect()
    data class ProcessActions(val actions: List<ExperienceAction>) : SideEffect()
}
