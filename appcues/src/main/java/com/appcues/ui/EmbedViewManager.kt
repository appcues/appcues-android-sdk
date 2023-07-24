package com.appcues.ui

import android.app.Activity
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.isVisible
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.findViewTreeLifecycleOwner
import com.appcues.RenderContextManager
import com.appcues.data.model.RenderContext
import com.appcues.monitor.AppcuesActivityMonitor
import com.appcues.monitor.AppcuesActivityMonitor.ActivityMonitorListener
import com.appcues.ui.composables.AppcuesComposition
import com.appcues.ui.primitive.EmbedChromeClient
import com.appcues.ui.utils.getParentView
import org.koin.core.scope.Scope

internal class EmbedViewManager(
    private val scope: Scope,
    private val renderContext: RenderContext.Embed,
    private val renderContextManager: RenderContextManager,
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

    private fun setViewVisible(isVisible: Boolean) {
        // adhering to requirement for the AppcuesViewModel constructor on line 71 below
        // would got called during any trait error/retry flow - which is not applicable here
        // for embeds, as it is only used for tooltips in flows, currently.
        renderContextManager.getEmbedView(renderContext)?.isVisible = isVisible
    }

    private fun Activity.setupView(): Boolean {
        val embedView = renderContextManager.getEmbedView(renderContext) ?: return false

        AppcuesActivityMonitor.subscribe(activityMonitorListener)

        viewModel = AppcuesViewModel(scope, renderContext, ::onCompositionDismiss, ::setViewVisible)
        gestureListener = ShakeGestureListener(this)

        val parentView = getParentView()
        parentView.findViewTreeLifecycleOwner()?.lifecycle?.addObserver(this@EmbedViewManager)

        embedView.addView(
            ComposeView(this).apply {
                setContent {
                    AppcuesComposition(
                        viewModel = remember { viewModel },
                        shakeGestureListener = remember { gestureListener },
                        logcues = scope.get(),
                        chromeClient = EmbedChromeClient(parentView),
                    )
                }
            }
        )
        return true
    }

    private fun Activity.removeView() {
        getParentView().findViewTreeLifecycleOwner()?.lifecycle?.removeObserver(this@EmbedViewManager)
        viewModel.onFinish()

        renderContextManager.getEmbedView(renderContext)?.run { post { removeAllViews() } }
    }
}
