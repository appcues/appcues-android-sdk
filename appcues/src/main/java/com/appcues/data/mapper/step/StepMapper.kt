package com.appcues.data.mapper.step

import com.appcues.data.mapper.action.ActionsMapper
import com.appcues.data.mapper.mergeActions
import com.appcues.data.mapper.mergeTraits
import com.appcues.data.mapper.trait.TraitsMapper
import com.appcues.data.model.Step
import com.appcues.data.remote.response.action.ActionResponse
import com.appcues.data.remote.response.step.StepResponse
import com.appcues.data.remote.response.trait.TraitResponse
import com.appcues.trait.StepDecoratingTrait
import java.util.UUID

internal class StepMapper(
    private val stepContentMapper: StepContentMapper,
    private val traitsMapper: TraitsMapper,
    private val actionsMapper: ActionsMapper,
) {

    fun map(
        from: StepResponse,
        superTraits: List<TraitResponse>,
        superActions: HashMap<UUID, List<ActionResponse>>?,
    ): Step {
        val mergedTraits = from.traits.mergeTraits(superTraits)
        val mergedActions = from.actions.mergeActions(superActions)

        return Step(
            id = from.id,
            content = stepContentMapper.map(from.content),
            traits = traitsMapper.map(mergedTraits).filterIsInstance(StepDecoratingTrait::class.java),
            actions = actionsMapper.map(mergedActions)
        )
    }
}
