package com.appcues.data.mapper.step

import com.appcues.data.mapper.LeveledTraitResponse
import com.appcues.data.mapper.action.ActionsMapper
import com.appcues.data.mapper.mergeTraits
import com.appcues.data.mapper.step.primitives.mapPrimitive
import com.appcues.data.mapper.trait.TraitsMapper
import com.appcues.data.model.ExperiencePrimitive
import com.appcues.data.model.ExperiencePrimitive.VerticalStackPrimitive
import com.appcues.data.model.Step
import com.appcues.data.remote.response.step.StepResponse
import com.appcues.data.remote.response.step.primitive.PrimitiveResponse
import com.appcues.data.remote.response.step.primitive.PrimitiveResponse.BoxPrimitiveResponse
import com.appcues.data.remote.response.step.primitive.PrimitiveResponse.StackPrimitiveResponse
import com.appcues.trait.ExperienceTraitLevel.STEP
import com.appcues.trait.StepDecoratingTrait
import java.util.UUID

internal class StepMapper(
    private val traitsMapper: TraitsMapper,
    private val actionsMapper: ActionsMapper,
) {
    fun map(
        from: StepResponse,
        stepContainerTraits: List<LeveledTraitResponse>,
    ): Step {
        val stepTraits = from.traits.map { it to STEP }
        val mergedTraits = stepTraits.mergeTraits(stepContainerTraits)

        val topStickyItems = mutableListOf<PrimitiveResponse>()
        val bottomStickyItems = mutableListOf<PrimitiveResponse>()
        val responseContent = from.content.extractStickyContent(topStickyItems, bottomStickyItems)

        return Step(
            id = from.id,
            content = responseContent.mapPrimitive(),
            stepDecoratingTraits = traitsMapper.map(mergedTraits).filterIsInstance(StepDecoratingTrait::class.java),
            actions = actionsMapper.map(from.actions),
            type = from.type,
            topStickyContent = topStickyItems.toWrappedStickyContent(),
            bottomStickyContent = bottomStickyItems.toWrappedStickyContent(),
        )
    }
}

// if one or more sticky content items exist, wrap them in a vertical stack that will then be applied as overlay content
private fun List<PrimitiveResponse>.toWrappedStickyContent(): ExperiencePrimitive? {
    if (isEmpty()) return null
    return VerticalStackPrimitive(UUID.randomUUID(), items = this.map { it.mapPrimitive() })
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
