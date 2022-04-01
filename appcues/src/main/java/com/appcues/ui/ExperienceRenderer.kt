package com.appcues.ui

import com.appcues.AppcuesConfig
import com.appcues.AppcuesCoroutineScope
import com.appcues.SessionMonitor
import com.appcues.data.AppcuesRepository
import com.appcues.data.model.Experience
import com.appcues.statemachine.Action.StartExperience
import com.appcues.statemachine.StateMachine
import kotlinx.coroutines.launch

internal class ExperienceRenderer(
    private val appcuesCoroutineScope: AppcuesCoroutineScope,
    private val repository: AppcuesRepository,
    private val stateMachine: StateMachine,
    private val sessionMonitor: SessionMonitor,
    private val config: AppcuesConfig,
) {

    suspend fun show(experience: Experience) {
        val canShow = config.interceptor?.canDisplayExperience(experience.id) ?: true
        if (canShow) {
            stateMachine.handleAction(StartExperience(experience))
        }
    }

    fun show(experienceId: String) {
        if (!sessionMonitor.checkSession("cannot show Experience $experienceId")) return

        appcuesCoroutineScope.launch {
            // should this check if the state is Idling before even trying to fetch
            // the experience? since it cannot show anyway, if already in another state?
            repository.getExperienceContent(experienceId)?.let {
                show(it)
            }
        }
    }

    fun preview(experienceId: String) {
        appcuesCoroutineScope.launch {
            // should this check if the state is Idling before even trying to fetch
            // the experience? since it cannot show anyway, if already in another state?
            repository.getExperiencePreview(experienceId)?.let {
                show(it)
            }
        }
    }
}
