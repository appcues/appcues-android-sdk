package com.appcues.data.model

import com.appcues.trait.BackdropDecoratingTrait
import com.appcues.trait.ContainerDecoratingTrait
import com.appcues.trait.ContentHolderTrait
import com.appcues.trait.ContentWrappingTrait
import com.appcues.trait.ExperiencePresentingTrait

internal data class StepContainer(
    val steps: List<Step>,
    val presentingTrait: ExperiencePresentingTrait,
    val contentHolderTrait: ContentHolderTrait,
    val contentWrappingTrait: ContentWrappingTrait,
    val backdropTraits: List<BackdropDecoratingTrait>,
    val containerTraits: List<ContainerDecoratingTrait>,
)
