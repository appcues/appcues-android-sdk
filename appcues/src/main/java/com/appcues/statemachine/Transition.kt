package com.appcues.statemachine

internal open class Transition(val state: State?, val sideEffect: SideEffect? = null) {
    // companion object is here to allow for "static" extension functions (see State.kt)
    companion object
}
