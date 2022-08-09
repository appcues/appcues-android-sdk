package com.appcues.debugger

import android.app.ActionBar.LayoutParams
import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewModelScope
import com.appcues.R
import com.appcues.debugger.DebuggerViewModel.UIState.Expanded
import com.appcues.debugger.ui.DebuggerComposition
import com.appcues.util.getNavigationBarHeight
import com.appcues.util.getStatusBarHeight
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import org.koin.core.scope.Scope
import kotlin.coroutines.CoroutineContext

internal class AppcuesDebuggerManager(context: Context, private val koinScope: Scope) : Application.ActivityLifecycleCallbacks {

    private val coroutineScope: CoroutineScope = object : CoroutineScope {
        override val coroutineContext: CoroutineContext = SupervisorJob() + Dispatchers.Main
    }

    private val application = context.applicationContext as Application

    private var debuggerViewModel: DebuggerViewModel? = null

    private lateinit var currentActivity: Activity

    private val onBackPressCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            debuggerViewModel?.onBackPress()
        }
    }

    fun start(activity: Activity, deepLinkPath: String? = null) = activity.runOnUiThread {
        this.currentActivity = activity
        coroutineScope.coroutineContext.cancelChildren()
        // it is possible to re-enter start without a stop (deepLinks) - in which case we continue to
        // use the VM we already have - else, make new one here
        val viewModel = debuggerViewModel ?: DebuggerViewModel(koinScope)
        debuggerViewModel = viewModel // and save reference
        viewModel.onStart(deepLinkPath)
        coroutineScope.launch {
            viewModel.uiState.collect { state -> onBackPressCallback.isEnabled = state is Expanded }
        }
        addDebuggerView(activity, viewModel)
        application.registerActivityLifecycleCallbacks(this)
        setDebuggerBackPressCallback(activity)
    }

    fun stop() {
        coroutineScope.coroutineContext.cancelChildren()
        removeDebuggerView()
        debuggerViewModel?.viewModelScope?.cancel() // stop the VM from listening to app activity
        application.unregisterActivityLifecycleCallbacks(this)
        onBackPressCallback.remove()
        debuggerViewModel = null // remove the reference to the current VM - new one will be made on next start()
    }

    override fun onActivityResumed(activity: Activity) {
        this.currentActivity = activity
        debuggerViewModel?.let { addDebuggerView(activity, it) }
    }

    override fun onActivityPostResumed(activity: Activity) {
        // not sure if this is necessary but I wanted to make sure we register our back press after everyone else
        // in case customer is using fragments with the same approach then our callback must be the last one so we can better
        // control native back press by enabling and disabling on our side
        setDebuggerBackPressCallback(activity)
        // also to make sure that if they change fragment, we will register again after the new fragment is attached
        (activity as? FragmentActivity)?.supportFragmentManager?.addFragmentOnAttachListener { _, _ ->
            setDebuggerBackPressCallback(activity)
        }
    }

    override fun onActivityPaused(activity: Activity) = Unit
    override fun onActivityStarted(activity: Activity) = Unit
    override fun onActivityStopped(activity: Activity) = Unit
    override fun onActivityCreated(activity: Activity, bundle: Bundle?) = Unit
    override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) = Unit
    override fun onActivityDestroyed(activity: Activity) = Unit

    private fun addDebuggerView(activity: Activity, debuggerViewModel: DebuggerViewModel) {
        getParentView(activity).also {
            // if view is not there
            if (it.findViewById<ComposeView>(R.id.appcues_debugger_view) == null) {
                // then we add
                it.addView(
                    ComposeView(activity).apply {

                        id = R.id.appcues_debugger_view

                        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT).apply {
                            // adds margin top and bottom according to visible status and navigation bar
                            setMargins(0, context.getStatusBarHeight(), 0, context.getNavigationBarHeight())
                        }

                        setContent {
                            MaterialTheme {
                                DebuggerComposition(debuggerViewModel) {
                                    stop()
                                    it.removeView(this)
                                }
                            }
                        }
                    }
                )
            }
        }
    }

    private fun removeDebuggerView() {
        // does nothing if currentActivity is not initialized
        if (this::currentActivity.isInitialized.not()) return

        getParentView(currentActivity).also {
            it.findViewById<ComposeView?>(R.id.appcues_debugger_view)?.run {
                it.removeView(this)
            }
        }
    }

    private fun getParentView(activity: Activity): ViewGroup {
        // if there is any difference in API levels we can handle it here
        return activity.window.decorView.rootView as ViewGroup
    }

    private fun setDebuggerBackPressCallback(activity: Activity) {
        // add onBackPressedDispatcher to handle internally native android back press when debugger is expanded
        if (activity is ComponentActivity) {
            onBackPressCallback.remove()

            // attach to the new activity
            activity.onBackPressedDispatcher.addCallback(onBackPressCallback)
        }
    }
}
