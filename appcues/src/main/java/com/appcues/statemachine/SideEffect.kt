package com.appcues.statemachine

import com.appcues.data.model.Experience

internal sealed class SideEffect {
    data class Continuation(val action: Action) : SideEffect()
    data class PresentContainer(val experience: Experience, val step: Int) : SideEffect()
    data class ReportError(val error: Error) : SideEffect()

    suspend fun execute(machine: StateMachine) =
        when (this) {
            is Continuation -> machine.handleAction(action)
            is ReportError -> machine.handleError(error)
            is PresentContainer -> {
                experience.stepContainer[step].presentingTrait.presentExperience()
            }
        }
}
