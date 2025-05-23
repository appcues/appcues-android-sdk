package com.appcues.data.mapper.experience

import com.appcues.action.ExperienceAction
import com.appcues.action.appcues.LaunchExperienceAction
import com.appcues.action.appcues.LinkAction
import com.appcues.data.mapper.LeveledTraitResponse
import com.appcues.data.mapper.action.ActionsMapper
import com.appcues.data.mapper.mergeTraits
import com.appcues.data.mapper.step.StepMapper
import com.appcues.data.mapper.trait.TraitsMapper
import com.appcues.data.model.Experience
import com.appcues.data.model.ExperiencePriority
import com.appcues.data.model.ExperiencePriority.NORMAL
import com.appcues.data.model.ExperienceTrigger
import com.appcues.data.model.Experiment
import com.appcues.data.model.RenderContext
import com.appcues.data.model.StepContainer
import com.appcues.data.remote.appcues.response.ExperimentResponse
import com.appcues.data.remote.appcues.response.experience.ExperienceResponse
import com.appcues.data.remote.appcues.response.experience.FailedExperienceResponse
import com.appcues.data.remote.appcues.response.experience.LossyExperienceResponse
import com.appcues.data.remote.appcues.response.step.StepContainerResponse
import com.appcues.di.component.AppcuesComponent
import com.appcues.di.component.get
import com.appcues.di.scope.AppcuesScope
import com.appcues.trait.AppcuesTraitException
import com.appcues.trait.ContentHolderTrait
import com.appcues.trait.ContentWrappingTrait
import com.appcues.trait.ExperienceTrait
import com.appcues.trait.ExperienceTraitLevel.EXPERIENCE
import com.appcues.trait.ExperienceTraitLevel.GROUP
import com.appcues.trait.PresentingTrait
import com.appcues.trait.appcues.DefaultContentHolderTrait
import java.util.UUID

internal class ExperienceMapper(
    private val stepMapper: StepMapper,
    private val traitsMapper: TraitsMapper,
    private val actionsMapper: ActionsMapper,
    override val scope: AppcuesScope,
) : AppcuesComponent {

    // this version is used to map all decoded response objects, which may represent real Experiences
    // or failed responses with minimal info for error reporting
    fun mapDecoded(
        from: LossyExperienceResponse,
        trigger: ExperienceTrigger,
        priority: ExperiencePriority = NORMAL,
        experiments: List<ExperimentResponse>? = null,
        requestId: UUID? = null,
    ): Experience {
        return when (from) {
            is ExperienceResponse -> map(from, trigger, priority, experiments, requestId)
            is FailedExperienceResponse -> mapFailed(from, trigger, priority, experiments, requestId)
        }
    }

    // this version makes a synthetic Experience from the failure data, with just enough info
    // to report the flow issue about deserialization in the `error` field, for troubleshooting
    private fun mapFailed(
        from: FailedExperienceResponse,
        trigger: ExperienceTrigger,
        priority: ExperiencePriority = NORMAL,
        experiments: List<ExperimentResponse>? = null,
        requestId: UUID? = null,
    ): Experience {
        return Experience(
            id = from.id,
            name = from.name ?: "",
            stepContainers = emptyList(),
            published = true,
            priority = priority,
            type = from.type ?: "",
            renderContext = from.getRenderContext(),
            publishedAt = from.publishedAt,
            localeId = from.context?.localeId,
            localeName = from.context?.localeName,
            workflowId = from.context?.workflowId,
            workflowTaskId = from.context?.workflowTaskId,
            experiment = experiments?.getExperiment(from.id),
            completionActions = emptyList(),
            trigger = trigger,
            requestId = requestId,
            error = from.error,
        )
    }

    // this version maps the normal successful ExperienceResponse
    fun map(
        from: ExperienceResponse,
        trigger: ExperienceTrigger,
        priority: ExperiencePriority = NORMAL,
        experiments: List<ExperimentResponse>? = null,
        requestId: UUID? = null,
    ): Experience {
        val experienceTraits = from.traits.map { it to EXPERIENCE }
        val renderContext = from.getRenderContext()
        return Experience(
            id = from.id,
            name = from.name,
            stepContainers = from.steps.mapToStepContainer(renderContext, experienceTraits),
            published = from.state != "DRAFT", // "DRAFT" is used for experience preview in builder
            priority = priority,
            type = from.type,
            renderContext = renderContext,
            publishedAt = from.publishedAt,
            localeId = from.context?.localeId,
            localeName = from.context?.localeName,
            workflowId = from.context?.workflowId,
            workflowTaskId = from.context?.workflowTaskId,
            experiment = experiments?.getExperiment(from.id),
            completionActions = arrayListOf<ExperienceAction>().apply {
                from.redirectUrl?.let {
                    add(
                        LinkAction(
                            redirectUrl = it,
                            linkOpener = get(),
                            appcues = get(),
                            logcues = get(),
                        )
                    )
                }
                from.nextContentId?.let {
                    add(
                        LaunchExperienceAction(
                            renderContext = renderContext,
                            completedExperienceId = from.id.toString(),
                            launchExperienceId = it,
                            experienceRenderer = get(),
                        )
                    )
                }
            },
            trigger = trigger,
            requestId = requestId,
        )
    }

    private fun List<StepContainerResponse>.mapToStepContainer(
        renderContext: RenderContext,
        experienceTraits: List<LeveledTraitResponse>,
    ) = map { stepContainerResponse ->
        val containerTraits = stepContainerResponse.traits.map { it to GROUP }
        val mergedTraits = containerTraits.mergeTraits(experienceTraits)
        val mappedTraits = traitsMapper.map(renderContext, mergedTraits)
        val presentingTrait = mappedTraits.getExperiencePresentingTraitOrThrow()
        // this is where we will use groups to organize steps into stepContainers
        // also merge all necessary traits for each step
        StepContainer(
            id = stepContainerResponse.id,
            steps = stepContainerResponse.children.map { step -> stepMapper.map(renderContext, step, presentingTrait, mergedTraits) },
            actions = actionsMapper.map(renderContext, stepContainerResponse.actions),
            contentHolderTrait = mappedTraits.getContainerCreatingTraitOrDefault(),
            // what should we do if no content wrapping trait is found?
            contentWrappingTrait = mappedTraits.filterIsInstance<ContentWrappingTrait>().first(),
        )
    }

    private fun List<ExperienceTrait>.getContainerCreatingTraitOrDefault(): ContentHolderTrait {
        return filterIsInstance<ContentHolderTrait>().firstOrNull() ?: DefaultContentHolderTrait(null)
    }

    private fun List<ExperienceTrait>.getExperiencePresentingTraitOrThrow(): PresentingTrait {
        return filterIsInstance<PresentingTrait>().firstOrNull()
            ?: throw AppcuesTraitException("Presenting capability trait required")
    }

    private fun List<ExperimentResponse>.getExperiment(experienceId: UUID) =
        this.firstOrNull { it.experienceId == experienceId }?.let { experimentResponse ->
            Experiment(
                id = experimentResponse.experimentId,
                group = experimentResponse.group,
                experienceId = experimentResponse.experienceId,
                goalId = experimentResponse.goalId,
                contentType = experimentResponse.contentType
            )
        }

    private fun LossyExperienceResponse.getRenderContext(): RenderContext {
        return when (this) {
            is ExperienceResponse -> {
                // check for experience-level or first group-level embed trait
                val eligibleTraits = traits + ((steps.firstOrNull()?.traits) ?: listOf())
                eligibleTraits.firstOrNull { it.type == "@appcues/embedded" }?.let {
                    (it.config?.get("frameID") as? String)?.let { frameId -> RenderContext.Embed(frameId) }
                } ?: RenderContext.Modal
            }
            is FailedExperienceResponse -> RenderContext.Modal
        }
    }
}
