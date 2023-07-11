package com.appcues.action.appcues

import com.appcues.action.ExperienceAction
import com.appcues.analytics.AnalyticsTracker
import com.appcues.analytics.ExperienceLifecycleEvent.StepInteraction
import com.appcues.analytics.ExperienceLifecycleEvent.StepInteraction.InteractionType
import com.appcues.data.model.AppcuesConfigMap
import com.appcues.data.model.RenderContext
import com.appcues.ui.ExperienceRenderer

internal class StepInteractionAction(
    override val config: AppcuesConfigMap,
    private val renderContext: RenderContext,
    private val interaction: StepInteractionData,
    private val analyticsTracker: AnalyticsTracker,
    private val experienceRenderer: ExperienceRenderer,
) : ExperienceAction {

    class StepInteractionData(
        val interactionType: InteractionType,
        viewDescription: String?,
        category: String,
        destination: String,
    ) {

        val properties = hashMapOf<String, Any>(
            "destination" to destination,
            "category" to category,
            "text" to (viewDescription ?: String())
        )
    }

    companion object {

        const val TYPE = "@appcues/step_interaction"
    }

    override suspend fun execute() {
        val state = experienceRenderer.getState(renderContext)
        val experience = state.currentExperience
        val stepIndex = state.currentStepIndex

        if (experience != null && stepIndex != null && experience.published) {
            val interactionEvent = StepInteraction(
                experience = experience,
                stepIndex = stepIndex,
                interactionType = interaction.interactionType,
                interactionProperties = interaction.properties
            )

            analyticsTracker.track(
                name = interactionEvent.name,
                properties = interactionEvent.properties,
                interactive = false,
                isInternal = true
            )
        }
    }
}
