package com.appcues.ui

import com.appcues.AppcuesFrameView
import com.appcues.data.model.RenderContext
import com.appcues.statemachine.StateMachine
import java.lang.ref.WeakReference

internal interface StateMachineOwning {
    val renderContext: RenderContext
    val stateMachine: StateMachine
    suspend fun reset()

    fun onScreenChange() { }

    fun isInvalid(): Boolean = false
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

    override fun isInvalid(): Boolean = frame.get() == null
}

internal class StateMachineDirectory {
    private var stateMachines = mutableMapOf<RenderContext, StateMachineOwning>()

    fun onScreenChange() {
        stateMachines.values.forEach { it.onScreenChange() }
        stateMachines.values.removeAll { it.isInvalid() }
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
