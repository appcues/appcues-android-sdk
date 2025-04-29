package com.appcues.ui

import com.appcues.AppcuesFrameView
import com.appcues.data.model.RenderContext
import com.appcues.data.model.RenderContext.Embed
import com.appcues.statemachine.StateMachine
import org.jetbrains.annotations.VisibleForTesting
import java.lang.ref.WeakReference
import kotlin.collections.set

internal interface StateMachineOwning {

    val renderContext: RenderContext
    val stateMachine: StateMachine
    suspend fun reset()

    suspend fun onConfigurationChanged() = Unit

    fun isInvalid(): Boolean = false
}

internal class AppcuesFrameStateMachineOwner(
    frame: AppcuesFrameView,
    override val renderContext: Embed,
    override val stateMachine: StateMachine
) : StateMachineOwning {

    private val _frame: WeakReference<AppcuesFrameView> = WeakReference(frame)
    val frame: AppcuesFrameView?
        get() = _frame.get()

    // Since we rely on WeakReference to call frame, this method allows us to test
    // the scenario where the WeakReference was cleared (System.gc())
    @VisibleForTesting
    fun simulateGarbageCollector() {
        _frame.clear()
    }

    // this is called on scenarios like cell re-use in a recycler, when a frame view
    // gets recycled into a different frame ID, or in any scenario where a frame ID is
    // set to a different frame - this frame is no longer valid
    override suspend fun reset() {
        frame?.reset()
        // do not need to dismiss here, as the frame UI is already removed for embed
        stateMachine.stop(false)
    }

    override fun isInvalid(): Boolean = frame == null
}

internal class StateMachineDirectory {

    private var stateMachines = mutableMapOf<RenderContext, StateMachineOwning>()

    fun onScreenChange() {
        stateMachines.values.removeAll { it.isInvalid() }
    }

    fun getOwner(context: RenderContext): StateMachineOwning? = stateMachines[context]

    fun getOwner(frame: AppcuesFrameView): StateMachineOwning? =
        stateMachines.values
            .filterIsInstance(AppcuesFrameStateMachineOwner::class.java)
            .firstOrNull { it.frame == frame }

    fun setOwner(owner: StateMachineOwning) {
        stateMachines[owner.renderContext] = owner
    }

    fun remove(context: RenderContext) {
        stateMachines.remove(context)
    }

    suspend fun resetAll() {
        stateMachines.values.forEach { it.reset() }
    }
}
