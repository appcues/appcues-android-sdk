package com.appcues.ui

import com.appcues.AppcuesConfig
import com.appcues.SessionMonitor
import com.appcues.data.AppcuesRepository
import com.appcues.data.model.Experience
import com.appcues.data.model.ExperiencePriority.NORMAL
import com.appcues.statemachine.Action.EndExperience
import com.appcues.statemachine.Action.StartExperience
import com.appcues.statemachine.State.Idling
import com.appcues.statemachine.StateMachine
import com.appcues.util.ResultOf.Failure
import com.appcues.util.ResultOf.Success

internal class ExperienceRenderer(
    private val repository: AppcuesRepository,
    private val stateMachine: StateMachine,
    private val sessionMonitor: SessionMonitor,
    private val config: AppcuesConfig,
) {

    suspend fun show(experience: Experience): Boolean {
        val canShow = config.interceptor?.canDisplayExperience(experience.id) ?: true

        if (!canShow) return false

        // "event_trigger" or "forced" experience priority is NORMAL, "screen_view" is low -
        // if an experience is currently showing and the new experience coming in is normal priority
        // then it replaces whatever is currently showing - i.e. an "event_trigger" experience will
        // supersede a "screen_view" triggered experience - per Appcues standard behavior
        if (experience.priority == NORMAL && stateMachine.currentState != Idling) {
            return stateMachine.handleAction(EndExperience(false)).run {
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
}
