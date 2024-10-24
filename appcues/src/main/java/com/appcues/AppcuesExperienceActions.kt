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

/**
 * AppcuesExperienceActions is the controller that allows custom component access useful appcues and experience calls.
 */
// Suppressing until figure out a way to make this class more concise
@Suppress("LongParameterList")
public class AppcuesExperienceActions internal constructor(
    private val identifier: String,
    private val renderContext: RenderContext,
    private val coroutineScope: CoroutineScope,
    private val analyticsTracker: AnalyticsTracker,
    private val experienceRenderer: ExperienceRenderer,
    private val actions: List<ExperienceAction>,
    private val actionsProcessor: ActionProcessor,
) {

    /**
     * Manually trigger all actions defined in studio for this custom block
     */
    public fun triggerBlockActions() {
        actionsProcessor.process(renderContext, actions, BUTTON_TAPPED, "Custom component $identifier")
    }

    /**
     * Move to the next step or completes the experience if called from the last step
     */
    public fun nextStep() {
        coroutineScope.launch {
            experienceRenderer.show(renderContext, StepOffset(1))
        }
    }

    /**
     * Move to a previous step if any
     */
    public fun previousStep() {
        coroutineScope.launch {
            experienceRenderer.show(renderContext, StepOffset(-1))
        }
    }

    /**
     * Closes the experience
     *
     * @param markComplete if set to true signals that the experience completed successfully.
     *                     leaving it as false (default) means that this experience is not completed
     *                     analytics wise, like when it was dismissed by tapping on a "X" close
     */
    public fun close(markComplete: Boolean = false) {
        coroutineScope.launch {
            experienceRenderer.dismiss(renderContext, markComplete = markComplete, destroyed = false)
        }
    }

    /**
     * Track an action taken by a user.
     *
     * @param name Name of the event.
     * @param properties Optional properties that provide additional context about the event.
     */
    public fun track(name: String, properties: Map<String, Any>? = null) {
        analyticsTracker.track(name, properties)
    }

    /**
     * Updates user profile.
     *
     * @param properties properties that provide additional context about the user.
     */
    public fun updateProfile(properties: Map<String, String>) {
        analyticsTracker.identify(properties, interactive = false)
    }
}
