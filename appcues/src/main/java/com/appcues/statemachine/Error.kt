package com.appcues.statemachine

import com.appcues.data.model.Experience
import com.appcues.data.model.RenderContext
import java.util.UUID

internal sealed class Error(open val message: String) {

    val id: UUID = UUID.randomUUID()

    data class ExperienceAlreadyActive(val ignoredExperience: Experience, override val message: String) : Error(message)
    data class ExperienceError(val experience: Experience, override val message: String) : Error(message)
    data class StepError(val experience: Experience, val stepIndex: Int, override val message: String) : Error(message)

    data class NoActiveStateMachine(val renderContext: RenderContext) :
        Error("No active state machine found that matches renderContext $renderContext")
}
