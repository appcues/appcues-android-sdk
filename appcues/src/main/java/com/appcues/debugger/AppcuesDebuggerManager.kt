package com.appcues.debugger

import android.app.ActionBar.LayoutParams
import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.findViewTreeOnBackPressedDispatcherOwner
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.viewModelScope
import com.appcues.AppcuesConfig
import com.appcues.R
import com.appcues.debugger.DebuggerViewModel.UIState.Expanded
import com.appcues.debugger.ui.DebuggerComposition
import com.appcues.debugger.ui.theme.AppcuesTheme
import com.appcues.di.scope.AppcuesScope
import com.appcues.ui.utils.getParentView
import com.appcues.util.AppcuesViewTreeOwner
import com.appcues.util.ContextWrapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference
import kotlin.coroutines.CoroutineContext

@Suppress("TooManyFunctions")
internal class AppcuesDebuggerManager(
    private val appcuesViewTreeOwner: AppcuesViewTreeOwner,
    private val contextWrapper: ContextWrapper,
    private val scope: AppcuesScope,
    private val appcuesConfig: AppcuesConfig,
) : Application.ActivityLifecycleCallbacks {

    private val coroutineScope: CoroutineScope = object : CoroutineScope {
        override val coroutineContext: CoroutineContext = SupervisorJob() + Dispatchers.Main
    }

    private var debuggerViewModel: DebuggerViewModel? = null
    private var debuggerParent: WeakReference<ViewGroup> = WeakReference(null)
    private var windowMonitorJob: Job? = null

    private lateinit var currentActivity: Activity

    fun start(activity: Activity, mode: DebugMode, deeplink: String? = null) = activity.runOnUiThread {
        currentActivity = activity
        coroutineScope.coroutineContext.cancelChildren()

        // it is possible to re-enter start without a stop (deepLinks) - in which case we continue to
        // use the VM we already have - else, make new one here
        val viewModel = debuggerViewModel?.let {
            if (it.uiState.value.mode == mode) {
                it
            } else {
                removeDebuggerView()
                null
            }
        } ?: DebuggerViewModel(scope, mode)

        debuggerViewModel = viewModel // and save reference
        viewModel.onStart(mode, deeplink)

        coroutineScope.launch {
            viewModel.uiState.collect { state -> onBackPressCallback.isEnabled = state is Expanded }
        }
        addDebuggerView(viewModel)
        startWindowMonitor()
        contextWrapper.getApplication().registerActivityLifecycleCallbacks(this)
    }

    fun stop() {
        windowMonitorJob?.cancel()
        windowMonitorJob = null
        coroutineScope.coroutineContext.cancelChildren()
        removeDebuggerView()
        debuggerViewModel?.viewModelScope?.cancel() // stop the VM from listening to app activity
        contextWrapper.getApplication().unregisterActivityLifecycleCallbacks(this)
        onBackPressCallback.remove()
        debuggerViewModel = null // remove the reference to the current VM - new one will be made on next start()
    }

    fun reset() {
        debuggerViewModel?.reset()
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
    override fun onActivityStarted(activity: Activity) = Unit
    override fun onActivityResumed(activity: Activity) {
        currentActivity = activity
        // setting this on postDelayed ensures we wait for
        // any transition from one activity to the another before we actually try to add
        // debugger view
        debuggerViewModel?.let {
            Handler(Looper.getMainLooper()).postDelayed({ addDebuggerView(it) }, 0)
        }
    }

    override fun onActivityPaused(activity: Activity) = Unit
    override fun onActivityStopped(activity: Activity) = Unit
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
    override fun onActivityDestroyed(activity: Activity) = Unit

    private fun addDebuggerView(debuggerViewModel: DebuggerViewModel) {
        if (this::currentActivity.isInitialized.not()) return

        val parentView = currentActivity.getParentView()
        if (parentView.findViewById<ComposeView?>(R.id.appcues_debugger_view) == null) {
            // Remove from previous parent if window changed (e.g. dialog opened/closed)
            debuggerParent.get()?.let { oldParent ->
                oldParent.findViewById<ComposeView?>(R.id.appcues_debugger_view)?.let {
                    oldParent.removeView(it)
                }
            }

            appcuesViewTreeOwner.init(parentView, currentActivity)
            setOnBackPressDispatcher(parentView)

            parentView.addView(
                ComposeView(currentActivity).apply {
                    id = R.id.appcues_debugger_view
                    layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
                    setContent {
                        AppcuesTheme(isTesting = appcuesConfig.isSnapshotTesting) {
                            DebuggerComposition(debuggerViewModel) {
                                stop()
                                parentView.removeView(this)
                            }
                        }
                    }
                }
            )
            debuggerParent = WeakReference(parentView)
        }
    }

    private fun removeDebuggerView() {
        if (this::currentActivity.isInitialized.not()) return

        // Try tracked parent first (handles cross-window case), fall back to getParentView
        val parent = debuggerParent.get() ?: currentActivity.getParentView()
        parent.findViewById<ComposeView?>(R.id.appcues_debugger_view)?.let {
            parent.removeView(it)
        }
        debuggerParent = WeakReference(null)
    }

    private fun startWindowMonitor() {
        windowMonitorJob?.cancel()
        windowMonitorJob = coroutineScope.launch(Dispatchers.Main) {
            while (true) {
                delay(WINDOW_MONITOR_INTERVAL_MS)
                checkWindowChanged()
            }
        }
    }

    private fun checkWindowChanged() {
        val vm = debuggerViewModel ?: return
        if (this::currentActivity.isInitialized.not()) return

        val currentParent = currentActivity.getParentView()
        val trackedParent = debuggerParent.get()
        if (trackedParent != null && trackedParent != currentParent) {
            addDebuggerView(vm)
        }
    }

    private fun setOnBackPressDispatcher(view: ViewGroup) {
        view.findViewTreeOnBackPressedDispatcherOwner()?.run {
            onBackPressCallback.remove()
            onBackPressedDispatcher.addCallback(onBackPressCallback)
        }
    }

    private val onBackPressCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            debuggerViewModel?.closeExpandedView()
        }
    }

    companion object {
        private const val WINDOW_MONITOR_INTERVAL_MS = 3000L
    }
}
