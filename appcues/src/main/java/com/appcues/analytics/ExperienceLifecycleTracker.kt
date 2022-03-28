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
import com.appcues.statemachine.State.EndingExperience
import com.appcues.statemachine.State.EndingStep
import com.appcues.statemachine.State.RenderingStep
import com.appcues.statemachine.StateMachine
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

    suspend fun start() = withContext(Dispatchers.IO) {
        stateMachine.stateResultFlow.collect { result ->
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
                    }
                }
            }
        }
    }

    private fun trackLifecycleEvent(event: ExperienceLifecycleEvent) {
        analyticsTracker.track(event.name, event.properties, false)
    }
}
