package com.appcues.analytics

import com.appcues.AppcuesConfig
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
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import java.util.Date

internal class ExperienceLifecycleTracker(
    override val scope: Scope,
) : KoinScopeComponent {

    // Circular dependency AnalyticsTracker -> Processor -> ExperienceRenderer -> this
    private val analyticsTracker: AnalyticsTracker by inject()
    private val storage: Storage by inject()
    private val config: AppcuesConfig by inject()

    fun onState(state: State): Unit = with(state) {
        if (!shouldTrackUnpublished()) return

        when (this) {
            is BeginningExperience -> config.experienceListener?.experienceStarted(experience.id)
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
                config.experienceListener?.experienceFinished(experience.id)

                if (markComplete) {
                    // if ending on the last step OR an action requested it be considered complete explicitly,
                    // track the experience_completed event
                    trackLifecycleEvent(ExperienceCompleted(experience))
                } else {
                    // otherwise its considered experience_dismissed (not completed)
                    trackLifecycleEvent(ExperienceDismissed(experience, flatStepIndex))
                }
            }
            else -> Unit
        }
    }

    fun onError(error: Error): Unit = with(error) {
        if (!shouldTrackUnpublished()) return

        when (this) {
            is Error.ExperienceError -> trackLifecycleEvent(ExperienceError(this))
            is Error.StepError -> trackLifecycleEvent(StepError(this))
            is Error.ExperienceAlreadyActive -> Unit
            is Error.NoActiveStateMachine -> Unit
        }
    }

    private fun trackLifecycleEvent(event: ExperienceLifecycleEvent, additionalProperties: Map<String, Any> = emptyMap()) {
        val properties = event.properties.toMutableMap()
        properties.putAll(additionalProperties)
        analyticsTracker.track(event.name, properties, interactive = false, isInternal = true)
    }

    private fun State.shouldTrackUnpublished(): Boolean =
        when (this) {
            is BeginningExperience -> experience.published
            is BeginningStep -> experience.published
            is EndingExperience -> experience.published
            is EndingStep -> experience.published
            is Idling -> false
            is RenderingStep -> experience.published
        }

    private fun Error.shouldTrackUnpublished(): Boolean =
        when (this) {
            is Error.ExperienceError -> experience.published
            is Error.StepError -> experience.published
            is Error.ExperienceAlreadyActive -> false
            is Error.NoActiveStateMachine -> false
        }
}
