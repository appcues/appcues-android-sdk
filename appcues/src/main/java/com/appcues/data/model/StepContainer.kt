package com.appcues.data.model

import com.appcues.trait.ContentHolderTrait
import com.appcues.trait.ContentWrappingTrait
import java.util.UUID

internal data class StepContainer(
    val id: UUID,
    val steps: List<Step>,
    val actions: Map<UUID, List<Action>>,
    val contentHolderTrait: ContentHolderTrait,
    val contentWrappingTrait: ContentWrappingTrait,
)
