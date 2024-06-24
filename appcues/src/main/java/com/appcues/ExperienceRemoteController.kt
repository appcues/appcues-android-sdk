package com.appcues

import com.appcues.analytics.AnalyticsTracker
import com.appcues.analytics.ExperienceLifecycleEvent.StepInteraction
import com.appcues.analytics.ExperienceLifecycleEvent.StepInteraction.InteractionType.FORM_SUBMITTED
import com.appcues.data.model.RenderContext
import com.appcues.data.model.StepReference.StepIndex
import com.appcues.data.model.StepReference.StepOffset
import com.appcues.ui.ExperienceRenderer
import com.appcues.util.toSlug
import kotlinx.coroutines.launch

public class ExperienceRemoteController internal constructor(
    private val renderContext: RenderContext,
    private val coroutineScope: AppcuesCoroutineScope,
    private val analyticsTracker: AnalyticsTracker,
    private val experienceRenderer: ExperienceRenderer
) {

    public fun track(name: String, properties: Map<String, Any>? = null) {
        analyticsTracker.track(name, properties)
    }

    public fun nextStep() {
        coroutineScope.launch {
            experienceRenderer.show(renderContext, StepOffset(1))
        }
    }

    public fun previousStep() {
        coroutineScope.launch {
            experienceRenderer.show(renderContext, StepOffset(-1))
        }
    }

    public fun goToStep(index: Int) {
        coroutineScope.launch {
            experienceRenderer.show(renderContext, StepIndex(index))
        }
    }

    public fun close(markComplete: Boolean = true) {
        coroutineScope.launch {
            experienceRenderer.dismiss(renderContext, markComplete = markComplete, destroyed = false)
        }
    }

    public fun submitForm(formName: String, map: Map<String, String>) {
        val (experience, stepIndex) = experienceRenderer.getState(renderContext).let {
            it?.currentExperience to it?.currentStepIndex
        }

        if (experience != null && stepIndex != null) {
            // set user profile attributes to capture the form question/answer
            analyticsTracker.identify(map.formattedAsProfileUpdate(formName), interactive = false)

            // track the interaction event
            val interactionEvent = StepInteraction(experience, stepIndex, FORM_SUBMITTED)
            analyticsTracker.track(interactionEvent.name, interactionEvent.properties, interactive = false, isInternal = true)
        }
    }

    public fun updateProfile(map: Map<String, String>) {
        analyticsTracker.identify(map, interactive = false)
    }

    private fun Map<String, String>.formattedAsProfileUpdate(formName: String): Map<String, Any> {
        val profileUpdate = hashMapOf<String, Any>()

        forEach {
            profileUpdate["${formName}_${it.key.toSlug()}"] = it.value
        }

        return profileUpdate
    }
}
