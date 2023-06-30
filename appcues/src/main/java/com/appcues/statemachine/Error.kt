package com.appcues.statemachine

import com.appcues.data.model.Experience
import java.util.UUID

internal sealed class Error(open val experience: Experience, open val message: String) {

    val id: UUID = UUID.randomUUID()

    data class ExperienceAlreadyActive(override val experience: Experience, override val message: String) : Error(experience, message)
    data class ExperienceError(override val experience: Experience, override val message: String) : Error(experience, message)
    data class StepError(override val experience: Experience, val stepIndex: Int, override val message: String) : Error(experience, message)
}
