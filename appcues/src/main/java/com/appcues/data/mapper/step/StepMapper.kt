package com.appcues.data.mapper.step

import com.appcues.data.mapper.action.ActionMapper
import com.appcues.data.mapper.action.mapValuesToAction
import com.appcues.data.mapper.trait.TraitMapper
import com.appcues.data.mapper.trait.mapToTrait
import com.appcues.data.remote.response.step.StepResponse
import com.appcues.domain.entity.step.Step

internal class StepMapper(
    private val actionMapper: ActionMapper = ActionMapper(),
    private val traitMapper: TraitMapper = TraitMapper(),
    private val stepContentMapper: StepContentMapper = StepContentMapper()
) {

    fun map(from: StepResponse) = Step(
        id = from.id,
        content = stepContentMapper.map(from.content),
        actions = from.actions.mapValuesToAction { actionMapper.map(it) },
        traits = from.traits.mapToTrait { traitMapper.map(it) },
    )
}
