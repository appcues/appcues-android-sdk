package com.appcues.data.mapper.step

import com.appcues.data.mapper.action.ActionsMapper
import com.appcues.data.mapper.mergeActions
import com.appcues.data.mapper.mergeTraits
import com.appcues.data.mapper.trait.TraitsMapper
import com.appcues.data.model.Step
import com.appcues.data.remote.response.action.ActionResponse
import com.appcues.data.remote.response.step.StepResponse
import com.appcues.trait.ExperienceTrait
import com.appcues.trait.ExperienceTrait.ExperienceTraitLevel.STEP
import com.appcues.trait.StepDecoratingTrait
import java.util.UUID

internal class StepMapper(
    private val stepContentMapper: StepContentMapper,
    private val traitsMapper: TraitsMapper,
    private val actionsMapper: ActionsMapper,
) {

    fun map(
        from: StepResponse,
        superTraits: List<ExperienceTrait>,
        superActions: Map<UUID, List<ActionResponse>>,
    ): Step {
        val mappedTraits = traitsMapper.map(from.traits)
        val mergedTraits = mappedTraits.mergeTraits(superTraits, STEP)
        val mergedActions = from.actions.mergeActions(superActions)

        return Step(
            id = from.id,
            content = stepContentMapper.map(from.content),
            stepDecoratingTraits = mergedTraits.filterIsInstance(StepDecoratingTrait::class.java),
            actions = actionsMapper.map(mergedActions),
            type = from.type,
        )
    }
}
