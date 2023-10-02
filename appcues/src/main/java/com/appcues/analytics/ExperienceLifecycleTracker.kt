package com.appcues.analytics

import com.appcues.AppcuesCoroutineScope
import com.appcues.Storage
import com.appcues.analytics.ExperienceLifecycleEvent.ExperienceCompleted
import com.appcues.analytics.ExperienceLifecycleEvent.ExperienceDismissed
import com.appcues.analytics.ExperienceLifecycleEvent.ExperienceError
import com.appcues.analytics.ExperienceLifecycleEvent.ExperienceStarted
import com.appcues.analytics.ExperienceLifecycleEvent.StepCompleted
import com.appcues.analytics.ExperienceLifecycleEvent.StepError
import com.appcues.analytics.ExperienceLifecycleEvent.StepSeen
import com.appcues.data.model.Experience
import com.appcues.statemachine.Error
import com.appcues.statemachine.Error.ExperienceAlreadyActive
import com.appcues.statemachine.Error.RenderContextNotActive
import com.appcues.statemachine.State
import com.appcues.statemachine.State.BeginningExperience
import com.appcues.statemachine.State.BeginningStep
import com.appcues.statemachine.State.EndingExperience
import com.appcues.statemachine.State.EndingStep
import com.appcues.statemachine.State.Idling
import com.appcues.statemachine.State.RenderingStep
import com.appcues.statemachine.StateMachine
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import java.util.Date

internal class ExperienceLifecycleTracker(
    override val scope: Scope,
) : KoinScopeComponent {

    // lazy property injection to avoid circular DI reference in constructor
    // AnalyticsTracker -> this <- Analytics Tracker
    private val analyticsTracker: AnalyticsTracker by inject()
    private val storage: Storage by inject()
    private val appcuesCoroutineScope: AppcuesCoroutineScope by inject()

    private var stateJob: Job? = null
    private var errorJob: Job? = null

    fun start(stateMachine: StateMachine, onEndedExperience: (Experience) -> Unit, dispatcher: CoroutineDispatcher = Dispatchers.IO) {
        // ensure any existing observers are stopped before starting new ones
        stop()

        stateJob = appcuesCoroutineScope.launch(dispatcher) { stateMachine.observeState(onEndedExperience) }
        errorJob = appcuesCoroutineScope.launch(dispatcher) { stateMachine.observeErrors() }
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

            if (state is EndingExperience) {
                onEndedExperience(state.experience)
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
            is RenderingStep -> {
                if (isFirst) {
                    // update this value for auto-properties
                    storage.lastContentShownAt = Date()
                    trackLifecycleEvent(ExperienceStarted(experience), SdkMetrics.trackRender(experience.requestId))
                }
                trackLifecycleEvent(StepSeen(experience, flatStepIndex))
            }
            is EndingStep -> {
                if (markComplete) {
                    trackLifecycleEvent(StepCompleted(experience, flatStepIndex))
                }
            }
            is EndingExperience -> {
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
            is Error.ExperienceError -> trackLifecycleEvent(ExperienceError(this))
            is Error.StepError -> trackLifecycleEvent(StepError(this))
            is ExperienceAlreadyActive -> Unit
            is RenderContextNotActive -> Unit
        }
    }

    private fun trackLifecycleEvent(event: ExperienceLifecycleEvent, additionalProperties: Map<String, Any> = emptyMap()) {
        val properties = event.properties.toMutableMap()
        properties.putAll(additionalProperties)
        analyticsTracker.track(event.name, properties, interactive = false, isInternal = true)
    }

    private fun State.isPublished(): Boolean =
        when (this) {
            is BeginningExperience -> experience.published
            is BeginningStep -> experience.published
            is EndingExperience -> experience.published
            is EndingStep -> experience.published
            is Idling -> false
            is RenderingStep -> experience.published
        }

    private fun Error.isPublished(): Boolean =
        when (this) {
            is Error.ExperienceError -> experience.published
            is Error.StepError -> experience.published
            is ExperienceAlreadyActive -> false
            is RenderContextNotActive -> false
        }
}
