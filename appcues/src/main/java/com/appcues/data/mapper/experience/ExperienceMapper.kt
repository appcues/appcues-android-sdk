package com.appcues.data.mapper.experience

import android.content.Context
import com.appcues.data.mapper.step.StepMapper
import com.appcues.data.mapper.trait.TraitsMapper
import com.appcues.data.model.Experience
import com.appcues.data.model.StepContainer
import com.appcues.data.remote.response.action.ActionResponse
import com.appcues.data.remote.response.experience.ExperienceResponse
import com.appcues.data.remote.response.step.StepContainerResponse
import com.appcues.data.remote.response.trait.TraitResponse
import com.appcues.trait.BackdropDecoratingTrait
import com.appcues.trait.ContainerDecoratingTrait
import com.appcues.trait.ContentHolderTrait
import com.appcues.trait.ContentWrappingTrait
import com.appcues.trait.ExperienceTrait
import com.appcues.trait.PresentingTrait
import com.appcues.trait.appcues.DefaultContentHolderTrait
import com.appcues.trait.appcues.DefaultPresentingTrait
import org.koin.core.scope.Scope
import java.util.UUID

internal class ExperienceMapper(
    private val stepMapper: StepMapper,
    private val traitsMapper: TraitsMapper,
    private val scope: Scope,
    private val context: Context,
) {

    fun map(from: ExperienceResponse): Experience {
        return Experience(
            id = from.id,
            name = from.name,
            stepContainers = from.steps.mapToStepContainer(from.traits, from.actions)
        )
    }

    private fun List<StepContainerResponse>.mapToStepContainer(
        experienceTraits: List<TraitResponse>,
        experienceActions: HashMap<UUID, List<ActionResponse>>?
    ) = map { stepContainerResponse ->
        val traits = traitsMapper.map(experienceTraits.mergeWith(stepContainerResponse.traits))
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
            presentingTrait = traits.getExperiencePresentingTraitOrDefault(),
            contentHolderTrait = traits.getContainerCreatingTraitOrDefault(),
            // what should we do if no content wrapping trait is found?
            contentWrappingTrait = traits.filterIsInstance<ContentWrappingTrait>().first(),
            backdropTraits = traits.filterIsInstance<BackdropDecoratingTrait>(),
            containerTraits = traits.filterIsInstance<ContainerDecoratingTrait>(),
        )
    }

    private fun List<ExperienceTrait>.getContainerCreatingTraitOrDefault(): ContentHolderTrait {
        return filterIsInstance<ContentHolderTrait>().firstOrNull() ?: DefaultContentHolderTrait(null)
    }

    private fun List<ExperienceTrait>.getExperiencePresentingTraitOrDefault(): PresentingTrait {
        return filterIsInstance<PresentingTrait>().firstOrNull() ?: DefaultPresentingTrait(null, scope, context)
    }

    private fun List<TraitResponse>.mergeWith(other: List<TraitResponse>): List<TraitResponse> {
        return mutableListOf<TraitResponse>().apply {
            addAll(this@mergeWith)

            other.forEach { trait ->
                if (none { it.type == trait.type }) {
                    add(trait)
                }
            }
        }
    }
}
