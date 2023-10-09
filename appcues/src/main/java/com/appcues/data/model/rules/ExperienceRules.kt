package com.appcues.data.model.rules

import java.util.Date
import java.util.UUID

internal data class ExperienceRules(
    val experienceId: UUID,
    val userId: String,
    val seenAt: Date,
)
