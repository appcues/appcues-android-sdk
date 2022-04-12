package com.appcues.action.appcues

import com.appcues.Appcues
import com.appcues.action.ExperienceAction
import com.appcues.data.model.AppcuesConfigMap
import com.appcues.data.model.getConfigOrDefault
import com.appcues.statemachine.Action.EndExperience
import com.appcues.statemachine.StateMachine

internal class CloseAction(
    override val config: AppcuesConfigMap,
    private val stateMachine: StateMachine
) : ExperienceAction {

    companion object {
        const val TYPE = "@appcues/close"
    }

    private val markComplete = config.getConfigOrDefault("markComplete", false)

    override suspend fun execute(appcues: Appcues) {
        stateMachine.handleAction(EndExperience(false, markComplete))
    }
}
