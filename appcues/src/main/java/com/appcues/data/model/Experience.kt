package com.appcues.data.model

import java.util.UUID

internal data class Experience(
    val id: UUID,
    val name: String,
    val stepContainers: List<StepContainer>
) {
    // will run once when creating the experience
    val flatSteps: List<Step> = stepContainers.flatMap { it.steps }
}
