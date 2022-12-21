package com.appcues.data.model

import com.appcues.trait.ContentHolderTrait
import com.appcues.trait.ContentWrappingTrait
import com.appcues.trait.PresentingTrait
import com.appcues.trait.appcues.LocalizationTrait
import java.util.UUID

internal data class StepContainer(
    val id: UUID,
    val steps: List<Step>,
    val actions: Map<UUID, List<Action>>,
    val presentingTrait: PresentingTrait,
    val contentHolderTrait: ContentHolderTrait,
    val contentWrappingTrait: ContentWrappingTrait,
    val localizationTrait: LocalizationTrait? = null,
)
