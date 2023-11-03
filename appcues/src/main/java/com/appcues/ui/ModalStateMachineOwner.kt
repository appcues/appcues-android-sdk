package com.appcues.ui

import android.view.MotionEvent
import android.view.ViewTreeObserver
import android.view.ViewTreeObserver.OnDrawListener
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.ViewTreeObserver.OnScrollChangedListener
import com.appcues.AppcuesCoroutineScope
import com.appcues.data.model.RenderContext
import com.appcues.data.model.RenderContext.Modal
import com.appcues.monitor.AppcuesActivityMonitor
import com.appcues.statemachine.Action.Reset
import com.appcues.statemachine.Action.Retry
import com.appcues.statemachine.Error.StepError
import com.appcues.statemachine.StateMachine
import com.appcues.statemachine.states.RenderingStepState
import com.appcues.ui.utils.getParentView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Timer
import java.util.TimerTask
import kotlin.concurrent.schedule

internal class ModalStateMachineOwner(
    override val stateMachine: StateMachine,
    private val coroutineScope: AppcuesCoroutineScope,
) : StateMachineOwning {

    companion object {
        private const val SCROLL_DEBOUNCE_MILLISECONDS = 1_000L
    }

    override val renderContext: RenderContext = Modal

    // used to attempt retry when items in view change
    private var viewTreeUpdateHandler: ViewTreeUpdateHandler = ViewTreeUpdateHandler { attemptRetry() }
    private var uiIdleDebounceTimer: TimerTask? = null
    private var viewTreeObserver: ViewTreeObserver? = null

    init {
        // set up observer on the Modal state machine errors to initiate retry
        coroutineScope.launch(Dispatchers.IO) {
            stateMachine.errorFlow.collect { error ->
                // if a recoverable step error is observed, begin attempting retry
                if (error is StepError && error.recoverable) {
                    startRetryHandling()
                }
            }
        }
    }

    override suspend fun reset() {
        stateMachine.stop(true)
    }

    override suspend fun onConfigurationChanged() {
        if (stateMachine.state is RenderingStepState) {
            stateMachine.handleAction(Reset)
        }
    }

    fun stopRetryHandling() {
        viewTreeUpdateHandler.detach()
        viewTreeObserver = null
    }

    private fun startRetryHandling() {
        if (viewTreeObserver == null) {
            AppcuesActivityMonitor.activity?.getParentView()?.viewTreeObserver?.let {
                viewTreeUpdateHandler.attach(it)
                this.viewTreeObserver = it
            }
        }
    }

    private fun attemptRetry() {
        onUiIdle {
            // stop listening for updates, will re-attach if another error occurs
            stopRetryHandling()

            // cancel any current touch (drag) as our overlay is about to drop on top and we don't want things moving behind it
            AppcuesActivityMonitor.activity?.getParentView()?.dispatchTouchEvent(
                MotionEvent.obtain(0L, 0L, MotionEvent.ACTION_CANCEL, 0f, 0f, 0)
            )

            // trigger the retry in the state machine
            coroutineScope.launch {
                stateMachine.handleAction(Retry)
            }
        }
    }

    private fun onUiIdle(block: () -> Unit) {
        // cancel any previous
        uiIdleDebounceTimer?.cancel()

        uiIdleDebounceTimer = Timer().schedule(delay = SCROLL_DEBOUNCE_MILLISECONDS) {
            // set to know to allow new Timer
            uiIdleDebounceTimer = null
            // run callback block
            block()
        }
    }

    private class ViewTreeUpdateHandler(
        private var action: () -> Unit
    ) : OnScrollChangedListener, OnGlobalLayoutListener, OnDrawListener {

        private var viewTreeObserver: ViewTreeObserver? = null

        fun attach(viewTreeObserver: ViewTreeObserver) {
            this.viewTreeObserver = viewTreeObserver

            viewTreeObserver.addOnScrollChangedListener(this)
            viewTreeObserver.addOnGlobalLayoutListener(this)
            viewTreeObserver.addOnDrawListener(this)
        }

        fun detach() {
            viewTreeObserver?.let {
                it.removeOnScrollChangedListener(this)
                it.removeOnGlobalLayoutListener(this)
                it.removeOnDrawListener(this)
            }
            viewTreeObserver = null
        }

        override fun onScrollChanged() {
            action()
        }

        override fun onGlobalLayout() {
            action()
        }

        override fun onDraw() {
            action()
        }
    }
}
