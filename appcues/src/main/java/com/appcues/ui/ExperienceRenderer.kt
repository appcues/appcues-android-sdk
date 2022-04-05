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
