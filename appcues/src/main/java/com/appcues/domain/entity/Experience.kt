package com.appcues.domain.entity

import com.appcues.domain.entity.step.Step
import com.appcues.domain.entity.trait.Trait
import java.util.UUID

internal data class Experience(
    val id: UUID,
    val name: String,
    val traits: List<Trait>,
    val steps: List<Step>
)
