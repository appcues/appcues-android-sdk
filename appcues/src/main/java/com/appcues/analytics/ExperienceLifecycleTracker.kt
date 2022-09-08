package com.appcues.analytics

import com.appcues.Storage
import com.appcues.analytics.ExperienceLifecycleEvent.ExperienceCompleted
import com.appcues.analytics.ExperienceLifecycleEvent.ExperienceDismissed
import com.appcues.analytics.ExperienceLifecycleEvent.ExperienceError
import com.appcues.analytics.ExperienceLifecycleEvent.ExperienceStarted
import com.appcues.analytics.ExperienceLifecycleEvent.StepCompleted
import com.appcues.analytics.ExperienceLifecycleEvent.StepError
import com.appcues.analytics.ExperienceLifecycleEvent.StepInteraction
import com.appcues.analytics.ExperienceLifecycleEvent.StepInteraction.InteractionType.FORM_SUBMITTED
import com.appcues.analytics.ExperienceLifecycleEvent.StepSeen
import com.appcues.statemachine.Error
import com.appcues.statemachine.State
import com.appcues.statemachine.State.BeginningExperience
import com.appcues.statemachine.State.BeginningStep
import com.appcues.statemachine.State.EndingExperience
import com.appcues.statemachine.State.EndingStep
import com.appcues.statemachine.State.Idling
import com.appcues.statemachine.State.Paused
import com.appcues.statemachine.State.RenderingStep
import com.appcues.statemachine.StateMachine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
        launch { observeState() }
        launch { observeErrors() }
    }

    private suspend fun observeState() {
        stateMachine.stateFlow.collect {
            if (it.shouldTrack()) { // will not track for unpublished (preview) experiences
                when (it) {
                    is RenderingStep -> {
                        if (it.isFirst) {
                            // update this value for auto-properties
                            storage.lastContentShownAt = Date()
                            trackLifecycleEvent(ExperienceStarted(it.experience))
                        }
                        trackLifecycleEvent(StepSeen(it.experience, it.flatStepIndex))
                    }
                    is EndingStep -> {
                        trackLifecycleEvent(StepCompleted(it.experience, it.flatStepIndex))

                        // TESTING!
                        // this is a spot where we can output the interaction analytics to validate structure, but still TBD
                        // on the rules around when these should be submitted related to actions in the UI.  Also, may need
                        // a way to plug in user profile attributes with the form answers, as web does.
                        val formState = it.experience.flatSteps[it.flatStepIndex].formState
                        if (formState.formItems.any() && formState.isFormComplete.value) {
                            trackLifecycleEvent(StepInteraction(it.experience, it.flatStepIndex, FORM_SUBMITTED))
                        }
                    }
                    is EndingExperience -> {
                        if (it.isExperienceCompleted()) {
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

    private suspend fun observeErrors() {
        stateMachine.errorFlow.collect {
            if (it.shouldTrack()) { // will not track for unpublished (preview) experiences
                when (it) {
                    is Error.ExperienceError -> trackLifecycleEvent(ExperienceError(it))
                    is Error.StepError -> trackLifecycleEvent(StepError(it))
                    is Error.ExperienceAlreadyActive -> Unit
                }
            }
        }
    }

    private fun trackLifecycleEvent(event: ExperienceLifecycleEvent) {
        analyticsTracker.track(event.name, event.properties, interactive = false, isInternal = true)
    }

    private fun State.shouldTrack(): Boolean =
        when (this) {
            is BeginningExperience -> experience.published
            is BeginningStep -> experience.published
            is EndingExperience -> experience.published
            is EndingStep -> experience.published
            is Idling -> false
            is RenderingStep -> experience.published
            is Paused -> false
        }

    private fun Error.shouldTrack(): Boolean =
        when (this) {
            is Error.ExperienceError -> experience.published
            is Error.StepError -> experience.published
            is Error.ExperienceAlreadyActive -> false
        }
}
