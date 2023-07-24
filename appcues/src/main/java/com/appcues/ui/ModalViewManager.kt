package com.appcues.ui

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout.LayoutParams
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.children
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.findViewTreeLifecycleOwner
import com.appcues.R
import com.appcues.data.model.RenderContext
import com.appcues.monitor.AppcuesActivityMonitor
import com.appcues.monitor.AppcuesActivityMonitor.ActivityMonitorListener
import com.appcues.ui.composables.AppcuesComposition
import com.appcues.ui.primitive.EmbedChromeClient
import com.appcues.ui.utils.getParentView
import org.koin.core.scope.Scope

internal class ModalViewManager(
    private val scope: Scope,
    private val renderContext: RenderContext
) : DefaultLifecycleObserver {

    private lateinit var viewModel: AppcuesViewModel
    private lateinit var gestureListener: ShakeGestureListener

    private val activityMonitorListener = object : ActivityMonitorListener {
        override fun onActivityChanged(activity: Activity) {
            viewModel.onActivityChanged()
        }
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        gestureListener.start()
    }

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
        gestureListener.stop()
    }

    fun start(): Boolean {
        val activity = AppcuesActivityMonitor.activity

        // inform the caller that we cannot currently add a view in the app
        if (activity == null || AppcuesActivityMonitor.isPaused) return false

        return activity.setupView()
    }

    private fun onCompositionDismiss() {
        AppcuesActivityMonitor.unsubscribe(activityMonitorListener)

        AppcuesActivityMonitor.activity?.removeView()
    }

    private fun Activity.setupView(): Boolean {
        AppcuesActivityMonitor.subscribe(activityMonitorListener)

        viewModel = AppcuesViewModel(scope, renderContext, ::onCompositionDismiss)
        gestureListener = ShakeGestureListener(this)

        val parentView = getParentView()
        parentView.findViewTreeLifecycleOwner()?.lifecycle?.addObserver(this@ModalViewManager)

        val overlayView = if (parentView.findViewById<View>(R.id.appcues_overlay_view) == null) {
            // then we add
            ComposeView(this).apply {
                id = R.id.appcues_overlay_view
                layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT).also {
                    // adds margin top and bottom according to visible status and navigation bar
                    ViewCompat.getRootWindowInsets(parentView)?.getInsets(WindowInsetsCompat.Type.systemBars())
                        ?.let { insets -> it.setMargins(insets.left, insets.top, insets.right, insets.bottom) }
                }

                // if debugger view exists, ensure we are positioned behind it.
                val debuggerView = parentView.findViewById<View>(R.id.appcues_debugger_view)
                if (debuggerView != null) {
                    parentView.addView(this, indexOfChild(debuggerView))
                } else {
                    parentView.addView(this)
                }
            }
        } else {
            // this is just a fallback that should never hit, but as a good practice if the view is already there
            // for some reason, we can just use it.
            parentView.findViewById(R.id.appcues_overlay_view)
        }

        overlayView.setContent {
            AppcuesComposition(
                viewModel = remember { viewModel },
                shakeGestureListener = remember { gestureListener },
                logcues = scope.get(),
                chromeClient = EmbedChromeClient(parentView),
            )
        }
        // remove customers view on accessibility stack
        parentView.setAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS)

        return true
    }

    private fun Activity.removeView() {
        getParentView().let {
            it.findViewTreeLifecycleOwner()?.lifecycle?.removeObserver(this@ModalViewManager)

            it.post {
                it.removeView(findViewById(R.id.appcues_overlay_view))
                // remove customers view on accessibility stack

                viewModel.onFinish()

                it.setAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES)
            }
        }
    }

    private fun ViewGroup.setAccessibility(accessibilityFlag: Int) {
        children.forEach { child ->
            if (child.id != R.id.appcues_overlay_view && child.id != R.id.appcues_debugger_view)
                child.importantForAccessibility = accessibilityFlag
        }
    }
}
