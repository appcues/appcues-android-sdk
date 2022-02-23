package com.appcues.data.model

import java.util.UUID

internal data class Step(
    val id: UUID,
    val content: ExperiencePrimitive,
    val traits: List<Trait>,
)
