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
        const val TYPE = "@appcues/continue"
    }

    private val index = config.getConfigInt("index")

    private val offset = config.getConfigInt("offset") ?: 1

    private val id = config.getConfig<String>("stepID")

    override suspend fun execute(appcues: Appcues) {
        val stepRef = when {
            index != null -> StepReference.StepIndex(index)
            id != null -> StepReference.StepId(UUID.fromString(id))
            else -> StepReference.StepOffset(offset)
        }

        // NOTE: there is a bug in kotlin that required this function to be revised slightly when upgrading from kotlin 1.6.0 to 1.6.21 to make the
        // suspend call to the state machine NOT be a tail call inside of a let, chained off the when statement.
        //
        // I don't understand the inner details, but hopefully fixed in 1.7.0 upcoming.  Without this change, an exception is thrown like
        // "ClassCastException: class CoroutineSingletons cannot be cast to class..."
        // Capturing this here in case any other similar issue pops up in the future.
        //
        // https://youtrack.jetbrains.com/issue/KT-51818/ClassCastException-class-CoroutineSingletons-cannot-be-cast-to-c#focus=Comments-27-6035324.0-0
        stateMachine.handleAction(StartStep(stepRef))
    }
}
