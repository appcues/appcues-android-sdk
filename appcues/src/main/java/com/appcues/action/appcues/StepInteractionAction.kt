package com.appcues.action.appcues

import com.appcues.action.ExperienceAction
import com.appcues.analytics.AnalyticsTracker
import com.appcues.analytics.ExperienceLifecycleEvent.StepInteraction
import com.appcues.analytics.ExperienceLifecycleEvent.StepInteraction.InteractionType
import com.appcues.data.model.AppcuesConfigMap
import com.appcues.statemachine.StateMachine

internal class StepInteractionAction(
    override val config: AppcuesConfigMap,
    val interaction: StepInteractionData,
    private val analyticsTracker: AnalyticsTracker,
    private val stateMachine: StateMachine,
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
        val experience = stateMachine.state.currentExperience
        val stepIndex = stateMachine.state.currentStepIndex

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
