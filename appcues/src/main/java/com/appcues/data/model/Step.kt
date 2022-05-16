package com.appcues.data.model

import com.appcues.trait.StepDecoratingTrait
import java.util.UUID

internal data class Step(
    val id: UUID,
    val content: ExperiencePrimitive,
    val traits: List<StepDecoratingTrait>,
    val actions: Map<UUID, List<Action>>,
    val type: String?,
)
