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

    fun start(stateMachine: StateMachine, dispatcher: CoroutineDispatcher = Dispatchers.IO) {
        appcuesCoroutineScope.launch(dispatcher) { stateMachine.observeState() }
        appcuesCoroutineScope.launch(dispatcher) { stateMachine.observeErrors() }
    }

    private suspend fun StateMachine.observeState() {
        stateFlow.collect {
            if (it.shouldTrack()) { // will not track for unpublished (preview) experiences
                when (it) {
                    is RenderingStep -> {
                        if (it.isFirst) {
                            // update this value for auto-properties
                            storage.lastContentShownAt = Date()
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
                        if (it.markComplete) {
                            // if ending on the last step OR an action requested it be considered complete explicitly,
                            // track the experience_completed event
                            trackLifecycleEvent(ExperienceCompleted(it.experience))
                        } else {
                            // otherwise its considered experience_dismissed (not completed)
                            trackLifecycleEvent(ExperienceDismissed(it.experience, it.flatStepIndex))
                        }
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
        analyticsTracker.track(event.name, properties, interactive = false, isInternal = true)
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
