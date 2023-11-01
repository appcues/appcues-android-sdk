package com.appcues.statemachine

internal data class Transition(
    val newState: State,
    val error: Error?,
    val sideEffect: SideEffect?
)
