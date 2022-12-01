package com.appcues.data.model

import com.appcues.trait.ContentHolderTrait
import com.appcues.trait.ContentWrappingTrait
import com.appcues.trait.PresentingTrait

internal data class StepContainer(
    val steps: List<Step>,
    val presentingTrait: PresentingTrait,
    val contentHolderTrait: ContentHolderTrait,
    val contentWrappingTrait: ContentWrappingTrait,
)
