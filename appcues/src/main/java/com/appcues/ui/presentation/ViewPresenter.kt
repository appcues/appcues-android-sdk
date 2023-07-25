package com.appcues.ui.presentation

import android.app.Activity
import android.view.ViewGroup
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.findViewTreeLifecycleOwner
import com.appcues.data.model.RenderContext
import com.appcues.monitor.AppcuesActivityMonitor
import com.appcues.monitor.AppcuesActivityMonitor.ActivityMonitorListener
import com.appcues.ui.composables.AppcuesComposition
import com.appcues.ui.primitive.EmbedChromeClient
import com.appcues.ui.utils.getParentView
import org.koin.core.scope.Scope

internal abstract class ViewPresenter(
    private val scope: Scope,
    protected val renderContext: RenderContext,
) : DefaultLifecycleObserver {

    private lateinit var viewModel: AppcuesViewModel
    private lateinit var gestureListener: ShakeGestureListener

    private val activityMonitorListener = object : ActivityMonitorListener {
        override fun onActivityChanged(activity: Activity) {
            viewModel.onActivityChanged()
        }
    }

    private val lifecycleObserver = object : DefaultLifecycleObserver {
        override fun onResume(owner: LifecycleOwner) {
            super.onResume(owner)
            gestureListener.start()
        }

        override fun onPause(owner: LifecycleOwner) {
            super.onPause(owner)
            gestureListener.stop()
        }
    }

    fun present(): Boolean {
        val activity = AppcuesActivityMonitor.activity

        // inform the caller that we cannot currently add a view in the app
        if (activity == null || AppcuesActivityMonitor.isPaused) return false

        activity.getParentView().run {
            // grab composeView or exit with false
            val composeView = setupView() ?: return false

            viewModel = AppcuesViewModel(scope, renderContext, ::onCompositionDismiss)
            gestureListener = ShakeGestureListener(activity)

            findViewTreeLifecycleOwner()?.lifecycle?.addObserver(lifecycleObserver)

            composeView.setContent {
                AppcuesComposition(
                    viewModel = remember { viewModel },
                    shakeGestureListener = remember { gestureListener },
                    logcues = scope.get(),
                    chromeClient = EmbedChromeClient(this),
                )
            }

            return true
        }
    }

    private fun onCompositionDismiss() {
        AppcuesActivityMonitor.unsubscribe(activityMonitorListener)

        AppcuesActivityMonitor.activity?.getParentView()?.run {
            findViewTreeLifecycleOwner()?.lifecycle?.removeObserver(lifecycleObserver)

            viewModel.onFinish()

            removeView()
        }
    }

    abstract fun ViewGroup.setupView(): ComposeView?

    abstract fun ViewGroup.removeView()
}
