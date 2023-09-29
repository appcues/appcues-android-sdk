package com.appcues.statemachine

internal class Transition(
    val newState: State,
    val error: Error?,
    val sideEffect: SideEffect?
)
