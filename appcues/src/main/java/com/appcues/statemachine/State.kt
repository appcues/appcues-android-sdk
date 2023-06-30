package com.appcues.statemachine

import com.appcues.data.model.Experience
import com.appcues.util.ResultOf

internal sealed class State(open val experience: Experience) {

    data class Idling(override val experience: Experience) : State(experience)
    data class BeginningExperience(override val experience: Experience) : State(experience)
    data class BeginningStep(
        override val experience: Experience,
        val flatStepIndex: Int,
        val isFirst: Boolean,
        // this is how the UI communicates success/failure in presentation
        // back to the state machine
        val presentationComplete: ((ResultOf<Unit, Error>) -> Unit),
    ) : State(experience)

    data class RenderingStep(override val experience: Experience, val flatStepIndex: Int, val isFirst: Boolean) : State(experience)
    data class EndingStep(
        override val experience: Experience,
        val flatStepIndex: Int,
        val markComplete: Boolean,
        // this works as a ContinuationSideEffect that AppcuesViewModel will
        // send to the state machine once it's done dismissing the current container
        // the presence of a non-null value is what tells the UI to dismiss the current container,
        // and it should be set to null if a dismiss is not requested (i.e. moving to next step in same container)
        val dismissAndContinue: (() -> Unit)?,
    ) : State(experience)

    data class EndingExperience(override val experience: Experience, val flatStepIndex: Int, val markComplete: Boolean) : State(experience)

    val currentStepIndex: Int?
        get() = when (this) {
            is Idling -> null
            is BeginningExperience -> null
            is BeginningStep -> this.flatStepIndex
            is EndingExperience -> this.flatStepIndex
            is EndingStep -> this.flatStepIndex
            is RenderingStep -> this.flatStepIndex
        }
}
