package com.appcues.data.model

import java.util.UUID

internal data class Experiment(
    val id: UUID,
    val group: String,
    val experienceId: UUID,
    val goalId: String,
    val contentType: String,
)
