package com.appcues

import com.appcues.analytics.AnalyticsTracker
import com.appcues.data.model.RenderContext
import com.appcues.data.model.StepReference.StepOffset
import com.appcues.ui.ExperienceRenderer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * TODO document this
 */
public class AppcuesExperienceActions internal constructor(
    private val renderContext: RenderContext,
    private val coroutineScope: CoroutineScope,
    private val analyticsTracker: AnalyticsTracker,
    private val experienceRenderer: ExperienceRenderer
) {

    /**
     * TODO document this
     * TODO implement it
     */
    public fun triggerBlockActions() {
        // TODO
    }

    /**
     * TODO document this
     */
    public fun nextStep() {
        coroutineScope.launch {
            experienceRenderer.show(renderContext, StepOffset(1))
        }
    }

    /**
     * TODO document this
     */
    public fun previousStep() {
        coroutineScope.launch {
            experienceRenderer.show(renderContext, StepOffset(-1))
        }
    }

    /**
     * TODO document this
     */
    public fun close(markComplete: Boolean = true) {
        coroutineScope.launch {
            experienceRenderer.dismiss(renderContext, markComplete = markComplete, destroyed = false)
        }
    }

    /**
     * TODO document this
     */
    public fun track(name: String, properties: Map<String, Any>? = null) {
        analyticsTracker.track(name, properties)
    }

    /**
     * TODO document this
     */
    public fun updateProfile(map: Map<String, String>) {
        analyticsTracker.identify(map, interactive = false)
    }
}
