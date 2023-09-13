package com.appcues.action.appcues

import com.appcues.action.ExperienceAction
import com.appcues.analytics.Analytics
import com.appcues.analytics.ExperienceLifecycleEvent.StepInteraction
import com.appcues.analytics.ExperienceLifecycleEvent.StepInteraction.InteractionType
import com.appcues.data.model.AppcuesConfigMap
import com.appcues.data.model.RenderContext
import com.appcues.experiences.Experiences

internal class StepInteractionAction(
    private val renderContext: RenderContext,
    private val interaction: StepInteractionData,
    private val analytics: Analytics,
    private val experiences: Experiences,
) : ExperienceAction {

    // required in ExperienceAction interface but not used in this action
    override val config: AppcuesConfigMap = null

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
        // TODO change this to new approach where we track step interaction directly from Analytics
        val experienceState = experiences.getExperienceState(renderContext) ?: return

        // do nothing if experience is not published
        if (!experienceState.experience.published) return

        StepInteraction(experienceState.experience, experienceState.stepIndex, interaction.interactionType, interaction.properties).run {
            analytics.track(name, properties, interactive = false, isInternal = true)
        }
    }
}
