package com.appcues.ui

import com.appcues.AppcuesFrameView
import com.appcues.data.model.RenderContext
import com.appcues.statemachine.StateMachine
import java.lang.ref.WeakReference

internal interface StateMachineOwning {
    var renderContext: RenderContext?
    var stateMachine: StateMachine?
    fun reset()
}

internal class AppcuesFrameStateMachineOwner(frame: AppcuesFrameView) : StateMachineOwning {
    val frame: WeakReference<AppcuesFrameView> = WeakReference(frame)
    override var renderContext: RenderContext? = null
    override var stateMachine: StateMachine? = null
    override fun reset() {
        frame.get()?.reset()
        // do not need to dismiss here, as the frame UI is already removed for embed
        stateMachine?.stop(false)
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

    fun setOwner(context: RenderContext, owner: StateMachineOwning) {
        // enforce uniqueness (a StateMachineOwning may only be registered for a single RenderContext)
        val oldRenderContext = owner.renderContext
        if (oldRenderContext != null && oldRenderContext != context) {
            stateMachines.remove(oldRenderContext)
        }

        stateMachines[context] = owner

        // Save the current renderContext so it can be easily removed in the above uniqueness check.
        owner.renderContext = context
    }

    fun resetAll() {
        stateMachines.values.forEach {
            it.reset()
        }
    }
}
