package com.appcues

import com.appcues.action.ActionProcessor
import com.appcues.action.ExperienceAction
import com.appcues.analytics.AnalyticsTracker
import com.appcues.analytics.ExperienceLifecycleEvent.StepInteraction.InteractionType.BUTTON_TAPPED
import com.appcues.data.model.RenderContext
import com.appcues.data.model.StepReference.StepOffset
import com.appcues.ui.ExperienceRenderer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

// Suppressing until figure out a way to make this class more concise
@Suppress("LongParameterList")
internal class AppcuesExperienceActionsImpl internal constructor(
    private val identifier: String,
    private val actions: List<ExperienceAction>,
    private val renderContext: RenderContext,
    private val coroutineScope: CoroutineScope,
    private val analyticsTracker: AnalyticsTracker,
    private val experienceRenderer: ExperienceRenderer,
    private val actionsProcessor: ActionProcessor,
) : AppcuesExperienceActions {

    override fun triggerBlockActions() {
        actionsProcessor.process(renderContext, actions, BUTTON_TAPPED, "Custom component $identifier")
    }

    override fun nextStep() {
        coroutineScope.launch {
            experienceRenderer.show(renderContext, StepOffset(1))
        }
    }

    override fun previousStep() {
        coroutineScope.launch {
            experienceRenderer.show(renderContext, StepOffset(-1))
        }
    }

    override fun close(markComplete: Boolean) {
        coroutineScope.launch {
            experienceRenderer.dismiss(renderContext, markComplete = markComplete, destroyed = false)
        }
    }

    override fun track(name: String, properties: Map<String, Any>?) {
        analyticsTracker.track(name, properties)
    }

    override fun updateProfile(properties: Map<String, String>) {
        analyticsTracker.identify(properties, interactive = false)
    }
}
