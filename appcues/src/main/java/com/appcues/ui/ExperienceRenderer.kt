package com.appcues.ui

import com.appcues.AppcuesCoroutineScope
import com.appcues.data.AppcuesRepository
import com.appcues.data.model.Experience
import com.appcues.statemachine.Action.StartExperience
import com.appcues.statemachine.StateMachine
import kotlinx.coroutines.launch

internal class ExperienceRenderer(
    private val appcuesCoroutineScope: AppcuesCoroutineScope,
    private val repository: AppcuesRepository,
    private val stateMachine: StateMachine,
) {

    fun show(experience: Experience) {
        stateMachine.handleAction(StartExperience(experience))
    }

    fun show(contentId: String) {
        appcuesCoroutineScope.launch {
            // should this check if the state is Idling before even trying to fetch
            // the experience? since it cannot show anyway, if already in another state?
            repository.getExperienceContent(contentId).also {
                show(it)
            }
        }
    }
}
