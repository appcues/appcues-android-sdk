package com.appcues.statemachine

internal sealed class StateResult {
    data class Success(val state: State) : StateResult()
    data class Failure(val error: Error) : StateResult()
}
