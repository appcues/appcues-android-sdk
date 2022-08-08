package com.appcues.data.mapper.experience

import android.content.Context
import com.appcues.action.ExperienceAction
import com.appcues.action.appcues.LaunchExperienceAction
import com.appcues.action.appcues.LinkAction
import com.appcues.data.mapper.mergeActions
import com.appcues.data.mapper.mergeTraits
import com.appcues.data.mapper.step.StepMapper
import com.appcues.data.mapper.trait.TraitsMapper
import com.appcues.data.model.Experience
import com.appcues.data.model.ExperiencePriority
import com.appcues.data.model.ExperiencePriority.NORMAL
import com.appcues.data.model.StepContainer
import com.appcues.data.remote.response.action.ActionResponse
import com.appcues.data.remote.response.experience.ExperienceResponse
import com.appcues.data.remote.response.step.StepContainerResponse
import com.appcues.trait.BackdropDecoratingTrait
import com.appcues.trait.ContainerDecoratingTrait
import com.appcues.trait.ContentHolderTrait
import com.appcues.trait.ContentWrappingTrait
import com.appcues.trait.ExperienceTrait
import com.appcues.trait.ExperienceTrait.ExperienceTraitLevel.GROUP
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

    fun map(from: ExperienceResponse, priority: ExperiencePriority = NORMAL): Experience {
        return Experience(
            id = from.id,
            name = from.name,
            stepContainers = from.steps.mapToStepContainer(traitsMapper.map(from.traits), from.actions),
            published = from.state != "DRAFT", // "DRAFT" is used for experience preview in builder
            priority = priority,
            type = from.type,
            publishedAt = from.publishedAt,
            completionActions = arrayListOf<ExperienceAction>().apply {
                from.redirectUrl?.let { add(LinkAction(it, scope.get())) }
                from.nextContentId?.let { add(LaunchExperienceAction(it)) }
            }
        )
    }

    private fun List<StepContainerResponse>.mapToStepContainer(
        superTraits: List<ExperienceTrait>,
        superActions: Map<UUID, List<ActionResponse>>?
    ) = map { stepContainerResponse ->
        val mappedTraits = traitsMapper.map(stepContainerResponse.traits)
        val mergedTraits = mappedTraits.mergeTraits(superTraits, GROUP)
        val mergedActions = stepContainerResponse.actions.mergeActions(superActions)

        // this is where we will use groups to organize steps into stepContainers
        // also merge all necessary traits for each step
        StepContainer(
            steps = stepContainerResponse.children.map { step -> stepMapper.map(step, mergedTraits, mergedActions) },
            presentingTrait = mergedTraits.getExperiencePresentingTraitOrDefault(),
            contentHolderTrait = mergedTraits.getContainerCreatingTraitOrDefault(),
            // what should we do if no content wrapping trait is found?
            contentWrappingTrait = mergedTraits.filterIsInstance<ContentWrappingTrait>().first(),
            backdropDecoratingTraits = mergedTraits.filterIsInstance<BackdropDecoratingTrait>(),
            containerDecoratingTraits = mergedTraits.filterIsInstance<ContainerDecoratingTrait>(),
        )
    }

    private fun List<ExperienceTrait>.getContainerCreatingTraitOrDefault(): ContentHolderTrait {
        return filterIsInstance<ContentHolderTrait>().firstOrNull() ?: DefaultContentHolderTrait(null)
    }

    private fun List<ExperienceTrait>.getExperiencePresentingTraitOrDefault(): PresentingTrait {
        return filterIsInstance<PresentingTrait>().firstOrNull() ?: DefaultPresentingTrait(null, scope, context)
    }
}
