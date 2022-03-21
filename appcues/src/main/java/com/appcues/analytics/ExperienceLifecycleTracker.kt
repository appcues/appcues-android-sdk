package com.appcues.analytics

import com.appcues.analytics.ExperienceLifecycleEvent.ExperienceCompleted
import com.appcues.analytics.ExperienceLifecycleEvent.ExperienceDismissed
import com.appcues.analytics.ExperienceLifecycleEvent.ExperienceError
import com.appcues.analytics.ExperienceLifecycleEvent.ExperienceStarted
import com.appcues.analytics.ExperienceLifecycleEvent.StepCompleted
import com.appcues.analytics.ExperienceLifecycleEvent.StepError
import com.appcues.analytics.ExperienceLifecycleEvent.StepSeen
import com.appcues.statemachine.Error
import com.appcues.statemachine.State.EndingExperience
import com.appcues.statemachine.State.EndingStep
import com.appcues.statemachine.State.Idling
import com.appcues.statemachine.State.RenderingStep
import com.appcues.statemachine.StateMachine
import com.appcues.statemachine.StateResult.Failure
import com.appcues.statemachine.StateResult.Success
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
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
        stateMachine.stateResultFlow.collect { result ->
            when (result) {
                is Success -> with(result.state) {
                    when (this) {
                        is RenderingStep -> {
                            if (!startedExperience) {
                                trackLifecycleEvent(ExperienceStarted(experience))
                                startedExperience = true
                            }
                            trackLifecycleEvent(StepSeen(experience, step))
                        }
                        is EndingStep -> {
                            trackLifecycleEvent(StepCompleted(experience, step))
                            // todo - need a way to check if the step ended was the last step of the last step container
                            // and mark the `completedExperience = true` if so -- so that we can correctly fire
                            // the experience completed vs. dismissed events
                        }
                        is EndingExperience -> {
                            if (completedExperience) {
                                trackLifecycleEvent(ExperienceCompleted(experience))
                            } else {
                                trackLifecycleEvent(ExperienceDismissed(experience, step))
                            }
                        }
                        is Idling -> {
                            startedExperience = false
                            completedExperience = false
                        }
                        else -> Unit
                    }
                }
                is Failure -> with(result.error) {
                    when (this) {
                        is Error.ExperienceError -> trackLifecycleEvent(ExperienceError(this))
                        is Error.StepError -> trackLifecycleEvent(StepError(this))
                    }
                }
            }
        }
    }

    private fun trackLifecycleEvent(event: ExperienceLifecycleEvent) {
        analyticsTracker.track(event.name, event.properties, false)
    }
}