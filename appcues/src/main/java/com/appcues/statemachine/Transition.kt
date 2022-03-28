package com.appcues.statemachine

import com.appcues.statemachine.SideEffect.ContinuationEffect
import com.appcues.statemachine.SideEffect.PresentContainerEffect
import com.appcues.statemachine.SideEffect.ReportErrorEffect

internal open class Transition(
    private val state: State?,
    private val sideEffect: SideEffect? = null
) {

    suspend fun applyTransition(stateMachine: StateMachine, stateBlock: suspend (State) -> Unit) {
        if (state != null) {
            stateBlock(state)
        }

        if (sideEffect != null) {
            when (sideEffect) {
                is ContinuationEffect -> sideEffect.applyEffect(stateMachine)
                is ReportErrorEffect -> sideEffect.applyEffect(stateMachine)
                is PresentContainerEffect -> sideEffect.applyEffect()
            }
        }
    }

    private fun ContinuationEffect.applyEffect(stateMachine: StateMachine) {
        stateMachine.handleAction(action)
    }

    private fun ReportErrorEffect.applyEffect(stateMachine: StateMachine) {
        stateMachine.reportError(error)
    }

    private fun PresentContainerEffect.applyEffect() {
        experience.stepContainers[containerIndex].presentingTrait.present()
    }

    class ErrorLoggingTransition(error: Error) : Transition(null, ReportErrorEffect(error))

    class EmptyTransition : Transition(null, null)
}
