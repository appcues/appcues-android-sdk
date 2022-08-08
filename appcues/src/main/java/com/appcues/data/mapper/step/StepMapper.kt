package com.appcues.data.mapper.step

import com.appcues.data.mapper.LeveledTraitResponse
import com.appcues.data.mapper.action.ActionsMapper
import com.appcues.data.mapper.mergeActions
import com.appcues.data.mapper.mergeTraits
import com.appcues.data.mapper.trait.TraitsMapper
import com.appcues.data.model.Step
import com.appcues.data.remote.response.action.ActionResponse
import com.appcues.data.remote.response.step.StepResponse
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
        stepContainerTraits: List<LeveledTraitResponse>,
        stepContainerActions: Map<UUID, List<ActionResponse>>,
    ): Step {
        val stepTraits = from.traits.map { it to STEP }
        val mergedTraits = stepTraits.mergeTraits(stepContainerTraits)
        val mergedActions = from.actions.mergeActions(stepContainerActions)

        return Step(
            id = from.id,
            content = stepContentMapper.map(from.content),
            stepDecoratingTraits = traitsMapper.map(mergedTraits).filterIsInstance(StepDecoratingTrait::class.java),
            actions = actionsMapper.map(mergedActions),
            type = from.type,
        )
    }
}
