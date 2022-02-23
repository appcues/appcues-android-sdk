package com.appcues.data.mapper.experience

import com.appcues.data.mapper.step.StepMapper
import com.appcues.data.mapper.trait.TraitMapper
import com.appcues.data.model.Experience
import com.appcues.data.model.Step
import com.appcues.data.model.Trait
import com.appcues.data.remote.response.experience.ExperienceResponse
import com.appcues.data.remote.response.step.StepResponse
import com.appcues.data.remote.response.trait.TraitResponse

internal class ExperienceMapper(
    private val stepMapper: StepMapper,
    private val traitMapper: TraitMapper = TraitMapper(),
) {

    fun map(from: ExperienceResponse): Experience {
        return Experience(
            id = from.id,
            name = from.name,
            traits = from.traits.mapToTrait { traitMapper.map(it) },
            steps = from.steps.mapToStep { stepMapper.map(it, from.actions) },
        )
    }

    private fun List<TraitResponse>.mapToTrait(transform: (TraitResponse) -> Trait) = map { transform(it) }

    private fun List<StepResponse>.mapToStep(transform: (StepResponse) -> Step) = map { transform(it) }
}
