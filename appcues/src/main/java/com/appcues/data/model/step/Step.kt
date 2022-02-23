package com.appcues.data.model.step

import com.appcues.data.model.ExperiencePrimitive
import com.appcues.data.model.action.Action
import com.appcues.data.model.trait.Trait
import java.util.UUID

internal data class Step(
    val id: UUID,
    val content: ExperiencePrimitive,
    val traits: List<Trait>,
    val actions: HashMap<UUID, List<Action>>
)
