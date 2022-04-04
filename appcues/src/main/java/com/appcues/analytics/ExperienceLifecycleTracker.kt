package com.appcues.analytics

import com.appcues.Storage
import com.appcues.analytics.ExperienceLifecycleEvent.ExperienceCompleted
import com.appcues.analytics.ExperienceLifecycleEvent.ExperienceDismissed
import com.appcues.analytics.ExperienceLifecycleEvent.ExperienceError
import com.appcues.analytics.ExperienceLifecycleEvent.ExperienceStarted
import com.appcues.analytics.ExperienceLifecycleEvent.StepCompleted
import com.appcues.analytics.ExperienceLifecycleEvent.StepError
import com.appcues.analytics.ExperienceLifecycleEvent.StepSeen
import com.appcues.statemachine.Error
import com.appcues.statemachine.State
import com.appcues.statemachine.State.BeginningExperience
import com.appcues.statemachine.State.BeginningStep
import com.appcues.statemachine.State.EndingExperience
import com.appcues.statemachine.State.EndingStep
import com.appcues.statemachine.State.Idling
import com.appcues.statemachine.State.RenderingStep
import com.appcues.statemachine.StateMachine
import com.appcues.util.ResultOf
import com.appcues.util.ResultOf.Failure
import com.appcues.util.ResultOf.Success
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import java.util.Date

internal class ExperienceLifecycleTracker(
    override val scope: Scope
) : KoinScopeComponent {

    // lazy property injection to avoid circular DI reference in constructor
    // AnalyticsTracker -> this <- Analytics Tracker
    private val analyticsTracker: AnalyticsTracker by inject()
    private val stateMachine: StateMachine by inject()
    private val storage: Storage by inject()

    suspend fun start(): Unit = withContext(Dispatchers.IO) {
        stateMachine.stateResultFlow.collect { result ->
            if (result.shouldTrack()) { // will not track for unpublished (preview) experiences
                when (result) {
                    is Success -> with(result.value) {
                        when (this) {
                            is RenderingStep -> {
                                if (isFirst) {
                                    // update this value for auto-properties
                                    storage.lastContentShownAt = Date()
                                    trackLifecycleEvent(ExperienceStarted(experience))
                                }
                                trackLifecycleEvent(StepSeen(experience, flatStepIndex))
                            }
                            is EndingStep -> {
                                trackLifecycleEvent(StepCompleted(experience, flatStepIndex))
                            }
                            is EndingExperience -> {
                                if (flatStepIndex == experience.flatSteps.count() - 1) {
                                    // if ending on the last step - it was completed
                                    trackLifecycleEvent(ExperienceCompleted(experience))
                                } else {
                                    // otherwise its considered dismissed (not completed)
                                    trackLifecycleEvent(ExperienceDismissed(experience, flatStepIndex))
                                }
                            }
                            else -> Unit
                        }
                    }
                    is Failure -> with(result.reason) {
                        when (this) {
                            is Error.ExperienceError -> trackLifecycleEvent(ExperienceError(this))
                            is Error.StepError -> trackLifecycleEvent(StepError(this))
                            is Error.ExperienceAlreadyActive -> Unit
                        }
                    }
                }
            }
        }
    }

    private fun trackLifecycleEvent(event: ExperienceLifecycleEvent) {
        analyticsTracker.track(event.name, event.properties, false)
    }

    private fun ResultOf<State, Error>.shouldTrack(): Boolean =
        when (this) {
            is Success -> {
                when (this.value) {
                    is BeginningExperience -> value.experience.published
                    is BeginningStep -> value.experience.published
                    is EndingExperience -> value.experience.published
                    is EndingStep -> value.experience.published
                    is Idling -> false
                    is RenderingStep -> value.experience.published
                }
            }
            is Failure -> {
                when (reason) {
                    is Error.ExperienceError -> reason.experience.published
                    is Error.StepError -> reason.experience.published
                    is Error.ExperienceAlreadyActive -> false
                }
            }
        }
}
