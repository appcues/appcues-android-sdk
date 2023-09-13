package com.appcues.analytics

import com.appcues.AppcuesCoroutineScope
import com.appcues.analytics.ExperienceLifecycleEvent.ExperienceCompleted
import com.appcues.analytics.ExperienceLifecycleEvent.ExperienceDismissed
import com.appcues.analytics.ExperienceLifecycleEvent.ExperienceError
import com.appcues.analytics.ExperienceLifecycleEvent.ExperienceStarted
import com.appcues.analytics.ExperienceLifecycleEvent.StepCompleted
import com.appcues.analytics.ExperienceLifecycleEvent.StepError
import com.appcues.analytics.ExperienceLifecycleEvent.StepSeen
import com.appcues.analytics.RenderingService.EventTracker
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

internal class ExperienceLifecycleTracker(
    private val coroutineScope: AppcuesCoroutineScope,
    private val eventTracker: EventTracker,
) {

    private var stateJob: Job? = null
    private var errorJob: Job? = null

    fun start(stateMachine: StateMachine, onEndedExperience: (Experience) -> Unit, dispatcher: CoroutineDispatcher = Dispatchers.IO) {
        // ensure any existing observers are stopped before starting new ones
        stop()

        stateJob = coroutineScope.launch(dispatcher) { stateMachine.observeState(onEndedExperience) }
        errorJob = coroutineScope.launch(dispatcher) { stateMachine.observeErrors() }
    }

    fun stop() {
        stateJob?.cancel()
        errorJob?.cancel()

        stateJob = null
        errorJob = null
    }

    private suspend fun StateMachine.observeState(onEndedExperience: (Experience) -> Unit) {
        stateFlow.collect {
            if (it.shouldTrack()) { // will not track for unpublished (preview) experiences
                when (it) {
                    is RenderingStep -> {
                        if (it.isFirst) {
                            trackLifecycleEvent(ExperienceStarted(it.experience), SdkMetrics.trackRender(it.experience.requestId))
                        }
                        trackLifecycleEvent(StepSeen(it.experience, it.flatStepIndex))
                    }
                    is EndingStep -> {
                        if (it.markComplete) {
                            trackLifecycleEvent(StepCompleted(it.experience, it.flatStepIndex))
                        }
                    }
                    is EndingExperience -> {
                        if (it.trackAnalytics) {
                            if (it.markComplete) {
                                // if ending on the last step OR an action requested it be considered complete explicitly,
                                // track the experience_completed event
                                trackLifecycleEvent(ExperienceCompleted(it.experience))
                            } else {
                                // otherwise its considered experience_dismissed (not completed)
                                trackLifecycleEvent(ExperienceDismissed(it.experience, it.flatStepIndex))
                            }
                        }

                        onEndedExperience(it.experience)
                    }
                    else -> Unit
                }
            }
        }
    }

    private suspend fun StateMachine.observeErrors() {
        errorFlow.collect {
            if (it.shouldTrack()) { // will not track for unpublished (preview) experiences
                when (it) {
                    is Error.ExperienceError -> trackLifecycleEvent(ExperienceError(it))
                    is Error.StepError -> trackLifecycleEvent(StepError(it))
                    is ExperienceAlreadyActive -> Unit
                    is RenderContextNotActive -> Unit
                }
            }
        }
    }

    private fun trackLifecycleEvent(event: ExperienceLifecycleEvent, additionalProperties: Map<String, Any> = emptyMap()) {
        val properties = event.properties.toMutableMap()
        properties.putAll(additionalProperties)

        eventTracker.trackEvent(event.name, properties, isInteractive = false, isInternal = true)
    }

    private fun State.shouldTrack(): Boolean =
        when (this) {
            is BeginningExperience -> experience.published
            is BeginningStep -> experience.published
            is EndingExperience -> experience.published
            is EndingStep -> experience.published
            is Idling -> false
            is RenderingStep -> experience.published
        }

    private fun Error.shouldTrack(): Boolean =
        when (this) {
            is Error.ExperienceError -> experience.published
            is Error.StepError -> experience.published
            is ExperienceAlreadyActive -> false
            is RenderContextNotActive -> false
        }
}
