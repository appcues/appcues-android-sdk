package com.appcues.analytics

import com.appcues.analytics.AnalyticEvents.ExperienceCompleted
import com.appcues.analytics.AnalyticEvents.ExperienceDismissed
import com.appcues.analytics.AnalyticEvents.ExperienceError
import com.appcues.analytics.AnalyticEvents.ExperienceStarted
import com.appcues.analytics.AnalyticEvents.StepCompleted
import com.appcues.analytics.AnalyticEvents.StepSeen
import com.appcues.statemachine.Error
import com.appcues.statemachine.State
import com.appcues.statemachine.StateMachine
import com.appcues.statemachine.states.EndingExperience
import com.appcues.statemachine.states.EndingStep
import com.appcues.statemachine.states.Idling
import com.appcues.statemachine.states.RenderingStep
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope

internal class ExperienceLifecycleTracker(
    override val scope: Scope
) : KoinScopeComponent {

    // lazy property injection to avoid circular DI reference in constructor
    // AnalyticsTracker -> this <- Analytics Tracker
    private val analyticsTracker: AnalyticsTracker by inject()
    private val stateMachine: StateMachine by inject()

    // note: this is operating under the assumption that we have a single state machine and
    // a single experience rendering at a time.  if/when that evolves, this will need
    // to be adjusted accordingly.
    private var startedExperience = false
    private var completedExperience = false

    suspend fun start() = withContext(Dispatchers.IO) {
        launch { monitorState(stateMachine.stateFlow) }
        launch { monitorErrors(stateMachine.errorFlow) }
    }

    private suspend fun monitorState(flow: SharedFlow<State>) =
        flow.collect {
            when (it) {
                is RenderingStep -> {
                    if (!startedExperience) {
                        analyticsTracker.track(ExperienceStarted)
                        startedExperience = true
                    }
                    analyticsTracker.track(StepSeen)
                }
                is EndingStep -> {
                    analyticsTracker.track(StepCompleted)
                    // todo - need a way to check if the step ended was the last step of the last step container
                    // and mark the `completedExperience = true` if so -- so that we can correctly fire
                    // the experience completed vs. dismissed events
                }
                is EndingExperience -> {
                    if (completedExperience) {
                        analyticsTracker.track(ExperienceCompleted)
                    } else {
                        analyticsTracker.track(ExperienceDismissed)
                    }
                }
                is Idling -> {
                    startedExperience = false
                    completedExperience = false
                }
            }
        }

    private suspend fun monitorErrors(flow: SharedFlow<Error>) =
        flow.collect {
            analyticsTracker.track(ExperienceError)
        }
}
