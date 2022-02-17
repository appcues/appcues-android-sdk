package com.appcues.data.mapper.experience

import com.appcues.data.mapper.action.ActionMapper
import com.appcues.data.mapper.action.mapValuesToAction
import com.appcues.data.mapper.step.StepMapper
import com.appcues.data.mapper.step.mapToStep
import com.appcues.data.mapper.trait.TraitMapper
import com.appcues.data.mapper.trait.mapToTrait
import com.appcues.data.model.Experience
import com.appcues.data.remote.response.experience.ExperienceResponse

internal class ExperienceMapper(
    private val actionMapper: ActionMapper = ActionMapper(),
    private val traitMapper: TraitMapper = TraitMapper(),
    private val stepMapper: StepMapper = StepMapper(),
) {

    fun map(from: ExperienceResponse): Experience {
        return Experience(
            id = from.id,
            name = from.name,
            actions = from.actions.mapValuesToAction { actionMapper.map(it) },
            traits = from.traits.mapToTrait { traitMapper.map(it) },
            steps = from.steps.mapToStep { stepMapper.map(it) },
        )
    }
}
