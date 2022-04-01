package com.appcues.data.mapper.experience

import android.content.Context
import com.appcues.data.mapper.mergeActions
import com.appcues.data.mapper.mergeTraits
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
            stepContainers = from.steps.mapToStepContainer(from.traits, from.actions),
            published = from.state != "DRAFT" // "DRAFT" is used for experience preview in builder
        )
    }

    private fun List<StepContainerResponse>.mapToStepContainer(
        superTraits: List<TraitResponse>,
        superActions: HashMap<UUID, List<ActionResponse>>?
    ) = map { stepContainerResponse ->
        val mergedTraits = stepContainerResponse.traits.mergeTraits(superTraits)
        val mappedTraits = traitsMapper.map(mergedTraits)
        val mergedActions = stepContainerResponse.actions.mergeActions(superActions)

        // this is where we will use groups to organize steps into stepContainers
        // also merge all necessary traits for each step
        StepContainer(
            steps = stepContainerResponse.children.map { step -> stepMapper.map(step, mergedTraits, mergedActions) },
            presentingTrait = mappedTraits.getExperiencePresentingTraitOrDefault(),
            contentHolderTrait = mappedTraits.getContainerCreatingTraitOrDefault(),
            // what should we do if no content wrapping trait is found?
            contentWrappingTrait = mappedTraits.filterIsInstance<ContentWrappingTrait>().first(),
            backdropTraits = mappedTraits.filterIsInstance<BackdropDecoratingTrait>(),
            containerTraits = mappedTraits.filterIsInstance<ContainerDecoratingTrait>(),
        )
    }

    private fun List<ExperienceTrait>.getContainerCreatingTraitOrDefault(): ContentHolderTrait {
        return filterIsInstance<ContentHolderTrait>().firstOrNull() ?: DefaultContentHolderTrait(null)
    }

    private fun List<ExperienceTrait>.getExperiencePresentingTraitOrDefault(): PresentingTrait {
        return filterIsInstance<PresentingTrait>().firstOrNull() ?: DefaultPresentingTrait(null, scope, context)
    }
}
