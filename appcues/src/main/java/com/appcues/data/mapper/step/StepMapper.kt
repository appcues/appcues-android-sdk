package com.appcues.data.mapper.step

import com.appcues.data.mapper.LeveledTraitResponse
import com.appcues.data.mapper.action.ActionsMapper
import com.appcues.data.mapper.mergeTraits
import com.appcues.data.mapper.step.primitives.mapPrimitive
import com.appcues.data.mapper.trait.TraitsMapper
import com.appcues.data.model.Step
import com.appcues.data.remote.response.step.StepResponse
import com.appcues.trait.ExperienceTraitLevel.STEP
import com.appcues.trait.StepDecoratingTrait

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

        return Step(
            id = from.id,
            content = from.content.mapPrimitive(),
            stepDecoratingTraits = traitsMapper.map(mergedTraits).filterIsInstance(StepDecoratingTrait::class.java),
            actions = actionsMapper.map(from.actions),
            type = from.type,
        )
    }
}
