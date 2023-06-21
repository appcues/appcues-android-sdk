package com.appcues.action.appcues

import com.appcues.Appcues
import com.appcues.action.ExperienceAction
import com.appcues.action.ExperienceActionQueueTransforming
import com.appcues.analytics.AnalyticsTracker
import com.appcues.analytics.ExperienceLifecycleEvent.StepInteraction
import com.appcues.analytics.ExperienceLifecycleEvent.StepInteraction.InteractionType.FORM_SUBMITTED
import com.appcues.analytics.formattedAsProfileUpdate
import com.appcues.data.model.AppcuesConfigMap
import com.appcues.data.model.RenderContext
import com.appcues.data.model.getConfigOrDefault
import com.appcues.ui.ExperienceRenderer

internal class SubmitFormAction(
    override val config: AppcuesConfigMap,
    private val renderContext: RenderContext,
    private val experienceRenderer: ExperienceRenderer,
    private val analyticsTracker: AnalyticsTracker,
) : ExperienceActionQueueTransforming {

    companion object {

        const val TYPE = "@appcues/submit-form"
    }

    private val skipValidation = config.getConfigOrDefault("skipValidation", false)

    // validate form and block future actions if needed
    override fun transformQueue(queue: List<ExperienceAction>, index: Int, appcues: Appcues): List<ExperienceAction> {
        if (skipValidation) {
            return queue
        }

        val (experience, stepIndex) = experienceRenderer.getState(renderContext)
            ?.let { (it.currentExperience to it.currentStepIndex) } ?: (null to null)

        if (experience != null && stepIndex != null) {
            val formState = experience.flatSteps[stepIndex].formState

            if (!formState.isFormComplete) {
                formState.shouldShowErrors.value = true
                // remove this action and all subsequent
                return queue.toMutableList().dropLast(queue.count() - index)
            }
        }

        return queue
    }

    // reports analytics for step interaction, for the form submission
    override suspend fun execute() {
        val (experience, stepIndex) = experienceRenderer.getState(renderContext)
            ?.let { (it.currentExperience to it.currentStepIndex) } ?: (null to null)

        if (experience != null && stepIndex != null) {
            val formState = experience.flatSteps[stepIndex].formState

            // set user profile attributes to capture the form question/answer
            analyticsTracker.identify(formState.formattedAsProfileUpdate(), interactive = false)

            // track the interaction event
            val interactionEvent = StepInteraction(experience, stepIndex, FORM_SUBMITTED)
            analyticsTracker.track(interactionEvent.name, interactionEvent.properties, interactive = false, isInternal = true)
        }
    }
}
