package com.appcues.statemachine

import com.appcues.data.model.Experience
import java.util.UUID

internal sealed class Error(open val message: String) {
    private val _id = UUID.randomUUID()

    // allows for override of error ID with a value driven by the experience.renderErrorId
    // for matching error/recovery IDs on render retry
    var errorId: UUID? = null

    val id: UUID
        get() = errorId ?: _id

    object ExperienceAlreadyActive : Error("Experience already active")

    data class ExperienceError(val experience: Experience, override val message: String) : Error(message)
    data class StepError(
        val experience: Experience,
        val stepIndex: Int,
        override val message: String,
        val recoverable: Boolean = false,
    ) : Error(message)
}
