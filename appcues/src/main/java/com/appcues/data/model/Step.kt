package com.appcues.data.model

import com.appcues.trait.BackdropDecoratingTrait
import com.appcues.trait.ContainerDecoratingTrait
import com.appcues.trait.MetadataSettingTrait
import com.appcues.trait.StepDecoratingTrait
import java.util.UUID

internal data class Step(
    val id: UUID,
    val content: ExperiencePrimitive,
    val stepDecoratingTraits: List<StepDecoratingTrait>,
    val backdropDecoratingTraits: List<BackdropDecoratingTrait>,
    val containerDecoratingTraits: List<ContainerDecoratingTrait>,
    val metadataSettingTraits: List<MetadataSettingTrait>,
    val actions: Map<UUID, List<Action>>,
    val type: String?,
    val formState: ExperienceStepFormState = ExperienceStepFormState(),
    val topStickyContent: ExperiencePrimitive? = null,
    val bottomStickyContent: ExperiencePrimitive? = null,
)
