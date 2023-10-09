package com.appcues.statemachine

import com.appcues.model.Experience
import com.appcues.model.RenderContext
import java.util.UUID

internal sealed class Error(open val message: String, errorId: UUID? = null) {

    val id: UUID = errorId ?: UUID.randomUUID()

    object ExperienceAlreadyActive : Error("Experience already active")

    data class RenderContextNotActive(val renderContext: RenderContext) : Error("RenderContext $renderContext is not active.")

    data class ExperienceError(
        val experience: Experience,
        override val message: String,
        val errorId: UUID? = null
    ) : Error(message, errorId)

    data class StepError(val experience: Experience, val stepIndex: Int, override val message: String) : Error(message)
}
