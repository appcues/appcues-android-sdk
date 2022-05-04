package com.appcues.statemachine

import com.appcues.data.model.Experience

internal sealed class State() {

    object Idling : State()
    data class BeginningExperience(val experience: Experience) : State()
    data class BeginningStep(
        val experience: Experience,
        val flatStepIndex: Int,
        val isFirst: Boolean,
        // this is how the UI communicates success/failure in presentation
        // back to the state machine
        val presentationComplete: (suspend () -> Unit)?,
    ) : State()
    data class RenderingStep(val experience: Experience, val flatStepIndex: Int, val isFirst: Boolean) : State()
    data class EndingStep(
        val experience: Experience,
        val flatStepIndex: Int,
        // this works as a ContinuationSideEffect that AppcuesViewModel will
        // send to the state machine once it's done dismissing the current container
        val dismissAndContinue: (suspend () -> Unit)?
    ) : State()
    data class EndingExperience(val experience: Experience, val flatStepIndex: Int, val markComplete: Boolean) : State()
}
