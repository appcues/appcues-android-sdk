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
        // next step index is needed here so AppcuesViewModel knows the next step
        // when sending StartStep action to state machine
        val flatNextStepIndex: Int?,
        val dismiss: Boolean
    ) : State(experience)

    data class EndingExperience(override val experience: Experience, val flatStepIndex: Int) : State(experience)
}
