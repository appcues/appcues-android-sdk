package com.appcues.action.appcues

import com.appcues.Appcues
import com.appcues.action.ExperienceAction
import com.appcues.action.ExperienceActionQueueTransforming
import com.appcues.analytics.Analytics
import com.appcues.analytics.ExperienceLifecycleEvent.StepInteraction
import com.appcues.analytics.ExperienceLifecycleEvent.StepInteraction.InteractionType.FORM_SUBMITTED
import com.appcues.analytics.formattedAsProfileUpdate
import com.appcues.data.model.AppcuesConfigMap
import com.appcues.data.model.RenderContext
import com.appcues.data.model.getConfigOrDefault
import com.appcues.experiences.Experiences

internal class SubmitFormAction(
    override val config: AppcuesConfigMap,
    private val renderContext: RenderContext,
    private val experiences: Experiences,
    private val analytics: Analytics,
) : ExperienceActionQueueTransforming {

    companion object {

        const val TYPE = "@appcues/submit-form"
    }

    private val skipValidation = config.getConfigOrDefault("skipValidation", false)

    // validate form and block future actions if needed
    override fun transformQueue(queue: List<ExperienceAction>, index: Int, appcues: Appcues): List<ExperienceAction> {
        val experienceState = experiences.getExperienceState(renderContext) ?: return queue
        val formState = experienceState.step.formState

        // do nothing if we skipValidation or if form is complete
        if (skipValidation || formState.isFormComplete) return queue

        // set show errors to true
        formState.shouldShowErrors.value = true
        // remove this action and all subsequent
        return queue.toMutableList().dropLast(queue.count() - index)
    }

    // reports analytics for step interaction, for the form submission
    override suspend fun execute() {
        // TODO change this to new approach where we track step interaction directly from Analytics
        val experienceState = experiences.getExperienceState(renderContext) ?: return
        val formState = experienceState.step.formState

        // set user profile attributes to capture the form question/answer
        analytics.updateProfile(formState.formattedAsProfileUpdate(), true)

        // track the interaction event
        StepInteraction(experienceState.experience, experienceState.stepIndex, FORM_SUBMITTED).run {
            analytics.track(name, properties, interactive = false, isInternal = true)
        }
    }
}
