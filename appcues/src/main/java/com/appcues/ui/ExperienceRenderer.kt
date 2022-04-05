package com.appcues.ui

import com.appcues.AppcuesConfig
import com.appcues.SessionMonitor
import com.appcues.data.AppcuesRepository
import com.appcues.data.model.Experience
import com.appcues.statemachine.Action.StartExperience
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
