package com.appcues.data.mapper.experience

import com.appcues.data.mapper.step.StepMapper
import com.appcues.data.mapper.trait.TraitsMapper
import com.appcues.data.model.Experience
import com.appcues.data.model.StepContainer
import com.appcues.data.remote.response.action.ActionResponse
import com.appcues.data.remote.response.experience.ExperienceResponse
import com.appcues.data.remote.response.step.StepContainerResponse
import com.appcues.trait.BackdropDecoratingTrait
import com.appcues.trait.ContainerDecoratingTrait
import com.appcues.trait.ContentWrappingTrait
import com.appcues.trait.ExperiencePresentingTrait
import java.util.UUID

internal class ExperienceMapper(
    private val stepMapper: StepMapper,
    private val traitsMapper: TraitsMapper,
) {

    fun map(from: ExperienceResponse): Experience {
        return Experience(
            id = from.id,
            name = from.name,
            stepContainer = from.steps.mapToStepContainer(from.actions)
        )
    }

    private fun List<StepContainerResponse>.mapToStepContainer(experienceActions: HashMap<UUID, List<ActionResponse>>?) =
        map { stepContainerResponse ->
            val traits = traitsMapper.map(stepContainerResponse.traits)
            // this is where we will use groups to organize steps into stepContainers
            // also merge all necessary traits for each step
            StepContainer(
                steps = stepContainerResponse.children.map { step ->
                    stepMapper.map(
                        step,
                        stepContainerResponse.actions,
                        experienceActions,
                    )
                },
                presentingTrait = traits.filterIsInstance(ExperiencePresentingTrait::class.java).first(),
                contentWrappingTrait = traits.filterIsInstance(ContentWrappingTrait::class.java).first(),
                backdropTraits = traits.filterIsInstance(BackdropDecoratingTrait::class.java),
                containerTraits = traits.filterIsInstance(ContainerDecoratingTrait::class.java),
            )
        }
}
