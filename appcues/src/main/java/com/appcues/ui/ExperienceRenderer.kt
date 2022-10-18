package com.appcues.ui

import com.appcues.AppcuesConfig
import com.appcues.SessionMonitor
import com.appcues.analytics.AnalyticsEvent
import com.appcues.analytics.AnalyticsTracker
import com.appcues.data.AppcuesRepository
import com.appcues.data.model.Experience
import com.appcues.data.model.ExperiencePriority.NORMAL
import com.appcues.statemachine.Action.EndExperience
import com.appcues.statemachine.Action.StartExperience
import com.appcues.statemachine.Error
import com.appcues.statemachine.State
import com.appcues.statemachine.State.Idling
import com.appcues.statemachine.State.Paused
import com.appcues.statemachine.StateMachine
import com.appcues.util.ResultOf
import com.appcues.util.ResultOf.Failure
import com.appcues.util.ResultOf.Success
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope

internal class ExperienceRenderer(
    override val scope: Scope,
) : KoinScopeComponent {

    // lazy prop inject here to avoid circular dependency with AnalyticsTracker
    // AnalyticsTracker > AnalyticsQueueProcessor > ExperienceRenderer(this) > AnalyticsTracker
    private val repository by inject<AppcuesRepository>()
    private val stateMachine by inject<StateMachine>()
    private val sessionMonitor by inject<SessionMonitor>()
    private val config by inject<AppcuesConfig>()
    private val analyticsTracker by inject<AnalyticsTracker>()

    suspend fun show(experience: Experience): Boolean {
        var canShow = config.interceptor?.canDisplayExperience(experience.id) ?: true

        if (canShow && experience.experiment != null) {
            // send analytics
            analyticsTracker.track(
                event = AnalyticsEvent.ExperimentEntered,
                properties = mapOf(
                    "experimentId" to experience.experiment.id,
                    "group" to experience.experiment.group
                ),
                interactive = false
            )

            // if this user is in the control group, it should not show
            canShow = experience.experiment.group != "control"
        }

        if (!canShow) return false

        // "event_trigger" or "forced" experience priority is NORMAL, "screen_view" is low -
        // if an experience is currently showing and the new experience coming in is normal priority
        // then it replaces whatever is currently showing - i.e. an "event_trigger" experience will
        // supersede a "screen_view" triggered experience - per Appcues standard behavior
        val priorityOverride = experience.priority == NORMAL && stateMachine.state != Idling
        // additionally - if there is a current Experience running in the Paused state - this means
        // that the AppcuesActivity has been covered up by another Activity in the foreground with priority,
        // and whatever is now requesting to display on top should take precedence since the host application
        // has opened another activity on top of a previous Experience that is no longer visible.
        val isPaused = stateMachine.state is Paused
        if (priorityOverride || isPaused) {
            return dismissCurrentExperience(markComplete = false, destroyed = false).run {
                when (this) {
                    is Success -> show(experience) // re-invoke show on the new experience now after dismiss
                    is Failure -> false // dismiss failed - can't continue
                }
            }
        }

        return stateMachine.handleAction(StartExperience(experience)).run {
            when (this) {
                is Success -> true
                is Failure -> false
            }
        }
    }

    suspend fun show(qualifiedExperiences: List<Experience>): Boolean {
        if (qualifiedExperiences.isEmpty()) {
            // If given an empty list of qualified experiences, complete with a success because this function has completed without error.
            // This function only recurses on a non-empty case, so this block only applies to the initial external call.
            return true
        }

        val success = show(qualifiedExperiences.first())
        if (!success) {
            val remainingExperiences = qualifiedExperiences.drop(1)
            if (remainingExperiences.isNotEmpty()) {
                // fallback logic - try the next remaining experience, if available
                return show(remainingExperiences)
            }
        }
        return success
    }

    suspend fun show(experienceId: String): Boolean {
        if (!sessionMonitor.checkSession("cannot show Experience $experienceId")) return false

        repository.getExperienceContent(experienceId)?.let {
            return show(it)
        }

        return false
    }

    suspend fun preview(experienceId: String): Boolean {
        repository.getExperiencePreview(experienceId)?.let {
            return show(it)
        }

        return false
    }

    fun stop() {
        stateMachine.stop()
    }

    suspend fun dismissCurrentExperience(markComplete: Boolean, destroyed: Boolean): ResultOf<State, Error> =
        stateMachine.handleAction(EndExperience(markComplete || stateMachine.state.isOnLastStep, destroyed))
}
