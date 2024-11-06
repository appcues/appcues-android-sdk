package com.appcues

import com.appcues.action.ActionProcessor
import com.appcues.action.ExperienceAction
import com.appcues.action.appcues.CloseAction
import com.appcues.action.appcues.ContinueAction
import com.appcues.action.appcues.TrackEventAction
import com.appcues.action.appcues.UpdateProfileAction
import com.appcues.analytics.AnalyticsTracker
import com.appcues.analytics.ExperienceLifecycleEvent.StepInteraction.InteractionType.BUTTON_TAPPED
import com.appcues.data.model.RenderContext
import com.appcues.data.model.StepReference.StepOffset
import com.appcues.ui.ExperienceRenderer

// Suppressing until figure out a way to make this class more concise
@Suppress("LongParameterList")
internal class AppcuesExperienceActionsImpl internal constructor(
    private val identifier: String,
    private val actions: List<ExperienceAction>,
    private val actionsProcessor: ActionProcessor,
    private val renderContext: RenderContext,
    private val analyticsTracker: AnalyticsTracker,
    private val experienceRenderer: ExperienceRenderer,
) : AppcuesExperienceActions {

    override fun triggerBlockActions() {
        actionsProcessor.enqueue(renderContext, actions, BUTTON_TAPPED, "Custom component $identifier")
    }

    override fun nextStep() {
        actionsProcessor.enqueue(ContinueAction(renderContext, experienceRenderer, StepOffset(1)))
    }

    override fun previousStep() {
        actionsProcessor.enqueue(ContinueAction(renderContext, experienceRenderer, StepOffset(-1)))
    }

    override fun close(markComplete: Boolean) {
        actionsProcessor.enqueue(CloseAction(renderContext, experienceRenderer, markComplete))
    }

    override fun track(name: String, properties: Map<String, Any>?) {
        actionsProcessor.enqueue(TrackEventAction(analyticsTracker, name, properties))
    }

    override fun updateProfile(properties: Map<String, String>) {
        actionsProcessor.enqueue(UpdateProfileAction(properties, analyticsTracker))
    }
}
