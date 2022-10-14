package com.appcues.action.appcues

import com.appcues.Appcues
import com.appcues.action.ExperienceAction
import com.appcues.action.MetadataSettingsAction
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
) : ExperienceAction, MetadataSettingsAction {

    companion object {

        const val TYPE = "@appcues/continue"
    }

    private val index = config.getConfigInt("index")

    private val offset = config.getConfigInt("offset") ?: 1

    private val id = config.getConfig<String>("stepID")

    private val stepReference: StepReference
        get() = when {
            index != null -> StepReference.StepIndex(index)
            id != null -> StepReference.StepId(UUID.fromString(id))
            else -> StepReference.StepOffset(offset)
        }
    override val category: String = "internal"

    override val destination: String = stepReference.destination

    override suspend fun execute(appcues: Appcues) {
        stateMachine.handleAction(StartStep(stepReference))
    }
}
