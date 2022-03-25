package com.appcues.action.appcues

import com.appcues.Appcues
import com.appcues.action.ExperienceAction
import com.appcues.data.model.AppcuesConfigMap
import com.appcues.data.model.getConfig
import com.appcues.data.model.getConfigInt
import com.appcues.statemachine.Action.StartStep
import com.appcues.statemachine.StateMachine
import com.appcues.statemachine.StepReference
import java.util.UUID

internal class ContinueAction(
    override val config: AppcuesConfigMap,
    private val stateMachine: StateMachine
) : ExperienceAction {

    companion object {
        const val NAME = "@appcues/continue"
    }

    private val index = config.getConfigInt("index")

    private val offset = config.getConfigInt("offset")

    private val id = config.getConfig<String>("stepID")

    override suspend fun execute(appcues: Appcues) {
        when {
            index != null -> StepReference.StepIndex(index)
            offset != null -> StepReference.StepOffset(offset)
            id != null -> StepReference.StepId(UUID.fromString(id))
            else -> null
        }?.let {
            stateMachine.handleAction(StartStep(it))
        }
    }
}
