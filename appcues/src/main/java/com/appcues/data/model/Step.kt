package com.appcues.data.model

import com.appcues.trait.StepDecoratingTrait
import java.util.HashMap
import java.util.UUID

internal data class Step(
    val id: UUID,
    val content: ExperiencePrimitive,
    val traits: List<StepDecoratingTrait>,
    val actions: HashMap<UUID, List<Action>>
)
