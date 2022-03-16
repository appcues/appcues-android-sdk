package com.appcues.statemachine

import com.appcues.data.model.Experience
import java.util.UUID

internal abstract class Error {
    val id: UUID = UUID.randomUUID()
    abstract val message: String

    data class ExperienceError(val experience: Experience, override val message: String) : Error()
    data class StepError(val experience: Experience, val stepIndex: Int, override val message: String) : Error()
}
