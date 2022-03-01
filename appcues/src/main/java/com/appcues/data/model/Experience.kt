package com.appcues.data.model

import java.util.UUID

internal data class Experience(
    val id: UUID,
    val name: String,
    val stepContainer: List<StepContainer>
)
