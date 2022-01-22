package com.appcues.domain.entity.step

import com.appcues.domain.entity.ExperienceComponent
import com.appcues.domain.entity.action.Action
import com.appcues.domain.entity.trait.Trait
import java.util.UUID

internal data class Step(
    val id: UUID,
    val content: ExperienceComponent,
    val traits: List<Trait>,
    val actions: HashMap<String, List<Action>>
)
