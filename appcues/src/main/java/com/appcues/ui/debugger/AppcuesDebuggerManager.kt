package com.appcues.ui.debugger

import android.app.ActionBar.LayoutParams
import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import com.appcues.R
import org.koin.core.scope.Scope

internal class AppcuesDebuggerManager(context: Context, private val koinScope: Scope) : Application.ActivityLifecycleCallbacks {

    private val application = context.applicationContext as Application

    lateinit var currentActivity: Activity

    fun start(currentActivity: Activity) {
        this.currentActivity = currentActivity

        addDebuggerView()

        application.registerActivityLifecycleCallbacks(this)
    }

    private fun stop() {
        application.unregisterActivityLifecycleCallbacks(this)
    }

    override fun onActivityStarted(activity: Activity) {
        this.currentActivity = activity
    }

    override fun onActivityCreated(activity: Activity, bundle: Bundle?) = Unit
    override fun onActivityResumed(activity: Activity) = Unit
    override fun onActivityPaused(activity: Activity) = Unit
    override fun onActivityStopped(activity: Activity) = Unit
    override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) = Unit
    override fun onActivityDestroyed(activity: Activity) = Unit

    private fun addDebuggerView() {
        currentActivity.window.decorView.findViewById<ViewGroup>(android.R.id.content).also {
            // if view is not there
            if (it.findViewById<ComposeView>(R.id.appcues_debugger_view) == null) {
                // then we add
                it.addView(
                    ComposeView(currentActivity).apply {

                        id = R.id.appcues_debugger_view

                        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)

                        setContent {
                            DebuggerComposition(DebuggerViewModel(scope = koinScope)) {
                                stop()
                                it.removeView(this)
                            }
                        }
                    }
                )
            }
        }
    }
}
