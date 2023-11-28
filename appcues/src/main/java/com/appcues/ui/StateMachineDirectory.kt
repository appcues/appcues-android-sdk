package com.appcues.ui

import com.appcues.AppcuesFrameView
import com.appcues.data.model.RenderContext
import com.appcues.data.model.RenderContext.Modal
import com.appcues.statemachine.Action.Reset
import com.appcues.statemachine.StateMachine
import com.appcues.statemachine.states.RenderingStepState
import java.lang.ref.WeakReference

internal interface StateMachineOwning {

    val renderContext: RenderContext
    val stateMachine: StateMachine
    suspend fun reset()

    suspend fun onConfigurationChanged() = Unit
}

internal class AppcuesFrameStateMachineOwner(
    frame: AppcuesFrameView,
    override val renderContext: RenderContext,
    override val stateMachine: StateMachine
) : StateMachineOwning {

    val frame: WeakReference<AppcuesFrameView> = WeakReference(frame)

    override suspend fun reset() {
        frame.get()?.reset()
        // do not need to dismiss here, as the frame UI is already removed for embed
        stateMachine.stop(false)
    }
}

internal class ModalStateMachineOwner(override val stateMachine: StateMachine) : StateMachineOwning {

    override val renderContext: RenderContext = Modal

    override suspend fun reset() {
        stateMachine.stop(true)
    }

    override suspend fun onConfigurationChanged() {
        if (stateMachine.state is RenderingStepState) {
            stateMachine.handleAction(Reset)
        }
    }
}

internal class StateMachineDirectory {

    private var stateMachines = mutableMapOf<RenderContext, StateMachineOwning>()

    fun cleanup() {
        stateMachines.values.removeAll {
            val frameOwner = (it as? AppcuesFrameStateMachineOwner)
            // clean any state machines for views that are no longer valid
            // (weak reference is null)
            if (frameOwner != null) {
                frameOwner.frame.get() == null
            } else {
                false
            }
        }
    }

    fun getOwner(context: RenderContext): StateMachineOwning? =
        stateMachines[context]

    fun getOwner(frame: AppcuesFrameView): StateMachineOwning? =
        stateMachines.values
            .filterIsInstance(AppcuesFrameStateMachineOwner::class.java)
            .firstOrNull {
                it.frame.get() == frame
            }

    fun setOwner(owner: StateMachineOwning) {
        stateMachines[owner.renderContext] = owner
    }

    suspend fun resetAll() {
        stateMachines.values.forEach {
            it.reset()
        }
    }
}
