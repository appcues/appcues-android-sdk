package com.appcues.ui

import com.appcues.data.AppcuesRepository
import com.appcues.data.model.Experience
import com.appcues.statemachine.Action.StartExperience
import com.appcues.statemachine.StateMachine

internal class ExperienceRenderer(
    private val repository: AppcuesRepository,
    private val stateMachine: StateMachine
) {
    suspend fun show(experience: Experience) {
        stateMachine.handleAction(StartExperience(experience))
    }

    suspend fun show(contentId: String) {
        // should this check if the state is Idling before even trying to fetch
        // the experience? since it cannot show anyway, if already in another state?
        repository.getContent(contentId).also {
            show(it)
        }
    }
}
