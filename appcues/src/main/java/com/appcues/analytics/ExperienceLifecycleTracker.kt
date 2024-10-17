package com.appcues.analytics

import com.appcues.AppcuesConfig
import com.appcues.Storage
import com.appcues.analytics.ExperienceLifecycleEvent.ExperienceCompleted
import com.appcues.analytics.ExperienceLifecycleEvent.ExperienceDismissed
import com.appcues.analytics.ExperienceLifecycleEvent.ExperienceError
import com.appcues.analytics.ExperienceLifecycleEvent.ExperienceStarted
import com.appcues.analytics.ExperienceLifecycleEvent.StepCompleted
import com.appcues.analytics.ExperienceLifecycleEvent.StepError
import com.appcues.analytics.ExperienceLifecycleEvent.StepRecovered
import com.appcues.analytics.ExperienceLifecycleEvent.StepSeen
import com.appcues.data.model.Experience
import com.appcues.di.component.AppcuesComponent
import com.appcues.di.component.inject
import com.appcues.di.scope.AppcuesScope
import com.appcues.statemachine.Error
import com.appcues.statemachine.Error.ExperienceAlreadyActive
import com.appcues.statemachine.State
import com.appcues.statemachine.StateMachine
import com.appcues.statemachine.states.BeginningExperienceState
import com.appcues.statemachine.states.BeginningStepState
import com.appcues.statemachine.states.EndingExperienceState
import com.appcues.statemachine.states.EndingStepState
import com.appcues.statemachine.states.RenderingStepState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID

internal class ExperienceLifecycleTracker(
    override val scope: AppcuesScope,
) : AppcuesComponent {

    // lazy property injection to avoid circular DI reference in constructor
    // AnalyticsTracker -> this <- Analytics Tracker
    private val analyticsTracker: AnalyticsTracker by inject()
    private val storage: Storage by inject()
    private val appcuesCoroutineScope: CoroutineScope by inject()
    private val config: AppcuesConfig by inject()

    private var stateJob: Job? = null
    private var errorJob: Job? = null

    fun start(stateMachine: StateMachine, onEndedExperience: (Experience) -> Unit) {
        // ensure any existing observers are stopped before starting new ones
        stop()

        stateJob = appcuesCoroutineScope.launch { stateMachine.observeState(onEndedExperience) }
        errorJob = appcuesCoroutineScope.launch { stateMachine.observeErrors() }
    }

    fun stop() {
        stateJob?.cancel()
        errorJob?.cancel()

        stateJob = null
        errorJob = null
    }

    private suspend fun StateMachine.observeState(onEndedExperience: (Experience) -> Unit) {
        stateFlow.collect { state ->
            // will not track for unpublished (preview) experiences
            if (state.isPublished()) {
                state.track()
            }

            when {
                state is RenderingStepState && state.isFirst -> {
                    config.experienceListener?.experienceStarted(state.experience.id)
                }
                state is EndingExperienceState -> {
                    config.experienceListener?.experienceFinished(state.experience.id)
                    onEndedExperience(state.experience)
                }
            }
        }
    }

    private suspend fun StateMachine.observeErrors() {
        errorFlow.collect { error ->
            // will not track for unpublished (preview) experiences
            if (error.isPublished()) {
                error.track()
            }
        }
    }

    private fun State.track() {
        when (this) {
            is RenderingStepState -> {
                if (isFirst) {
                    // update this value for auto-properties
                    storage.lastContentShownAt = Date()
                    trackLifecycleEvent(ExperienceStarted(experience), SdkMetrics.trackRender(experience.requestId))
                }
                if (experience.renderErrorId != null) {
                    val errorId = experience.renderErrorId
                    trackLifecycleEvent(StepRecovered(experience, flatStepIndex), mapOf("errorId" to errorId.toString()))
                    experience.renderErrorId = null
                }
                trackLifecycleEvent(StepSeen(experience, flatStepIndex))
            }
            is EndingStepState -> {
                if (markComplete) {
                    trackLifecycleEvent(StepCompleted(experience, flatStepIndex))
                }
            }
            is EndingExperienceState -> {
                if (trackAnalytics) {
                    if (markComplete) {
                        // if ending on the last step OR an action requested it be considered complete explicitly,
                        // track the experience_completed event
                        trackLifecycleEvent(ExperienceCompleted(experience))
                    } else {
                        // otherwise its considered experience_dismissed (not completed)
                        trackLifecycleEvent(ExperienceDismissed(experience, flatStepIndex))
                    }
                }
            }
            else -> Unit
        }
    }

    private fun Error.track() {
        when (this) {
            is Error.ExperienceError -> {
                // only track error analytics the first time, as they may retry/recover later.
                if (experience.renderErrorId == null) {
                    experience.renderErrorId = UUID.randomUUID()
                    errorId = experience.renderErrorId
                    trackLifecycleEvent(ExperienceError(this))
                }
            }
            is Error.StepError -> {
                if (experience.renderErrorId == null) {
                    experience.renderErrorId = UUID.randomUUID()
                    errorId = experience.renderErrorId
                    trackLifecycleEvent(StepError(this))
                }
            }
            ExperienceAlreadyActive -> Unit
        }
    }

    private fun trackLifecycleEvent(event: ExperienceLifecycleEvent, additionalProperties: Map<String, Any> = emptyMap()) {
        val properties = event.properties.toMutableMap()
        properties.putAll(additionalProperties)
        analyticsTracker.track(event.name, properties, interactive = false, isInternal = true)
    }

    private fun State.isPublished(): Boolean =
        when (this) {
            is BeginningExperienceState -> experience.published
            is BeginningStepState -> experience.published
            is EndingExperienceState -> experience.published
            is EndingStepState -> experience.published
            is RenderingStepState -> experience.published
            else -> false
        }

    private fun Error.isPublished(): Boolean =
        when (this) {
            is Error.ExperienceError -> experience.published
            is Error.StepError -> experience.published
            ExperienceAlreadyActive -> false
        }
}
