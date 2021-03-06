package com.appcues.statemachine

import com.appcues.statemachine.SideEffect.ReportErrorEffect

internal open class Transition(
    val state: State?,
    val sideEffect: SideEffect? = null
) {
    class ErrorLoggingTransition(error: Error) : Transition(null, ReportErrorEffect(error))
    class EmptyTransition : Transition(null, null)
}
