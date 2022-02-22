package com.appcues.action.appcues

import com.appcues.Appcues
import com.appcues.action.ActionConfigMap
import com.appcues.action.ExperienceAction
import com.appcues.statemachine.Action.EndExperience
import com.appcues.statemachine.StateMachine

internal class AppcuesCloseAction(
    override val config: ActionConfigMap,
    private val stateMachine: StateMachine
) : ExperienceAction {

    override suspend fun execute(appcues: Appcues) {
        stateMachine.handleAction(EndExperience())
    }
}
