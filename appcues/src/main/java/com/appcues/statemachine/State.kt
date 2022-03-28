package com.appcues.statemachine

import com.appcues.data.model.Experience

internal sealed class State(open val experience: Experience?) {

    data class Idling(override val experience: Experience? = null) : State(experience)
    data class BeginningExperience(override val experience: Experience) : State(experience)
    data class BeginningStep(override val experience: Experience, val flatStepIndex: Int) : State(experience)
    data class RenderingStep(override val experience: Experience, val flatStepIndex: Int) : State(experience)
    data class EndingStep(
        override val experience: Experience,
        val flatStepIndex: Int,
        // this works as a ContinuationSideEffect that AppcuesViewModel will
        // send to the state machine once it's done dismissing the current container
        val dismissAndContinue: Action?
    ) : State(experience)

    data class EndingExperience(override val experience: Experience, val flatStepIndex: Int) : State(experience)
}
