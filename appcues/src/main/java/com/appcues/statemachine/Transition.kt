package com.appcues.statemachine

import com.appcues.statemachine.SideEffect.ReportErrorEffect
import com.appcues.statemachine.State.Idling

internal open class Transition(
    val state: State?,
    val sideEffect: SideEffect? = null,
    val emitStateChange: Boolean = true,
) {
    class ErrorLoggingTransition(error: Error, fatal: Boolean) : Transition(if (fatal) Idling else null, ReportErrorEffect(error))
    class EmptyTransition : Transition(null, null)
}
