package com.appcues.data.mapper.step

import com.appcues.data.mapper.LeveledTraitResponse
import com.appcues.data.mapper.action.ActionsMapper
import com.appcues.data.mapper.mergeTraits
import com.appcues.data.mapper.step.primitives.mapPrimitive
import com.appcues.data.mapper.trait.TraitsMapper
import com.appcues.data.model.ExperiencePrimitive
import com.appcues.data.model.ExperiencePrimitive.VerticalStackPrimitive
import com.appcues.data.model.RenderContext
import com.appcues.data.model.Step
import com.appcues.data.remote.appcues.response.step.StepResponse
import com.appcues.data.remote.appcues.response.step.primitive.PrimitiveResponse
import com.appcues.data.remote.appcues.response.step.primitive.PrimitiveResponse.BoxPrimitiveResponse
import com.appcues.data.remote.appcues.response.step.primitive.PrimitiveResponse.StackPrimitiveResponse
import com.appcues.trait.BackdropDecoratingTrait
import com.appcues.trait.ContainerDecoratingTrait
import com.appcues.trait.ExperienceTraitLevel.STEP
import com.appcues.trait.MetadataSettingTrait
import com.appcues.trait.PresentingTrait
import com.appcues.trait.StepDecoratingTrait
import java.util.UUID

internal class StepMapper(
    private val traitsMapper: TraitsMapper,
    private val actionsMapper: ActionsMapper,
) {

    fun map(
        renderContext: RenderContext,
        from: StepResponse,
        presentingTrait: PresentingTrait,
        stepContainerTraits: List<LeveledTraitResponse>,
    ): Step {
        val stepTraits = from.traits.map { it to STEP }
        val mergedTraits = stepTraits.mergeTraits(stepContainerTraits)
        val mappedTraits = traitsMapper.map(renderContext, mergedTraits)

        val topStickyItems = mutableListOf<PrimitiveResponse>()
        val bottomStickyItems = mutableListOf<PrimitiveResponse>()
        val responseContent = from.content.extractStickyContent(topStickyItems, bottomStickyItems)

        return Step(
            id = from.id,
            content = responseContent.mapPrimitive(),
            presentingTrait = presentingTrait,
            stepDecoratingTraits = mappedTraits.filterIsInstance<StepDecoratingTrait>(),
            backdropDecoratingTraits = mappedTraits.filterIsInstance<BackdropDecoratingTrait>(),
            containerDecoratingTraits = mappedTraits.filterIsInstance<ContainerDecoratingTrait>(),
            metadataSettingTraits = mappedTraits.filterIsInstance<MetadataSettingTrait>(),
            actions = actionsMapper.map(renderContext, from.actions),
            type = from.type,
            topStickyContent = topStickyItems.toWrappedStickyContent(),
            bottomStickyContent = bottomStickyItems.toWrappedStickyContent(),
        )
    }
}

// if a single sticky item, return as-is
// if multiple sticky items, wrap them in a vertical stack
private fun List<PrimitiveResponse>.toWrappedStickyContent(): ExperiencePrimitive? {
    return when {
        this.count() == 1 -> this.first().mapPrimitive()
        this.count() > 1 -> VerticalStackPrimitive(UUID.randomUUID(), items = this.map { it.mapPrimitive() })
        else -> null
    }
}

// This function recursively walks through the content and extracts any items that were meant to be sticky content - pinned
// to either the top or bottom. This type of sticky content will only exist as h-stacks (rows) inside of v-stacks (content container),
// in practice. These items are then removed from the main content collected into top and bottom collections, which are applied later
// to the composition as overlay content.
private fun PrimitiveResponse.extractStickyContent(
    topSticky: MutableList<PrimitiveResponse>,
    bottomSticky: MutableList<PrimitiveResponse>
): PrimitiveResponse {
    return when (this) {
        is StackPrimitiveResponse -> {
            val mutableItems = items.toMutableList()
            val topStickyItems = mutableItems.filterIsInstance<StackPrimitiveResponse>().filter { it.sticky?.lowercase() == "top" }
            val bottomStickyItems = mutableItems.filterIsInstance<StackPrimitiveResponse>().filter { it.sticky?.lowercase() == "bottom" }
            mutableItems.removeAll(topStickyItems)
            mutableItems.removeAll(bottomStickyItems)
            topSticky.addAll(topStickyItems)
            bottomSticky.addAll(bottomStickyItems)

            copy(
                items = mutableItems.map { it.extractStickyContent(topSticky, bottomSticky) }
            )
        }
        is BoxPrimitiveResponse -> {
            copy(
                items = items.map { it.extractStickyContent(topSticky, bottomSticky) }
            )
        }
        else -> this
    }
}
