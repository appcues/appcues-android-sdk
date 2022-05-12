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
import com.appcues.R
import com.appcues.debugger.DebuggerViewModel.UIState.Expanded
import com.appcues.debugger.ui.DebuggerComposition
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
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

    fun start(activity: Activity, deeplinkPath: String? = null) {
        this.currentActivity = activity
        coroutineScope.coroutineContext.cancelChildren()
        debuggerViewModel = DebuggerViewModel(koinScope, deeplinkPath).also {
            coroutineScope.launch {
                it.uiState.collect { state -> onBackPressCallback.isEnabled = state is Expanded }
            }

            addDebuggerView(activity, it)
        }

        application.registerActivityLifecycleCallbacks(this)

        setDebuggerBackPressCallback(activity)
    }

    private fun stop() {
        coroutineScope.coroutineContext.cancelChildren()

        removeDebuggerView(this.currentActivity)

        application.unregisterActivityLifecycleCallbacks(this)
        onBackPressCallback.remove()
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
        (activity as FragmentActivity).supportFragmentManager.addFragmentOnAttachListener { _, _ ->
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

                        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)

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

    private fun removeDebuggerView(activity: Activity) {
        getParentView(activity).also {
            it.findViewById<ComposeView?>(R.id.appcues_debugger_view)?.run {
                it.removeView(this)
            }
        }
    }

    private fun getParentView(activity: Activity): ViewGroup {
        // if there is any difference in API levels we can handle it here
        return activity.window.decorView.findViewById<ViewGroup>(android.R.id.content)
            .let { it.parent as ViewGroup }
            .let { it.parent as ViewGroup }
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
